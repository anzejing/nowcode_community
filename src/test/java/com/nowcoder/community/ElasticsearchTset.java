package com.nowcoder.community;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostDocument;
import com.nowcoder.community.dao.elasticsearch.DiscussPostIndex;
import com.nowcoder.community.dao.elasticsearch.DiscussPostSearch;
import com.nowcoder.community.entity.DiscussPost;
import lombok.val;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource(value = "classpath:application.properties")
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTset {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private DiscussPostIndex discussPostIndex;
    @Autowired
    private DiscussPostDocument discussPostDocument;
    @Autowired
    private DiscussPostSearch discussPostSearch;

    @Test
    public void ElasticsearchClientBuild() {
        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost("127.0.0.1", 9200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);
        System.out.println("elasticsearchClient = " + elasticsearchClient);
    }


    @Test
    public void addIndex() throws IOException {
        //1.使用client获取操作索引对象
        val indices = elasticsearchClient.indices();
        //2.具体操作获取返回值
        //2.1 设置索引名称
//        CreateIndexRequest createIndexRequest=new CreateIndexRequest("user");
//        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest, RequestOptions.DEFAULT);
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest ->
                                                                createIndexRequest.index("post"));


        //3.根据返回值判断结果
        System.out.println(createIndexResponse.toString());
    }

    @Test
    public void testtt() throws IOException{
//        discussPostRepository.createIndex("elasticsearch-client");
//        discussPostRepository.createIndexWithMapping();
//        discussPostRepository.indexIsExist("elasticsearch-client");
        discussPostIndex.indexDetail("test");
//        discussPostRepository.deleteIndex("elasticsearch-client");

    }

    @Test
    public void testAddDocument() throws IOException {
        DiscussPost discussPost= discussPostMapper.selectDiscussPostById(241);
        discussPost.setTitle("中华人民共和国");
        discussPostDocument.AddDocument(discussPost);

    }

    @Test
    public void testGetDocument() throws IOException {
        DiscussPost discussPost= discussPostDocument.GetDocument(241);
        System.out.println(discussPost.toString());

    }

    @Test
    public void testBatchInsert() throws IOException {
        List<DiscussPost> postList=new ArrayList<>();
        postList.add(discussPostMapper.selectDiscussPostById(280));
        postList.add(discussPostMapper.selectDiscussPostById(277));
        postList.add(discussPostMapper.selectDiscussPostById(276));
        postList.add(discussPostMapper.selectDiscussPostById(274));
        discussPostDocument.BatchInsert(postList);


    }
    @Test
    public void testInsert() throws IOException {
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(112,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(131,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(132,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(133,0,100));
        discussPostDocument.BatchInsert(discussPostMapper.selectDiscussPosts(134,0,100));

    }

    @Test
    public void testsearch() throws IOException {
        String searchText = "中华人民共和国";

        SearchResponse<DiscussPost> response = elasticsearchClient.search(s -> s
                        .index("discusspost")
                        .query(q -> q
                                .match(t -> t
                                        .field("content")
                                        .field("title")
                                        .query(searchText)
                                )
                        ),
                DiscussPost.class
        );

        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            System.out.println("There are " + total.value() + " results");
        } else {
            System.out.println("There are more than " + total.value() + " results");
        }

        List<Hit<DiscussPost>> hits = response.hits().hits();
        for (Hit<DiscussPost> hit: hits) {
            DiscussPost post = hit.source();
            System.out.println("Found product " + post.toString() + ", score " + hit.score());
        }
    }

    @Test
    public void testRestClient() throws IOException {
        Page<DiscussPost> searchResult =  discussPostSearch.RestClient("中华人民共和国",0,50);
        System.out.println(searchResult);
        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(searchResult!=null){
            for (DiscussPost post:searchResult){
                System.out.println(post.toString());
            }
        }
    }


}
