package com.jonah.vttp5_paf_day08l.repo;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
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
                {$match:{user:"deinstein"}},
            {$lookup:{
                from: 'games',
                foreignField: "gid",
                localField: "gid",
                as: "GAME"
            }},
            {$unwind: "$GAME"},
            {$sort:{rating:-1}},
            {$limit:3},
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



    /* 
     Workshop 8 part 1 get all reviews for a game
     
     db.games.aggregate([
{$match:{gid:3}},
{$lookup:{
    from: 'comment',
    foreignField: "gid",
    localField: "gid",
    as: 'Comments',
}},
{$project:{
    _id:1,
    name:1,
    year:1,
    ranking:1,
    users_rated:1,
    average_rating: {$avg: "$Comments.rating"},
    url:1,
    image:1,
    reviews: "$Comments.c_id"
}}

])

     
     */

     public List<Document> getAllReviewsForGameId(String gameId){
        MatchOperation matchUser = Aggregation.match(Criteria.where("gid").is(Integer.parseInt(gameId)));
        LookupOperation lookupComments = Aggregation.lookup("comment", "gid", "gid", "Comments");
        ProjectionOperation projectFields = Aggregation.project("_id","name","year","ranking","users_rated","url","image")
        .and(AccumulatorOperators.Avg.avgOf("$Comments.rating")).as("average_rating")
        .and("$Comments.c_id").as("reviews");

        Aggregation pipeline = Aggregation.newAggregation(matchUser,lookupComments,projectFields);
        AggregationResults<Document> results = template.aggregate(pipeline, "games",Document.class);

        System.out.println("GAME REVIEWS: " + results.getMappedResults());

        return results.getMappedResults();


     }






     /* 
    Worskhop 8 , get the highest rated review for each game

    db.games.aggregate([

{$lookup:{
    from: 'comment',
    foreignField: "gid",
    localField: "gid",
    as: 'Comments',
}},
{$unwind: "$Comments"},
{$sort: {"Comments.rating": -1}},
{$group: {
    _id: "$gid",
    highestComment:{$max: "$Comments.rating"},
    
    commentIds:{$push:"$Comments.c_id"},
    
}},
{$project:{
    gid:"$gid",
    highestCommentId: {$arrayElemAt:["$commentIds",0]},
}},
{$sort:{_id:1}},
{$lookup:{
    from: 'comment',
    foreignField: "c_id",
    localField: "highestCommentId",
    as: 'Comments',
}},


])
      */







      /* 
       Another shorter method for ws8

       db.comment.aggregate([
{$sort: {"rating": -1}},           JUST CHANGE THIS SORTING TO GET HIGHER OR LOWEST
{$group: {
    _id: "$gid",
    highestComment:{$max: "$rating"},
    commentIds:{$push:"$c_id"},
    
}},
{$project:{
    gid:"$gid",
    highestCommentId: {$arrayElemAt:["$commentIds",0]},
}},
{$sort:{_id:1}},

{$lookup:{
    from: 'comment',
    foreignField: "c_id",
    localField: "highestCommentId",
    as: 'Comments',
}},


])
       */


     public List<Document> getHighestLowestReview(Boolean highest){
        System.out.println("TRYING TO GET HIGHEST REVIEWS\n");
        SortOperation sortByRating = Aggregation.sort(Direction.DESC, "rating");

        if(!highest){
            sortByRating = Aggregation.sort(Direction.ASC, "rating");
        }

        GroupOperation groupByGameId = Aggregation.group("$gid").push("$c_id").as("commentIds").max("$rating").as("highestComment");
        ProjectionOperation projectGameAndComment = Aggregation.project().and("gid").as("gid").and(ArrayOperators.ArrayElemAt.arrayOf("$commentIds").elementAt(0) ).as("highestCommentId");
        SortOperation sortByGameId = Aggregation.sort(Direction.ASC, "_id");
        LimitOperation limitTotal = Aggregation.limit(1000000);
        LookupOperation lookupComment = Aggregation.lookup("comment", "highestCommentId", "c_id", "comments");
        Aggregation pipeline = Aggregation.newAggregation(sortByRating,groupByGameId,projectGameAndComment,sortByGameId,limitTotal, lookupComment);
        AggregationResults<Document> results = template.aggregate(pipeline, "comment",Document.class);
        System.out.println("THE RESULT FOR HIGHEST REVIEWS IS" + results.getMappedResults());
        return results.getMappedResults();


     }

}







/* 
                db.comment.aggregate([
                {$match:{user:"deinstein"}},
            {$lookup:{
                from: 'games',
                foreignField: "gid",
                localField: "gid",
                as: "GAME",
                pipeline:[
                {$sort:{user:1, rating:1}}       THIS ALLOWS SORTING IN 2 variables, username and rating.
                ]
            }},
            {$unwind: "$GAME"},
            {$sort:{rating:-1}},
            {$limit:3},
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