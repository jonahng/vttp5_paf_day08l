package com.jonah.vttp5_paf_day08l;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jonah.vttp5_paf_day08l.repo.Repo;

@Component
public class AggregateExample {
    @Autowired
    private Repo repo;



    @Override
    public void run(String... args){
        List<Document> results = repo.findGamesByName("Bootleggers");
        System.out.println("THE RESULTS ARE" + results);
    }
    
}
