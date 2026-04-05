package com.example.myapplication.api

import com.example.myapplication.models.NewsResponse
import com.example.myapplication.util.Constants.Companion.API_KEY
import okhttp3.Response
import retrofit2.http.GET
import java.util.Locale
import retrofit2.http.Query
import retrofit2.http.QueryName

interface NewsAPI
{
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        countryCode: String = "us",
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): retrofit2.Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY



    ): retrofit2.Response<NewsResponse>

}