package com.greedygame.brokenandroidcomposeproject.data

import kotlinx.coroutines.flow.Flow

interface ArticleRepositoryContract {
    val articles: Flow<List<Article>>

    suspend fun refreshArticles(): Result<Unit>

    suspend fun getArticle(id: Int): Article?

    suspend fun updateArticle(article: Article)
}