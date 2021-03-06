package com.zzp.demo.springboot.es.controller;

import com.zzp.demo.springboot.es.enity.es.EsBlog;
import com.zzp.demo.springboot.es.enity.mysql.MysqlBlog;
import com.zzp.demo.springboot.es.repository.es.EsBlogRepository;
import com.zzp.demo.springboot.es.repository.mysql.MysqlBlogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
public class DataController {
    private static final String MYSQL = "mysql";
    private static final String ES = "es";

    @Autowired
    MysqlBlogRepository mysqlBlogRepository;

    @Autowired
    EsBlogRepository esBlogRepository;

    @GetMapping("/blogs")
    public Object blogList() {
        List<MysqlBlog> mysqlBlogs = mysqlBlogRepository.queryAll();
        return mysqlBlogs;
    }

    @PostMapping("/search")
    public Object search(@RequestBody Param param) {
        Map<String, Object> map = new HashMap<>();
        // 统计耗时
        StopWatch watch = new StopWatch();
        watch.start();
        String type = param.getType();
        // mysql 的搜索
        if (MYSQL.equals(type)) {
            List<MysqlBlog> mysqlBlogs = mysqlBlogRepository.queryBlogs(param.getKeyword());
            map.put("list", mysqlBlogs);
            // es 的搜索s
        } else if (ES.equals(type)) {
            BoolQueryBuilder builder = QueryBuilders.boolQuery();
            builder.should(QueryBuilders.matchPhraseQuery("title", param.getKeyword()));
            builder.should(QueryBuilders.matchPhraseQuery("content", param.getKeyword()));
            String s = builder.toString();
            log.info("s={}", s);
            Page<EsBlog> search = (Page<EsBlog>) esBlogRepository.search(builder);
            List<EsBlog> content = search.getContent();
            map.put("list", content);
        } else {
            return "I don't understand！";
        }
        watch.stop();
        // 计算耗时-毫秒
        long millis = watch.getTotalTimeMillis();
        map.put("duration", millis);
        return map;
    }

    @GetMapping("/blog/{id}")
    public Object blog(@PathVariable Integer id) {
        Optional<MysqlBlog> byId = mysqlBlogRepository.findById(id);
        MysqlBlog blog = byId.get();
//        return byId.get();
        return blog;
    }


    @Data
    // @Data 生成get、set方法
    private static class Param {
        // mysql es
        private String type;
        private String keyword;
    }

}
