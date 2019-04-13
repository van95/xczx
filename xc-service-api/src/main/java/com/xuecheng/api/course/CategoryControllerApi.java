package com.xuecheng.api.course;


import com.xuecheng.framework.domain.course.ext.CategoryNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="课程分类管理接口",description = "课程分类接口，提供数据模型的管理、查询接口")
public interface CategoryControllerApi {

    @ApiOperation("查询分类")
    public CategoryNode findCategoryList();
}
