package com.xuecheng.manage_cms.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
@RequestMapping("/freemarker")
public class FreemarkerController {


    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/test")
    public String genHtml(Map map){
        // http://localhost:31001/cms/config/getModel/5a791725dd573c3574ee333f
        // C:\Users\76065\Desktop\course.ftl

        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getModel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        return "index_banner";
    }

    @RequestMapping("/test1")
    public String genHtml1(Map map){
        // http://localhost:31001/cms/config/getModel/5a791725dd573c3574ee333f
        // C:\Users\76065\Desktop\course.ftl

        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/detail/4028e581617f945f01617f9dabc40000", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        return "course";
    }

}
