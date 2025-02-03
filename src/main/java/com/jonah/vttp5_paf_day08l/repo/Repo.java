package com.jonah.vttp5_paf_day08l.repo;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;

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
    

    public List<Document> groupCommentsByUserNested(String user){
        GroupOperation groupByUser = Aggregation.group("user")
        //this is to put 2 values into an object
        .push(
            new BasicDBObject().append("gid", "$gid").append("text","$c_text")).as("comments");
        LimitOperation takeTopThree = Aggregation.limit(3);

        Aggregation pipeline = Aggregation.newAggregation(groupByUser, takeTopThree);
        return template.aggregate(pipeline, "comment", Document.class).getMappedResults();


    }

    /* 
                db.comment.aggregate([
            {$lookup:{
                from: 'games',
                foreignField: "gid",
                localField: "gid",
                as: "GAME"
            }},
            {$unwind: "$GAME"},
                {$group:{
                    _id: "$user",
                    count:{$sum: 1},
                    game_comment_rating:{$push:{Game:"$GAME.name", comment: "$c_text",rating: "$rating"}},
                    game:{$push:"$GAME.name"},
                    comment:{$push:"$c_text"},
                    rating:{$push:"$rating"}
                }}

            ])
     */

    public List<Document> getUserComments(String user, long limit){
        MatchOperation matchUser = Aggregation.match(Criteria.where("user").is(user));
        LookupOperation lookupGames = Aggregation.lookup("games", "gid", "gid", "GAME");
        AggregationOperation unwindGames = Aggregation.unwind("GAME");
        
  /*       GroupOperation groupByName = Aggregation.group("user").push("GAME.name").as("Game")
        .push("c_text").as("comment").push("rating").as("rating"); */
        SortOperation sortByRating = Aggregation.sort(Direction.DESC, "rating");
        LimitOperation limiting = Aggregation.limit(limit);
        

        GroupOperation groupByName = Aggregation.group("user").push(
            new BasicDBObject().append("GameName", "$GAME.name").append("Comment", "$c_text").append("Rating", "$rating")
        ).as("Comments");

        Aggregation pipeline = Aggregation.newAggregation(matchUser,lookupGames,unwindGames,sortByRating,limiting,groupByName);

        AggregationResults<Document> results = template.aggregate(pipeline, "comment", Document.class);

        return results.getMappedResults();

    }
}
