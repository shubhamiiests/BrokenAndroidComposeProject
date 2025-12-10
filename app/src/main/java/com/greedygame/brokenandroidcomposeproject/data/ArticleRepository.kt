package com.greedygame.brokenandroidcomposeproject.data

import com.google.gson.Gson
import com.greedygame.brokenandroidcomposeproject.db.AppDatabase
import com.greedygame.brokenandroidcomposeproject.network.ApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ArticleRepository(
    private val api: ApiService,
    database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ArticleRepositoryContract {

    private val articleDao = database.articleDao()

    // ---- interface property ----
    override val articles: Flow<List<Article>> = articleDao.getArticles()

    /**
     * Refreshes local cache.
     * 1. Tries network on IO dispatcher.
     * 2. If server returns empty list → falls back to bundled fake JSON.
     * 3. If network throws (no internet, etc.) → returns Result.failure(...)
     */
    override suspend fun refreshArticles(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val networkArticles = tryFetchFromNetwork()

            val finalList = if (networkArticles.isNotEmpty()) {
                networkArticles
            } else {
                parseFakeJson()
            }

            articleDao.clear()
            articleDao.insertAll(finalList)
            Result.success(Unit)
        } catch (e: Exception) {
            // UI (NewsViewModel) will decide how to show this error
            Result.failure(e)
        }
    }

    // ---- interface functions ----
    override suspend fun getArticle(id: Int): Article? = withContext(ioDispatcher) {
        articleDao.getArticleById(id)
    }

    override suspend fun updateArticle(article: Article) = withContext(ioDispatcher) {
        articleDao.update(article)
    }

    // ----------------- internal helpers -----------------

    /**
     * Network-only; let exceptions bubble up so refreshArticles()
     * can distinguish "no internet" vs success.
     */
    private suspend fun tryFetchFromNetwork(): List<Article> {
        val response = api.getArticles()
        val dtoList = response.articles.orEmpty()
        return dtoList.mapIndexed { index, dto ->
            Article(
                id = index + 1,              // backend doesn't give stable id
                title = dto.title ?: "Untitled",
                author = dto.author,
                content = dto.content,
                imageUrl = dto.imageUrl
            )
        }
    }

    /**
     * Parses the intentionally broken JSON but maps it correctly to [Article].
     */
    private fun parseFakeJson(): List<Article> {
        val fakeJson = """
            [
              {"identifier":1,"heading":"Hello","writer":"Alice"},
              {"identifier":2,"heading":"Compose + Room","writer":"Bob"}
            ]
        """.trimIndent()

        val gson = Gson()
        val dtoArray: Array<FakeArticleDto> = try {
            gson.fromJson(fakeJson, Array<FakeArticleDto>::class.java)
        } catch (e: Exception) {
            emptyArray()
        }

        return dtoArray.map { dto ->
            Article(
                id = dto.identifier,
                title = dto.heading,
                author = dto.writer,
                content = dto.content,
                imageUrl = dto.imageUrl
            )
        }
    }
}
