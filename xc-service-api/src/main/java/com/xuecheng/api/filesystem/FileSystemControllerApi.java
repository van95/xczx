package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value="文件系统管理接口",description = "文件系统接口，提供文件的上传、下载、查询等")
public interface FileSystemControllerApi {


    @ApiOperation("上传图片")
    public UploadFileResult upload(MultipartFile multipartFile, String businesskey, String filetag, String metadata);

}
