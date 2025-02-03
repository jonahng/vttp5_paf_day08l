package com.jonah.vttp5_paf_day08l.repo;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.stereotype.Repository;

@Repository
public class ShowsRepo {
    //THIS IS FOR A DIFFERENT MONGO DATABASE

    @Autowired
    private MongoTemplate template;


    /* 
     db.series.aggregate([
     {$unwind: '$genres'},
     {$group:{_id : '$genres', count:{sum:1}}}
     ])
     */

    public List<Document> listSeriesByGenres(){
        UnwindOperation unwindGenres = Aggregation.unwind("genres");
        GroupOperation groupAndCountGenres = Aggregation.group("genres")
        .count().as("count");
        SortOperation sortGenres = Aggregation.sort(Direction.ASC, "_id");

        Aggregation pipeline = Aggregation.newAggregation(unwindGenres,groupAndCountGenres,sortGenres);
        
        return template.aggregate(pipeline, "series", Document.class).getMappedResults();

    }
    
}
