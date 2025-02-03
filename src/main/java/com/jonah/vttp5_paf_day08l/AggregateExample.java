package com.jonah.vttp5_paf_day08l;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jonah.vttp5_paf_day08l.repo.Repo;
import com.jonah.vttp5_paf_day08l.repo.ShowsRepo;

@Component
public class AggregateExample implements CommandLineRunner{
    @Autowired
    private Repo repo;

    @Autowired
    private ShowsRepo showsRepo;



    @Override
    public void run(String... args){
        List<Document> results = repo.findGamesByName("Bootleggers");
        System.out.println("THE RESULTS ARE" + results);
    }
    
}
