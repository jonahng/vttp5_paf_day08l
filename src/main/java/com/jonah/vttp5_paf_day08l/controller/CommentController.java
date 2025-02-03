package com.jonah.vttp5_paf_day08l.controller;

import java.nio.channels.MulticastChannel;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jonah.vttp5_paf_day08l.services.CommentService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    CommentService commentService;


    @GetMapping("/{user}")
    public ResponseEntity<List<Document>> getUserComments(@PathVariable("user") String user, @RequestParam MultiValueMap<String,String> params){
        //example of how to deal with multiple request params and set a default value for each param.   
        String limit = params.getFirst("limit");

        if(limit == null){
            limit = "1";
        }
        List<Document> result = commentService.getUserComments(user, Long.parseLong(limit));
        System.out.println("THE RESULT FOR USER COMMENTS IS:" + result.toString());

        return ResponseEntity.ok().body(result);

        
    }
    
}
