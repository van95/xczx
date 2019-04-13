package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_course.ManageCourseApplication;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.service.CourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest(classes = ManageCourseApplication.class)
@RunWith(SpringRunner.class)
public class TestDao {

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseBaseMapper CourseBaseMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    TeachplanRepository teachplanRepository;



    @Autowired
    CourseService courseService;

    @Autowired
    CmsPageClient cmsPageClient;

    @Test
    public void testCourseBaseRepository() {
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }

    @Test
    public void testCourseBaseMapper() {
        CourseBase courseBase = CourseBaseMapper.findCourseBaseById("402885816240d276016240f7e5000002");
        System.out.println(courseBase);

    }

    @Test
    public void testTeachplanMapper() {
        TeachplanNode teachplanNode = teachplanMapper.selectList("4028e581617f945f01617f9dabc40000");
        System.out.println(teachplanNode);

    }

    @Test
    public void testCategoryMapper() {
        CategoryNode categoryList = categoryMapper.findCategoryList();
        System.out.println(categoryList);
    }


    @Test
    public void testTeachplanRepository() {
        List<Teachplan> byCourseidAndParentid = teachplanRepository.findByCourseidAndParentid("4028e581617f945f01617f9dabc40000", "1");
        System.out.println(byCourseidAndParentid);
    }

    @Test
    public void testFeign(){
        CmsPage byId = cmsPageClient.findOne("5a754adf6abb500ad05688d9");
        System.out.println(byId);
    }
}
