package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;


public interface CourseBaseMapper {

    Page<CourseBase> findCourseList();

    CourseBase findCourseBaseById(String id);
}
