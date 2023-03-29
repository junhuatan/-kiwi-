package com.tan.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tan.blog.Utils.UserThreadLocal;
import com.tan.blog.dao.dos.Archives;
import com.tan.blog.dao.mapper.ArticleBodyMapper;
import com.tan.blog.dao.mapper.ArticleMapper;
import com.tan.blog.dao.mapper.ArticleTagMapper;
import com.tan.blog.dao.pojo.Article;
import com.tan.blog.dao.pojo.ArticleBody;
import com.tan.blog.dao.pojo.ArticleTag;
import com.tan.blog.dao.pojo.SysUser;
import com.tan.blog.service.*;
import com.tan.blog.vo.*;

import com.tan.blog.vo.params.ArticleParam;
import com.tan.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired(required = false)
    private ArticleMapper articleMapper;

    @Autowired(required = false)
    private TagService tagService;

    @Autowired(required = false)
    private SysUserService sysUserService;

    @Autowired(required = false)
    private ArticleTagMapper articleTagMapper;

    @Override
    public Result listArticle(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<Article> articleIPage = articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth());
        List<Article> records = articleIPage.getRecords();
        return Result.success(copyList(records,true,true));
    }


//    @Override
//    public Result listArticle(PageParams pageParams) {
//        /**
//         * 1.分页查询article数据库表
//         */
//        Page<Article> page=new Page<>(pageParams.getPage(),pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
//        if(pageParams.getCategoryId() != null) {
//            // and category_id = #{categoryId}
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList = new ArrayList<>();
//        if(pageParams.getTagId() != null) {
//            //加入标签条件查询
//            //article表中并没有tag字段 一篇文章有多个标签
//            //articie_tog article_id 1：n tag_id
//            //我们需要利用一个全新的属于文章标签的queryWrapper将这篇文章的article_Tag查出来，保存到一个list当中。
//            // 然后再根据queryWrapper的in方法选择我们需要的标签即可。
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper=new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if(articleIdList.size() > 0){
//                //and id in(1.2.3)
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//
//        //1.是否置顶排序
////        queryWrapper.orderByDesc(Article::getWeight); 可以多种一起
//        //2order by create_data descs时间倒叙
//
//        System.out.println("11111111111111111111111111111111111111111111111111");
//        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
//        System.out.println("2222222222222222222222222222222222222222222222222");
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        System.out.println("333333333333333333333333333333333333333333333333333333");
//        List<Article> records=articlePage.getRecords();
//        //能直接返回吗  ？ 明显不能
//        List<ArticleVo> articleVoList =copyList(records,true,true);
//        return Result.success(articleVoList);
//    }


    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit "+ limit);
        //== select id,title from article order by view_counts desc limit 5
        List<Article> articles=articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit "+ limit);
        //== select id,title from article order by CreateDate desc limit 5
        List<Article> articles=articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList=articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 1. 根据id查询 文章信息
         * 2. 根据bodyId和categoryId 去做关联查询
         */
        Article article = this.articleMapper.selectById(articleId);
        ArticleVo articleVo = cope(article, true, true,true,true);
        //查看完文章了，新增阅读数，有没有问题呢？
        //查看完文章之后，本应该直接返回数据了，这时候做了一个更新操作，更新时加写锁，阻塞其他的读操作，性能就会比较低
        // 更新 增加了此次接口的 耗时 如果一旦更新出问题，不能影响 查看文章的操作
        //线程池  可以把更新操作 扔到线程池中去执行，和主线程就不相关了
        //threadService.updateArticleViewCount(articleMapper,article);
        threadService.updateArticleCount(articleMapper,article);

        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        //其接口 要加入拦截器中
        SysUser sysUser = UserThreadLocal.get();
        /**
         * 1. 发布文章 目的 构建Article对象
         * 2. 作者id  当前的登录用户
         * 3. 标签  要将标签加入到 关联列表当中
         * 4. body 内容存储 article bodyId
         */
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        //插入之后 会生成一个文章id
        this.articleMapper.insert(article);
        //tag
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setArticleId(article.getId());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);

        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        //创建文章
        Map<String,String> map = new HashMap<>();
        map.put("id", article.getId().toString());
        return Result.success(map);
    }


    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor) {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for (Article record:records) {
            articleVoList.add(cope(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList=new ArrayList<>();
        for (Article record:records) {
            articleVoList.add(cope(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo cope(Article article,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory){
        ArticleVo articleVo=new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
//        articleVo.setId(article.getId());
        BeanUtils.copyProperties(article,articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));

        if(isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }

        if(isAuthor){
            Long authorId=article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }

        if(isBody){
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }

        if(isCategory){
            Long categoryId=article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }
        return articleVo;

    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo=new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
