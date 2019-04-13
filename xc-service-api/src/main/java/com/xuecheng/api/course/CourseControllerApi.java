package com.xuecheng.api.course;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CoursePreviewResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api(value = "课程管理接口", description = "课程管理接口，提供课程的增、删、改、查")
public interface CourseControllerApi {

    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    public ResponseResult addTeachPlan(Teachplan teachplan);

    @ApiOperation("查询我的课程列表")
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest);

    @ApiOperation("添加课程")
    public ResponseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("根据id查询课程基本信息")
    public CourseBase getCoursebaseById(String courseId);

    @ApiOperation("修改课程基本信息")
    public ResponseResult updateCoursebase(String courseId, CourseBase courseBase);

    @ApiOperation("根据id查询课程营销信息")
    public CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("修改课程营销信息")
    public ResponseResult updateCourseMarket(String courseId, CourseMarket courseMarket);

    @ApiOperation("添加图片信息")
    public ResponseResult addCoursePic(String courseId, String fileId);

    @ApiOperation("查询图片")
    public CoursePic findCoursePicList(String courseId);

    @ApiOperation("删除图片")
    public ResponseResult deleteCoursePic(String courseId);

    @ApiOperation("课程预览内容")
    public CourseView courseview(String courseId);

    @ApiOperation("课程预览")
    CoursePreviewResult preview(String courseId);

    @ApiOperation("课程发布")
    ResponseResult publish(String courseId);

    @ApiOperation("保存媒资信息")
    ResponseResult savemedia(TeachplanMedia teachplanMedia);
}
