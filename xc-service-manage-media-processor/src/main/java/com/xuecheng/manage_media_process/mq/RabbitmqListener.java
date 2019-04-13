package com.xuecheng.manage_media_process.mq;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RabbitmqListener {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String srcFolder;

    @Value("${xc-service-manage-media.ffmpeg}")
    String ffmpeg_path;


    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}", containerFactory = "factory")
    public void processMedia(String message) {

        // 解析message
        Map<String, String> map = JSON.parseObject(message, Map.class);
        String mediaId = map.get("mediaId");

        // 查询数据库
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            return;
        }

        MediaFile mediaFile = optional.get();
        if (!mediaFile.getFileName().endsWith(".avi")) {
            // 不以avi结尾的文件不处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }
        // 处理中
        mediaFile.setProcessStatus("303001");
        mediaFileRepository.save(mediaFile);

        String fileId = mediaFile.getFileId();

        // 生成mp4
        String mp4_name = fileId + ".mp4";
        String video_path = srcFolder + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4folder_path = srcFolder + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        String mp4_result = mp4VideoUtil.generateMp4();

        if (!"success".equals(mp4_result)) {
            mediaFile.setProcessStatus("303003");
            mediaFileRepository.save(mediaFile);
            return;

        } else {
            // 生成m3u8
            video_path = srcFolder + mediaFile.getFilePath() + mp4_name;
            String m3u8Name = fileId + ".m3u8";
            String m3u8folder = srcFolder + mediaFile.getFilePath() + "hls/";
            HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, video_path, m3u8Name, m3u8folder);
            String m3u8_result = hlsVideoUtil.generateM3u8();
            if (!"success".equals(m3u8_result)) {
                mediaFile.setProcessStatus("303003");
                mediaFileRepository.save(mediaFile);
                return;
            }
            // 保存到数据库
            mediaFile.setFileType("avi");
            mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8Name);
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setTslist(hlsVideoUtil.get_ts_list());
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

            mediaFile.setProcessStatus("303002");
            mediaFileRepository.save(mediaFile);
        }
    }
}
