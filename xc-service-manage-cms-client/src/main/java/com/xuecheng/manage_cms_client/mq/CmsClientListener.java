package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.manage_cms_client.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 页面发布监听类
 */
@Component
public class CmsClientListener {

    @Autowired
    PageService pageService;


    @RabbitListener(queues = "${xuecheng.mq.queue}")
    public void a(String message){
        if (StringUtils.isNotEmpty(message)){
            Map<String,String> map = JSON.parseObject(message, Map.class);
            String pageId = map.get("pageId");
            pageService.savePageToServerPath(pageId);
        }
    }
}
