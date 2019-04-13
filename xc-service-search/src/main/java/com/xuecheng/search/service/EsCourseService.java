package com.xuecheng.search.service;


import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EsCourseService {
    @Value("${xuecheng.course.source_field}")
    String source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;


    public QueryResponseResult<CoursePub> search(int page, int size, CourseSearchParam courseSearchParam) {

        if (courseSearchParam == null) {
            courseSearchParam = new CourseSearchParam();
        }
        // 查询对象
        SearchRequest searchRequest = new SearchRequest();
        // 创建查询源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 结果对象字段
        searchSourceBuilder.fetchSource(source_field.split(","), new String[0]);
        // 设置查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders
                    .multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10f);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        // 过滤查询
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        if (courseSearchParam.getPrice_min() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(courseSearchParam.getPrice_min()));
        }
        if (courseSearchParam.getPrice_max() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(courseSearchParam.getPrice_max()));
        }

        searchSourceBuilder.query(boolQueryBuilder);

        // 高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        // 分页查询
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 2;
        }
        searchSourceBuilder.from((page - 1) * size);
        searchSourceBuilder.size(size);

        searchRequest.source(searchSourceBuilder);

        try {
            // 发送查询请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            // 得到结果集
            SearchHits hits = searchResponse.getHits();
            // 结果集条数
            long totalHits = hits.getTotalHits();

            List<CoursePub> list = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                // 创建coursePub对象
                CoursePub coursePub = new CoursePub();

                // 设置高亮
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields.size()>0){
                    HighlightField highlightField = highlightFields.get("name");
                    if (highlightField!=null){
                        Text[] texts = highlightField.getFragments();
                        StringBuilder sb = new StringBuilder();
                        for (Text text : texts) {
                            sb.append(text);
                        }
                        coursePub.setName(sb.toString());
                    }
                }

                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                if (StringUtils.isEmpty(coursePub.getName())){
                    coursePub.setName((String) sourceAsMap.get("name"));
                }
                // 价格
                Double price = (Double) sourceAsMap.get("price");
                coursePub.setPrice(price);
                // 原价格
                Double price_old = (Double) sourceAsMap.get("price_old");
                coursePub.setPrice_old(price_old);
                // 图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);

                list.add(coursePub);
            }
            QueryResult<CoursePub> queryResult = new QueryResult<>();
            queryResult.setTotal(totalHits);
            queryResult.setList(list);
            return new QueryResponseResult<CoursePub>(CommonCode.SUCCESS, queryResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new QueryResponseResult<CoursePub>(CommonCode.FAIL, null);
    }


}
