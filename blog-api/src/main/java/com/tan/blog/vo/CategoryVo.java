package com.tan.blog.vo;

import lombok.Data;

@Data
public class CategoryVo {

//id，图标路径，图标名称
    private String id;

    private String avatar;

    private String categoryName;

    private String description;
}