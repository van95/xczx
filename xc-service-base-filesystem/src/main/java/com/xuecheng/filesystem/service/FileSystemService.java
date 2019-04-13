package com.xuecheng.filesystem.service;


import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FileSystemService {
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    Integer connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    Integer network_timeout_in_seconds;

    @Value("${xuecheng.fastdfs.charset}")
    String charset;
    @Autowired
    FileSystemRepository fileSystemRepository;

    public UploadFileResult upload(MultipartFile multipartFile, String businesskey, String filetag, String metadata) {

        if (multipartFile==null){

        }

        // 上传图片
        String fileId = getFileId(multipartFile);
        if (StringUtils.isEmpty(fileId)) {
            return new UploadFileResult(FileSystemCode.FS_UPLOADFILE_SERVERFAIL, null);
        }
        // 将图片信息保存到mongodb xc_fs collection中

        FileSystem fileSystem = new FileSystem();
        if (StringUtils.isNotEmpty(metadata)) {
            Map map = null;
            try {
                map = JSON.parseObject(metadata, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_METAERROR);
            }
            fileSystem.setMetadata(map);
        }
        fileSystem.setFileId(fileId);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFilePath(fileId);
        fileSystem.setFileSize(multipartFile.getSize());
        fileSystem.setFiletag(filetag);
        fileSystem.setFileType(multipartFile.getContentType());

        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);

    }

    private String getFileId(MultipartFile multipartFile) {
        initServer();

        try {
//            TrackerClient trackerClient = new TrackerClient();
            StorageClient1 storageClient1 = new StorageClient1();
//            TrackerServer trackerServer = trackerClient.getConnection();
//            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
//            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storeStorage);
            String origin_name = multipartFile.getOriginalFilename();
            String file_ext_name = origin_name.substring(origin_name.lastIndexOf(".") + 1);

            String fileId = storageClient1.upload_file1(multipartFile.getBytes(), file_ext_name, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initServer() {

        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionCast.cast(FileSystemCode.FS_INIT_ERROR);
        }
    }
}

