package com.xuecheng.ucenter.service;


import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    public XcUserExt getUserExt(String username) {
        // 查询用户
        XcUser xcUser = xcUserRepository.findByUsername(username);
        if (xcUser == null) {
            return null;
        }
        String userId = xcUser.getId();
        // 查询公司用户表
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(userId);
        String companyId = null;
        if (xcCompanyUser != null && StringUtils.isNotEmpty(xcCompanyUser.getCompanyId())) {
            companyId = xcCompanyUser.getCompanyId();
        }
        // 设置属性
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        xcUserExt.setCompanyId(companyId);
        return xcUserExt;
    }
}
