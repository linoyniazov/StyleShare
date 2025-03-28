package com.example.styleshare.model

import com.example.styleshare.model.entities.Post

data class CategoryWithPosts(
    val name: String,
    val posts: List<Post>
)