package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigDao;
import com.xuecheng.manage_cms.dao.CmsPageDao;
import com.xuecheng.manage_cms.dao.CmsTemplateDao;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    private CmsPageDao cmsPageDao;

    @Autowired
    private CmsConfigDao cmsConfigDao;

    @Autowired
    private CmsTemplateDao cmsTemplateDao;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    /**
     * 根据id查询cms配置信息
     *
     * @param id
     * @return
     */
    public CmsConfig getModel(String id) {
        Optional<CmsConfig> optional = cmsConfigDao.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * 分页查询cmsPage
     *
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        // 包含页面名称模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching().
                withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        CmsPage cmsPage = new CmsPage();
        // 设置查询条件到cmsPage对象中
        cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        // 设置别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        // 设置站点id
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        // 设置模板id
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        Example<CmsPage> example = Example.of(cmsPage, matcher);

        // 处理页码
        if (page <= 0) {
            page = 1;
        }

        if (size <= 0) {
            size = 10;
        }
        // 传入初始值最小为1,但是Pageable页码从0开始
        page = page - 1;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> result = cmsPageDao.findAll(example, pageable);

        QueryResult<CmsPage> cmsPageQueryResult = new QueryResult<>();
        cmsPageQueryResult.setList(result.getContent());
        cmsPageQueryResult.setTotal(result.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, cmsPageQueryResult);
        return queryResponseResult;
    }

    /**
     * 增加页面
     *
     * @param cmsPage
     * @return
     */
    @Transactional
    public CmsPageResult add(CmsPage cmsPage) {
        if (cmsPage == null) {
            // 抛出异常
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage cmsPage1 = cmsPageDao.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) { // 存在相同页面
            // 抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        // 保存页面
        cmsPage.setPageId(null);
        cmsPageDao.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    public CmsPageResult save(CmsPage cmsPage) {
        if (cmsPage == null) {
            // 抛出异常
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage cmsPage1 = cmsPageDao.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            return update(cmsPage1.getPageId(), cmsPage);
        } else {
            return add(cmsPage);
        }
    }

    /**
     * 根据id查询页面
     *
     * @param id
     * @return
     */
    public CmsPage getById(String id) {
        Optional<CmsPage> optionalCmsPage = cmsPageDao.findById(id);
        if (optionalCmsPage.isPresent()) {
            return optionalCmsPage.get();
        }
        return null;
    }

    /**
     * 更新页面
     *
     * @param id
     * @param cmsPage
     * @return
     */
    @Transactional
    public CmsPageResult update(String id, CmsPage cmsPage) {
        CmsPage one = getById(id);
        if (one != null) {
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            // 更新dataUrl
            one.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage save = cmsPageDao.save(one);
            if (save != null) {
                return new CmsPageResult(CommonCode.SUCCESS, one);
            }
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 删除页面
     *
     * @param id
     * @return
     */
    @Transactional
    public ResponseResult delete(String id) {
        CmsPage one = getById(id);
        if (one != null) {
            cmsPageDao.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据页面id静态化页面
     *
     * @param pageId
     * @return
     */
    public String genHtml(String pageId) {
        CmsPage cmsPage = getById(pageId);
        // 判断页面是否为空
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXIST);
        }
        // 1.获取模型数据
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        Map model = getData(dataUrl);

        // 2.获取模板文件内容
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        String templateContent = getTemplate(templateId);
        if (StringUtils.isEmpty(templateContent)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        // 3.生成静态化页面内容
        String content = mergeModelAndTemplate(model, templateContent);
        if (StringUtils.isEmpty(content)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return content;
    }


    /**
     * 获取模型数据
     *
     * @param dataUrl
     * @return
     */
    private Map getData(String dataUrl) {
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        // 判断模型是否为空
        if (body == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        Map model = new HashMap();
        model.putAll(body);
        return model;
    }

    /**
     * 获取模板内容
     *
     * @param templateId
     * @return
     */
    private String getTemplate(String templateId) {
        Optional<CmsTemplate> optional = cmsTemplateDao.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            // 得到GridFS文件系统中模板文件的id
            String templateFileId = cmsTemplate.getTemplateFileId();
            // 根据文件id查询模板文件
            GridFSFile file = gridFsTemplate.findOne(Query.query(new Criteria("_id").is(templateFileId)));
            // 获得下载流
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
            try {
                // 读取模板文件内容
                return IOUtils.toString(gridFSDownloadStream, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 生成静态页面
     *
     * @param model           数据模型
     * @param templateContent 模板内容
     * @return 静态页面内容
     */
    private String mergeModelAndTemplate(Map model, String templateContent) {
        // 创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            // 获取模板
            Template template = configuration.getTemplate("template");
            // 得到静态化内容
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 页面发布
     *
     * @param pageId
     */
    public ResponseResult postPage(String pageId) {
        // 根据pageId生成静态文件内容
        String content = genHtml(pageId);
        if (StringUtils.isEmpty(content)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        // 存入gridFS
        String htmlField = saveHtmlToGridFS(pageId, content);
        // 更新cmsPage
        CmsPage cmsPage = getById(pageId);
        cmsPage.setHtmlFileId(htmlField);
        cmsPageDao.save(cmsPage);
        // 发送消息通知
        Map map = new HashMap();
        map.put("pageId", pageId);
        String message = JSON.toJSONString(map);
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, cmsPage.getSiteId(), message);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    private String saveHtmlToGridFS(String pageId, String content) {
        InputStream inputStream = null;
        try {
            inputStream = IOUtils.toInputStream(content, "utf-8");
            CmsPage cmsPage = getById(pageId);
            if (StringUtils.isNotEmpty(cmsPage.getHtmlFileId())) {
                gridFsTemplate.delete(Query.query(Criteria.where("_id").is(cmsPage.getHtmlFileId())));
            }
            ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
            return objectId.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return null;
    }

    public SysDictionary getDictionary(String dType) {
        if (StringUtils.isNotEmpty(dType)) {
            return sysDictionaryRepository.findByDType(dType);
        }
        return null;
    }

    public ResponseResult saveAndPostPage(CmsPage cmsPage){
        CmsPageResult save = save(cmsPage);
        if (!save.isSuccess()){
            return new ResponseResult(CommonCode.FAIL);
        }
        ResponseResult responseResult = postPage(save.getCmsPage().getPageId());
        if (!save.isSuccess()){
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
