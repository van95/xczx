package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private PageService pageService;

    @GetMapping("/cms/preview/{id}")
    public void genHtml(@PathVariable("id") String id)  {
        String content = pageService.genHtml(id);
        if (StringUtils.isNotEmpty(content)){
            try {
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
