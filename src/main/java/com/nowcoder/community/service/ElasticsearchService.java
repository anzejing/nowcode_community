package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostDocument;
import com.nowcoder.community.dao.elasticsearch.DiscussPostSearch;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostDocument postDocument;
    @Autowired
    private DiscussPostSearch postSearch;

    public void saveDiscussPost(DiscussPost post) throws IOException {
        postDocument.AddDocument(post);
    }

    public void deleteDiscussPost(int id) throws IOException {
        postDocument.DeleteDocument(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int pageSize) throws IOException {

        return postSearch.RestClient(keyword,current,pageSize);


    }



}
