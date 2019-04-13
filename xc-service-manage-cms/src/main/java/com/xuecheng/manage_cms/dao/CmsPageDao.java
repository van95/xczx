package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageDao extends MongoRepository<CmsPage,String> {
    /**
     * 根据页面名称,站点id和页面路径查找页面
     * @param pageName
     * @param siteId
     * @param pageWebPath
     * @return
     */
    public CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);
}
