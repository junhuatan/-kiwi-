package com.tan.blog.dao.pojo;

import lombok.Data;
//内容表
@Data
public class ArticleBody {

    private Long id;

    private String content;

    private String contentHtml;

    private Long articleId;

}