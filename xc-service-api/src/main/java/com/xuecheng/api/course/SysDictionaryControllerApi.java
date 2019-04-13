package com.xuecheng.api.course;


import com.xuecheng.framework.domain.system.SysDictionary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="数据字典管理接口",description = "数据字典接口，提供数据字典的管理、查询接口")
public interface SysDictionaryControllerApi {

    @ApiOperation("查询数据字典")
    public SysDictionary getDictionary(String dType);
}
