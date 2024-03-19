package com.nowcoder.community.dao.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.nowcoder.community.entity.DiscussPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Repository
public class DiscussPostDocument {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostDocument.class);


    /**
     * 添加文档
     */
    public void AddDocument (DiscussPost post) throws IOException {
        IndexResponse indexResponse = elasticsearchClient.index(indexRequest ->
                indexRequest.index("discusspost").document(post).id(String.valueOf(post.getId()))
        );
        logger.info("== response: {}, responseStatus: {}", indexResponse, indexResponse.result());
    }

    /**
     * 获取文档信息
     */
    public DiscussPost GetDocument (int id) throws IOException {
        GetResponse<DiscussPost> getResponse = elasticsearchClient.get(getRequest ->
                getRequest.index("discusspost").id(String.valueOf(id)), DiscussPost.class
        );

        logger.info("== document source: {}, response: {}", getResponse.source(), getResponse);
        return getResponse.source();
    }

    /**
     * 更新文档信息
     */
    public void UpdateDocument (DiscussPost discussPost) throws IOException {
        UpdateResponse<DiscussPost> updateResponse = elasticsearchClient.update(updateRequest ->
                updateRequest.index("discusspost").id(String.valueOf(discussPost.getUserId()))
                        .doc(discussPost), DiscussPost.class
        );
        logger.info("== response: {}, responseStatus: {}", updateResponse, updateResponse.result());
    }

    /**
     * 删除文档信息
     */
    public void DeleteDocument (int id) throws IOException {
        DeleteResponse deleteResponse = elasticsearchClient.delete(deleteRequest ->
                deleteRequest.index("discusspost").id(String.valueOf(id))
        );
        logger.info("== response: {}, result:{}", deleteResponse, deleteResponse.result());

    }

    /**
     * 批量插入文档
     */
    public void BatchInsert (List<DiscussPost> postList) throws IOException {

        List<BulkOperation> bulkOperationList = new ArrayList<>();

        for (int i=0; i< postList.size(); i++) {
            int finalI = i;
            bulkOperationList.add(new BulkOperation.Builder().create(
                    e -> e.document(postList.get(finalI)).id(String.valueOf(postList.get(finalI).getId()))).build());
        }

        BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest ->
                bulkRequest.index("discusspost").operations(bulkOperationList)
        );

        // 这边插入成功的话显示的是 false
        logger.info("== errors: {}", bulkResponse.errors());
    }
}
