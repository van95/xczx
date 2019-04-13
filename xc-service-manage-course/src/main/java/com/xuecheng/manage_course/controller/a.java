package com.xuecheng.manage_course.controller;

import com.xuecheng.framework.web.BaseController;

import javax.servlet.http.HttpSession;

public class a extends BaseController {

    public void a (){


        HttpSession session = request.getSession();
        session.invalidate();
        String username = (String) session.getAttribute("username");
    }
}
