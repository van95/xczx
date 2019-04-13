package com.xuecheng.manage_cms_client.service;


import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms_client.dao.CmsPageDao;
import com.xuecheng.manage_cms_client.dao.CmsSiteDao;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    CmsPageDao cmsPageDao;

    @Autowired
    CmsSiteDao cmsSiteDao;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    GridFsTemplate gridFsTemplate;


    public void savePageToServerPath(String pageId) {
        // 得到cmsPage
        CmsPage cmsPage = getCmsPageById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXIST);
        }
        // 根据pageId得到HtmlField
        String htmlFileId = cmsPage.getHtmlFileId();
        // 根据HtmlField得到静态页面内容
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        if (gridFSDownloadStream==null){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        // 将静态页面写入服务器地址
        CmsSite cmsSite = getCmsSiteById(cmsPage.getSiteId());
        // 得到服务器路径
        String path = cmsSite.getSitePhysicalPath() + cmsPage.getPageWebPath() + cmsPage.getPageName();
        FileOutputStream fileOutputStream = null;
        // 将文件保存到服务器
        try {
            fileOutputStream = new FileOutputStream(path);
            IOUtils.copy(gridFSDownloadStream, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(gridFSDownloadStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    public CmsPage getCmsPageById(String pageId) {
        Optional<CmsPage> optional = cmsPageDao.findById(pageId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public CmsSite getCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteDao.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
