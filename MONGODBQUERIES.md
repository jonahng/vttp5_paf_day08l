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
    }}

])







db.series.aggregate([
{$bucket:{
    groupBy: "$rating.average",
    boundaries: [0,5,7,8],
    default: 'Greater than 9',
    output: {
        ratings:{$push: "$rating.average"}
    }
    
}},

])






db.series.aggregate([
{$project:{
    id:1,
    title: {$concat: ["$name"," (", "$language", ") "]},
    summary:1,
    genres_count: {$size: '$genres'}
}}
])






db.series.aggregate([
{$match: {"network.country.timezone":{$ne: "null"}}},
{
    $group:{
        _id: "$network.country.name",
        count: {$sum: 1},
        titles:{$push: "$name"}
    }
},
{$sort:{count : -1}}
]
)


