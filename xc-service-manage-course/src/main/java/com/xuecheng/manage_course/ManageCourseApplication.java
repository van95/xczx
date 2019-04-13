package com.xuecheng.manage_course;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Administrator
 * @version 1.0
 **/

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.course")//扫描实体类
@ComponentScan(basePackages={"com.xuecheng.api"})//扫描接口
@ComponentScan(basePackages={"com.xuecheng.manage_course"})
@ComponentScan(basePackages={"com.xuecheng.framework"})//扫描common下的所有类
@MapperScan(basePackages = "com.xuecheng.manage_course.dao")
public class ManageCourseApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ManageCourseApplication.class, args);
    }
}
