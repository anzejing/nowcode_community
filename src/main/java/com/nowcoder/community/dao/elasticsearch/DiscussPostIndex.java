package com.nowcoder.community.dao.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.nowcoder.community.entity.DiscussPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class DiscussPostIndex {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostIndex.class);

    /**
     * 创建索引 - 不指定 mapping
     */
    public void createIndex (String indexName) throws IOException {
        CreateIndexResponse createIndexResponse = elasticsearchClient.indices()
                .create(createIndexRequest ->
                        createIndexRequest.index(indexName)
                );
        logger.info("== {} 索引创建是否成功: {}", indexName, createIndexResponse.acknowledged());
    }


    /**
     * 判断索引是否存在
     */
    public void indexIsExist (String indexName) throws IOException {
        BooleanResponse booleanResponse = elasticsearchClient.indices()
                .exists(existsRequest ->
                        existsRequest.index(indexName)
                );

        logger.info("== {} 索引创建是否存在: {}", indexName, booleanResponse.value());
    }

    /**
     * 查看索引的相关信息
     */
    public void indexDetail (String indexName) throws IOException {
        GetIndexResponse getIndexResponse = elasticsearchClient.indices()
                .get(getIndexRequest ->
                        getIndexRequest.index(indexName)
                );

        Map<String, Property> properties = getIndexResponse.get(indexName).mappings().properties();

        for (String key : properties.keySet()) {
            logger.info("== {} 索引的详细信息为: == key: {}, Property: {}", indexName, key, properties.get(key)._kind());
        }

    }

    /**
     * 删除索引
     */
    public void deleteIndex (String indexName) throws IOException {
        DeleteIndexResponse deleteIndexResponse = elasticsearchClient.indices()
                .delete(deleteIndexRequest ->
                        deleteIndexRequest.index(indexName)
                );

        logger.info("== {} 索引创建是否删除成功: {}", indexName, deleteIndexResponse.acknowledged());
    }
}
