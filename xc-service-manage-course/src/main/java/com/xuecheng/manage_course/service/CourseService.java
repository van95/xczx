package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CoursePreviewResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachMediaRepository teachMediaRepository;

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Value("${course-publish.siteId}")
    String siteId; // 站点id

    @Value("${course-publish.templateId}")
    String templateId; // 课程详情页id

    @Value("${course-publish.previewUrl}")
    String previewUrl; // 预览url

    @Value("${course-publish.pageWebPath}")
    String pageWebPath; // 页面路径

    @Value("${course-publish.pagePhysicalPath}")
    String pagePhysicalPath; // 页面物理路径

    @Value("${course-publish.dataUrlPre}")
    String dataUrlPre; // 数据模型url


    public TeachplanNode findTeachplanList(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        return teachplanMapper.selectList(courseId);
    }

    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Teachplan tp = new Teachplan();
        BeanUtils.copyProperties(teachplan, tp);
        // 设置 grade 等级属性
        if (StringUtils.isEmpty(teachplan.getParentid())) {
            // 设置parentId
            List<Teachplan> list = teachplanRepository.findByCourseidAndParentid(teachplan.getCourseid(), "0");
            Teachplan parent = null;
            if (list == null || list.size() <= 0) {
                parent = getRootParent(teachplan.getCourseid());
            } else {
                parent = list.get(0);
            }
            tp.setParentid(parent.getId());
            tp.setGrade("2");
        } else {
            tp.setGrade("3");
        }
        // courseId为空?
        teachplanRepository.save(tp);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private Teachplan getRootParent(String courseId) {
        Teachplan tp = new Teachplan();
        CourseBase courseBase = getCourseBaseById(courseId);
        tp.setGrade("1");
        tp.setParentid("0");
        tp.setStatus("0");
        tp.setPname(courseBase.getName());
        tp.setCourseid(courseId);
        teachplanRepository.save(tp);
        return tp;
    }

    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }
        // 开始分页
        PageHelper.startPage(page, size);
        Page<CourseBase> courseList = courseBaseMapper.findCourseList();
        List<CourseBase> result = courseList.getResult();
        QueryResult queryResult = new QueryResult();
        // 设置结果集
        queryResult.setList(result);
        // 设置总结果数
        queryResult.setTotal(courseList.getTotal());
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }


    @Transactional
    public ResponseResult addCourseBase(CourseBase courseBase) {
//        private String id;
//        private String name;
//        private String users;
//        private String mt;
//        private String st;
//        private String grade;
//        private String studymodel;
//        private String teachmode;
//        private String description;
//        private String status;
//        @Column(name="company_id")
//        private String companyId;
//        @Column(name="user_id")
//        private String userId;
        CourseBase cb = new CourseBase();
        BeanUtils.copyProperties(courseBase, cb);
        // 主键置空
        cb.setId(null);
        // 状态设置为制作中
        cb.setStatus("202001");
        courseBaseRepository.save(cb);


        return new ResponseResult(CommonCode.SUCCESS);
    }


    public CategoryNode findCategoryList() {
        return categoryMapper.findCategoryList();
    }

    public CourseBase getCourseBaseById(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult updateCourseBase(String courseId, CourseBase courseBase) {
        CourseBase courseBase1 = getCourseBaseById(courseId);
        if (courseBase1 == null) {
            return new ResponseResult(CommonCode.INVALID_PARAM);
        }
        courseBase.setId(courseId);
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseMarket getCourseMarketById(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket) {
        CourseMarket courseMarketById = getCourseMarketById(courseId);
        if (courseMarketById == null) {
            // 新增课程营销
            courseMarket.setId(courseId);
            courseMarketRepository.save(courseMarket);
        } else {
            // 修改课程营销
            courseMarketRepository.save(courseMarket);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePicList(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public ResponseResult deleteCoursePic(String courseId) {
        CoursePic pic = findCoursePicList(courseId);
        if (pic != null) {
            coursePicRepository.delete(pic);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CourseView getCourseView(String courseId) {
        CourseView courseView = new CourseView();
        // 课程基本信息
        CourseBase coursebaseById = this.getCourseBaseById(courseId);
        courseView.setCourseBase(coursebaseById);
        // 课程营销信息
        CourseMarket courseMarketById = getCourseMarketById(courseId);
        courseView.setCourseMarket(courseMarketById);
        // 课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if (picOptional.isPresent()) {
            courseView.setCoursePic(picOptional.get());
        }
        // 课程计划
        TeachplanNode teachplanNode = findTeachplanList(courseId);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    /**
     * 页面预览
     *
     * @param courseId
     * @return
     */
    public CoursePreviewResult preview(String courseId) {
        // 添加cmsPage,得到cmsPageResult
        CmsPage cmsPage = initCmsPage(courseId);
        CmsPageResult result = cmsPageClient.save(cmsPage);
        if (!result.isSuccess()) {
            return new CoursePreviewResult(CommonCode.FAIL, null);
        }
        CmsPage resultCmsPage = result.getCmsPage();
        if (!result.isSuccess()) {
            return new CoursePreviewResult(CommonCode.FAIL, null);
        }
        // 得到pageId
        String pageId = resultCmsPage.getPageId();

        return new CoursePreviewResult(CommonCode.SUCCESS, previewUrl + pageId);
    }

    private CmsPage initCmsPage(String courseId) {
        CmsPage cmsPage = new CmsPage();
        cmsPage.setDataUrl(dataUrlPre + courseId);
        cmsPage.setPageName(courseId + ".html");
        // 设置别名
        CourseBase courseBase = getCourseBaseById(courseId);
        if (courseBase != null) {
            cmsPage.setPageAliase(courseBase.getName());
        }
        cmsPage.setTemplateId(templateId);
        cmsPage.setSiteId(siteId);
        cmsPage.setPagePhysicalPath(pagePhysicalPath);
        cmsPage.setPageWebPath(pageWebPath);
        cmsPage.setPageCreateTime(new Date());
        cmsPage.setPageStatus("202001"); // 制作中

        return cmsPage;
    }

    @Transactional
    public ResponseResult publish(String courseId) {
        CmsPage cmsPage = initCmsPage(courseId);
        // 生成页面
        ResponseResult responseResult = cmsPageClient.saveAndPostPage(cmsPage);
        if (!responseResult.isSuccess()) {
            return new ResponseResult(CommonCode.FAIL);
        }

        // 修改课程状态
        CourseBase courseBase = getCourseBaseById(courseId);
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);

        // 生成coursePub
        CoursePub coursePub = getCoursePub(courseId);
        saveCoursePub(courseId, coursePub);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    private CoursePub getCoursePub(String courseId) {
        CoursePub coursePub = new CoursePub();
        CourseView courseView = getCourseView(courseId);
        BeanUtils.copyProperties(courseView.getCourseBase(), coursePub);
        BeanUtils.copyProperties(courseView.getCourseMarket(), coursePub);
        BeanUtils.copyProperties(courseView.getCoursePic(), coursePub);
        // 设置主键
        coursePub.setId(courseId);
        // 设置时间戳
        coursePub.setTimestamp(new Date());
        // 设置teachPlan
        TeachplanNode teachplanNode = courseView.getTeachplanNode();
        String teachPlan = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachPlan);
        // 设置发布时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String pubTime = sdf.format(new Date());
        coursePub.setPubTime(pubTime);
        return coursePub;
    }

    private void saveCoursePub(String courseId, CoursePub coursePub) {
        Optional<CoursePub> optional = coursePubRepository.findById(courseId);
        if (optional.isPresent()) {
            // 存在coursePub
            coursePubRepository.save(coursePub);
        } else {
            // 不存在coursePub
            coursePubRepository.save(coursePub);
        }
    }

    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId()) || StringUtils.isEmpty(teachplanMedia.getMediaId())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        // 只能为节点为3的课程计划添加媒资信息
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanMedia.getTeachplanId());
        if(!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optional.get();
        //只允许为叶子结点课程计划选择视频
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询课程计划
        Optional<TeachplanMedia> optionalTeachplanMedia = teachMediaRepository.findById(teachplanMedia.getTeachplanId());
        TeachplanMedia tm = null;
        if (optionalTeachplanMedia.isPresent()) {
            // 数据库有teachplanMedia
             tm = optionalTeachplanMedia.get();
        } else {
            // 数据库没有teachplanMedia
             tm = new TeachplanMedia();
        }
        // 保存课程计划媒资信息
        tm.setTeachplanId(teachplanMedia.getTeachplanId());
        tm.setCourseId(teachplanMedia.getCourseId());
        tm.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        tm.setMediaUrl(teachplanMedia.getMediaUrl());
        tm.setMediaId(teachplanMedia.getMediaId());
        teachMediaRepository.save(tm);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
