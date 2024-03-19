package com.nowcoder.community.dao.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.reindex.Source;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSONObject;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import com.nowcoder.community.CommunityApplication;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DiscussPostSearch {

    @Autowired
    private  ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "discusspost";
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostSearch.class);


    /**
     * 根据 name 查询相应的文档， search api 才是 elasticsearch-client 的优势，可以看出使用 lambda 大大简化了代码量，
     * 可以与 restHighLevelClient 形成鲜明的对比，但是也有可读性较差的问题，所以 lambda 的基础要扎实
     */
    public Page<DiscussPost> RestClient (String searchText,int current,int pageSize) throws IOException {





        SearchResponse<DiscussPost> search = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q ->
                                q.match(t ->t
                                        .field("title")
                                        .field("content")
                                        .query(searchText)
                                )
                        )
                        .from((current) * pageSize)
                        .size(pageSize)
                        .sort(List.of(
                                SortOptionsBuilders
                                        .field(f->f
                                                .field("type")
                                                .order(SortOrder.Desc)),
                                SortOptionsBuilders
                                        .field(f->f
                                                .field("score")
                                                .order(SortOrder.Desc)),
                                SortOptionsBuilders
                                        .field(f->f
                                                .field("createTime")
                                                .order(SortOrder.Desc))
                                )

                        )

                        .highlight(builder -> builder
                                .fields(Map.of(
                                                "content", // 第一个要高亮的字段名
                                                HighlightField.of(hf -> hf
                                                        .fragmentSize(150) // 每个高亮片段的最大字符数
                                                        .numberOfFragments(3) // 返回的最大片段数
                                                        .preTags("<em>") // 高亮前缀
                                                        .postTags("</em>") // 高亮后缀
                                                ),
                                                "title", // 第二个要高亮的字段名
                                                HighlightField.of(hf -> hf
                                                        .fragmentSize(50) // 可以为不同字段设置不同的片段大小
                                                        .numberOfFragments(1) // 可以为不同字段设置不同的片段数
                                                        .preTags("<em>")
                                                        .postTags("</em>")
                                                )
                                    )
                                )
                        ),
                DiscussPost.class);
        List<DiscussPost> resultList = search.hits().hits().stream().map(hit -> {
            DiscussPost source = hit.source();
            Map<String, List<String>> highlightFields = hit.highlight();
            String title = source.getTitle();
            String content = source.getContent();
            // 替换高亮字段
            if (highlightFields.containsKey("title")) {
                title = highlightFields.get("title").getFirst();
            }
            if (highlightFields.containsKey("content")) {
                content = highlightFields.get("content").getFirst();
            }
            return getDiscussPost(source, title, content);
        }).collect(Collectors.toList());

        long totalHits = search.hits().total().value();
        Pageable pageable= Pageable.ofSize(pageSize).withPage(current);
        return new PageImpl<>(resultList, pageable, totalHits);


    }

    private static DiscussPost getDiscussPost(DiscussPost source, String title, String content) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(source.getId());
        discussPost.setUserId(source.getUserId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setType(source.getType());
        discussPost.setStatus(source.getStatus());
        discussPost.setCreateTime(source.getCreateTime());
        discussPost.setCommentCount(source.getCommentCount());
        discussPost.setScore(source.getScore());
        return discussPost;
    }


}
