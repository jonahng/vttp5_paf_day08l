package com.jonah.vttp5_paf_day08l.repo;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
public class Repo {
    @Autowired
    private MongoTemplate template;



    /* 
     db.games.aggregate([
     {$match: {name:{$regex:name, $options:'i'}}},
     {$project:{name:1, ranking:1, image:1, _id:-1}},
     {$sort: {ranking:-1}},
     {$limit:3}
     ])
     */
    public List<Document> findGamesByName(String name){
        Criteria criteria = Criteria.where("name").regex(name, "i");

        MatchOperation matchName = Aggregation.match(criteria);

        ProjectionOperation projectFields = Aggregation.project("name", "ranking", "image").andExclude("_id");

        SortOperation sortByRanking = Aggregation.sort(Direction.DESC, "ranking");

        LimitOperation takeTopThree = Aggregation.limit(3);

        //The order is critical
        Aggregation pipeline = Aggregation.newAggregation(matchName,projectFields,sortByRanking,takeTopThree);

        AggregationResults<Document> results = template.aggregate(pipeline, "games", Document.class);
        return results.getMappedResults();
    }





    /* 
     
    db.games.aggregate([
    {$group:{_id:'$user', comments:{$push:{gid:'$gid',text:'$c_text'}}}}
    ])

     */
    public List<Document> groupCommentsByUser(String user){
        GroupOperation groupByUser = Aggregation.group("user")
        .push("c_text").as("comments");
        LimitOperation takeTopThree = Aggregation.limit(3);

        Aggregation pipeline = Aggregation.newAggregation(groupByUser, takeTopThree);
        return template.aggregate(pipeline, "comment", Document.class).getMappedResults();


    }



    //Example of unwind
    
}
