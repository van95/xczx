package com.xuecheng;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.ManageCmsApplication;
import com.xuecheng.manage_cms.dao.CmsPageDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = ManageCmsApplication.class)
@RunWith(SpringRunner.class)
public class test {

    @Autowired
    CmsPageDao cmsPageDao;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void testFind(){
        List<CmsPage> all = cmsPageDao.findAll();
        System.out.println(all);
    }
}
