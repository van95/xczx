package com.xuecheng;


import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.manage_cms.ManageCmsApplication;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = ManageCmsApplication.class)
@RunWith(SpringRunner.class)
public class FreemarkerTest {

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Test
    public void testSave() throws IOException {
        InputStream in = new FileInputStream("C:\\Users\\76065\\Desktop\\course.ftl");
        ObjectId objectId = gridFsTemplate.store(in, "课程详情模板","");
        System.out.println(objectId); // 5c7a40756a51607da446df64
        in.close();
    }

    @Test
    public void testDownload() throws IOException {
        GridFSFile file = gridFsTemplate.findOne(Query.query(new Criteria("_id").is("5c73b56c520e2055a86d67e6")));
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
        String s1 = IOUtils.toString(gridFSDownloadStream);
        System.out.println(s1);
        GridFsResource gridFsResource = new GridFsResource(file,gridFSDownloadStream);
        InputStream inputStream = gridFsResource.getInputStream();
        String s = IOUtils.toString(inputStream);
        System.out.println(s);
    }

    //基于模板生成静态化文件
    @Test
    public void testGenerateHtml1() throws IOException, TemplateException {
        //创建配置类
        Configuration configuration=new Configuration(Configuration.getVersion());
        //设置模板路径
        String classpath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //设置字符集
        configuration.setDefaultEncoding("utf‐8");
        //加载模板
        Template template = configuration.getTemplate("test.ftl");
        //数据模型
        Map<String,Object> map = new HashMap<>();
        map.put("name","黑马程序员");//静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/test1.html"));
        int copy = IOUtils.copy(inputStream, fileOutputStream);
    }

    //基于模板字符串生成静态化文件
    @Test
    public void testGenerateHtmlByString1() throws IOException, TemplateException {
        //创建配置类
        Configuration configuration=new Configuration(Configuration.getVersion());
        //模板内容，这里测试时使用简单的字符串作为模板
        String templateString="" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                " 名称：${name}\n" +
                " </body>\n" +
                "</html>";
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        //得到模板
        Template template = configuration.getTemplate("template","utf‐8");
        //数据模型
        Map<String,Object> map = new HashMap<>();
        map.put("name","黑马程序员");
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //静态化内容
        System.out.println(content);
        InputStream inputStream = IOUtils.toInputStream(content);
        //输出文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("f:/test1.html"));
        IOUtils.copy(inputStream, fileOutputStream);
    }


    //基于模板生成静态化文件
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //创建配置类
        Configuration configuration=new Configuration(Configuration.getVersion());
        //设置模板路径
        String classpath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //设置字符集
//        configuration.setDefaultEncoding("utf‐8");
        //加载模板
        Template template = configuration.getTemplate("test.ftl");
        //数据模型
        Map<String,Object> map = new HashMap<>();
        map.put("name","黑马程序员");//静态化

        Writer writer= new OutputStreamWriter(new FileOutputStream(new File("f:/test1.html")),"utf-8");
        template.process(map,writer);
    }
}
