package com.tan.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tan.blog.dao.mapper.TagMapper;
import com.tan.blog.dao.pojo.Tag;
import com.tan.blog.service.TagService;
import com.tan.blog.vo.Result;
import com.tan.blog.vo.TagVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    private List<TagVo> copyList(List<Tag> tagList) {
        List<TagVo> tagVoList=new ArrayList<>();
        for (Tag tag:tagList) {
            tagVoList.add(cope(tag));
        }
        return tagVoList;
    }
    private TagVo cope(Tag tag){
        TagVo tagVo=new TagVo();
        BeanUtils.copyProperties(tag,tagVo);
        tagVo.setId(String.valueOf(tag.getId()));
        return tagVo;

    }

    @Override
    public List<TagVo> findTagsByArticleId(Long articleId) {
        //mybatisPlus无法多表查询
        List<Tag> tags=tagMapper.findTagsByArticleId(articleId);
        return copyList(tags);
    }

    @Override
    public Result hots(int limit) {
        /**
         * 1. 标签所拥有的文章数量最多 最热标签
         * 2.查询 揯据tag_id分组技术，从大到小排序 取前limit个
         */
        List<Long> tagIds=tagMapper.findHotsTagIds(limit);
        //需求的是tagid和tagname;
        if(CollectionUtils.isEmpty(tagIds)){
            return Result.success(Collections.emptyList());
        }
        List<Tag> tagList=tagMapper.findTagsByTagIds(tagIds);

        return Result.success(tagList);
    }

    @Override
    public Result findAll() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Tag::getId,Tag::getTagName);
        List<Tag> tags = this.tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tags));
    }

    @Override
    public Result findAllDetail() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        List<Tag> tags = this.tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tags));
    }

    @Override
    public Result findDetailById(Long id) {
        Tag tag = this.tagMapper.selectById(id);
        return Result.success(cope(tag));
    }

}
