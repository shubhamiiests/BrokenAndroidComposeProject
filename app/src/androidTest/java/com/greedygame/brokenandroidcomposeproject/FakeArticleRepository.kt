package com.greedygame.brokenandroidcomposeproject

import com.greedygame.brokenandroidcomposeproject.data.Article
import com.greedygame.brokenandroidcomposeproject.data.ArticleRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeArticleRepository : ArticleRepositoryContract {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    override val articles: Flow<List<Article>> = _articles

    var shouldFailRefresh = false

    override suspend fun refreshArticles(): Result<Unit> {
        return if (shouldFailRefresh) {
            Result.failure(Exception("Fake error"))
        } else {
            _articles.value = listOf(
                Article(1, "Test Title", "Tester", "Some content", null)
            )
            Result.success(Unit)
        }
    }

    override suspend fun getArticle(id: Int): Article? =
        _articles.value.firstOrNull { it.id == id }

    override suspend fun updateArticle(article: Article) {
        _articles.value = _articles.value.map {
            if (it.id == article.id) article else it
        }
    }
}
