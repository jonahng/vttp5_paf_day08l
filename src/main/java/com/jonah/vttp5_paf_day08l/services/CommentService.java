package com.jonah.vttp5_paf_day08l.services;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jonah.vttp5_paf_day08l.repo.Repo;

@Service
public class CommentService {
    @Autowired
    Repo repo;

    public List<Document> getUserComments(String user, long limit){
        return repo.getUserComments(user, limit);
    }
    
    
}
