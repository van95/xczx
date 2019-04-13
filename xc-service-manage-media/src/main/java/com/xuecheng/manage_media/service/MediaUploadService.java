package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.domain.media.response.MediaFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String srcFolder;

    //视频处理路由
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    public  String routingkey_media_video;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /*   1、上传前检查上传环境
       检查文件是否上传，已上传则直接返回。
       检查文件上传路径是否存在，不存在则创建。*/

    /**
     * @param fileMd5  文件md5码
     * @param fileName 文件名称
     * @param fileSize 文件大小
     * @param mimetype 文件类型 video/mp4
     * @param fileExt  文件后缀名
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        String filePath = getFilePath(fileMd5, fileName, fileExt);
        File file = new File(filePath);
        // 判断文件是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (file.exists() && optional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        String fileFolderStr = getFileFolderPath(fileMd5);
        // 创建文件夹
        File fileFolder = new File(fileFolderStr);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /* 2、分块检查
     检查分块文件是否上传，已上传则返回true。
     未上传则检查上传路径是否存在，不存在则创建。*/
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String getChunkFilePath = getChunkFilePath(fileMd5, chunk);
        File chunkFile = new File(getChunkFilePath);
        if (chunkFile.exists() && chunkFile.length() == chunkSize) {
            // chunk已存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }

        if (chunkFile.exists()) {
            // chunk存在但大小不完整,删除
            chunkFile.delete();
        }
        File chunkFolder = new File(getChunkFolderPath(fileMd5));
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        return new CheckChunkResult(CommonCode.SUCCESS, false);
    }

    /*3、分块上传
    将分块文件上传到指定的路径。*/
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        File chunkFile = new File(getChunkFilePath(fileMd5, chunk));
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(chunkFile);
            inputStream = file.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            return new ResponseResult(CommonCode.SUCCESS);
        } catch (IOException e) {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }

    }

    /* 4、合并分块
     将所有分块文件合并为一个文件。
     在数据库记录文件信息。*/
    public MediaFileResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

//        if (!"avi".equals(fileExt)) {
//            return new MediaFileResult(MediaCode.MERGE_FILE_CHECKFAIL, null);
//        }
        // 合并分块
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        File chunkFoler = new File(chunkFolderPath);
        File[] files = chunkFoler.listFiles();
        List<File> list = Arrays.asList(files);
        list.sort((a, b) -> Integer.parseInt(a.getName()) - Integer.parseInt(b.getName()) > 0 ? 1 : -1);
        // 合并文件位置
        String video_path = getFilePath(fileMd5, fileName, fileExt);
        File mergeFile = new File(video_path);
        if (mergeFile.exists()) {
            mergeFile.delete();
        }
        try {
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            for (File file : list) {
                RandomAccessFile raf_read = new RandomAccessFile(file, "r");
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = raf_read.read(bytes)) != -1) {
                    raf_write.write(bytes, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new MediaFileResult(MediaCode.MERGE_FILE_FAIL, null);
        }
        // 检查md5
        boolean md5Check = checkMd5(mergeFile, fileMd5);
        if (!md5Check) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        // 生成mp4
        String mp4_name = fileMd5 + ".mp4";
        String mp4folder_path = getFileFolderPath(fileMd5);
        String m3u8Name = fileMd5 + ".m3u8";
        String m3u8folder = mp4folder_path + "/hls/";

        // 存入mongodb
        MediaFile mediaFile = new MediaFile();

        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileSize(fileSize);
        mediaFile.setFilePath(fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/");
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileStatus("301002");
        mediaFile.setUploadTime(new Date());

        mediaFileRepository.save(mediaFile);

        Map map = new HashMap();
        map.put("mediaId",fileMd5);

        String message = JSON.toJSONString(map);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,message);
        return new MediaFileResult(CommonCode.SUCCESS, mediaFile);


    }

    // 文件路径
    private String getFilePath(String fileMd5, String fileName, String fileExt) {
        return srcFolder + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    // 文件夹路径
    private String getFileFolderPath(String fileMd5) {
        return srcFolder + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/";
    }

    // chunk块路径
    private String getChunkFilePath(String fileMd5, Integer chunk) {
        return srcFolder + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/" + chunk;
    }

    // chunk文件夹路径
    private String getChunkFolderPath(String fileMd5) {
        return srcFolder + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk";
    }

    // 检查md5
    private boolean checkMd5(File file, String fileMd5) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            String md5 = DigestUtils.md5Hex(fis);
            if (fileMd5.equalsIgnoreCase(md5)) {
                return true;
            }
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return false;
    }
}
