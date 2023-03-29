package com.tan.blog.service;

import com.tan.blog.vo.CategoryVo;
import com.tan.blog.vo.Result;

import java.util.List;

public interface CategoryService {

    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(Long id);
}
