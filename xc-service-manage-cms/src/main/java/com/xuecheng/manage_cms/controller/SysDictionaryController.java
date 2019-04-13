package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.course.SysDictionaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/sys")
public class SysDictionaryController implements SysDictionaryControllerApi {

    @Autowired
    PageService pageService;


    @Override
    @GetMapping("/dictionary/get/{dType}")
    public SysDictionary getDictionary(@PathVariable("dType") String dType) {
        return pageService.getDictionary(dType);
    }
}
