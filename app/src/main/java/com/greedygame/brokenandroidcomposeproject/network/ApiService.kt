package com.greedygame.brokenandroidcomposeproject.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class NewsResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("totalResults") val totalResults: Int?,
    @SerializedName("articles") val articles: List<NewsArticleDto>?
)

data class NewsArticleDto(
    @SerializedName("title") val title: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("urlToImage") val imageUrl: String?
)

interface ApiService {
    @GET("/v2/everything?q=android&apiKey=7ee16f7ef86f411f88ed88b65462cdd2")
    suspend fun getArticles(): NewsResponse
}

object ApiClient {
    val api: ApiService = Retrofit.Builder()
        .baseUrl("https://newsapi.org")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
