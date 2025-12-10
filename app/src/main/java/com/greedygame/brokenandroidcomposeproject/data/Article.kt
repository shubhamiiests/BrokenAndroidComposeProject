package com.greedygame.brokenandroidcomposeproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String?,
    val content: String?,
    val imageUrl: String?
)

data class FakeArticleDto(
    val identifier: Int,
    val heading: String,
    val writer: String?,
    val content: String? = null,
    val imageUrl: String? = null
)
