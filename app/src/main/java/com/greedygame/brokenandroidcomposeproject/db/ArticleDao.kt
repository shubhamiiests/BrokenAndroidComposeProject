package com.greedygame.brokenandroidcomposeproject.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.greedygame.brokenandroidcomposeproject.data.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY id DESC")
    fun getArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Int): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<Article>)

    @Update
    suspend fun update(article: Article)

    @Query("DELETE FROM articles")
    suspend fun clear()
}
