package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.Article
import com.example.myapplication.models.NewsResponse
import com.example.myapplication.repository.NewsRepository
import com.example.myapplication.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headLinesPage = 1
    var headLinesResponse: NewsResponse? = null
    var currentCountry = "us"
    var currentCategory: String? = null
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newsSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadLines(currentCountry, currentCategory)
    }

    fun getHeadLines(countryCode: String, category: String? = null) = viewModelScope.launch {
        headlinesInternet(countryCode, category)
    }

    val categoryNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var categoryPage = 1
    var categoryResponse: NewsResponse? = null
    fun getCategoryNews(category: String) = viewModelScope.launch {
        categoryResponse = null
        categoryPage = 1
        categoryNews.postValue(Resource.Loading())

        try {
            if (internetConnection(getApplication())) {
                val response = newsRepository.getHeadLines(currentCountry, category, categoryPage)
                categoryNews.postValue(handleCategoryResponse(response))
            }
        } catch (t: Throwable) {
            categoryNews.postValue(Resource.Error(t.message ?: "Error"))
        }
    }

    private fun handleCategoryResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponce ->
                if (categoryResponse == null) {
                    categoryResponse = resultResponce
                } else {
                    val oldArticle = categoryResponse?.articles
                    val newArticle = resultResponce.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(categoryResponse ?: resultResponce)
            }
        }
        return Resource.Error(response.message())
    }


    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headLinesPage++
                if (headLinesResponse == null) {
                    headLinesResponse = resultResponse
                } else {
                    val oldArticles = headLinesResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(headLinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourite(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteArticles()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun headlinesInternet(countryCode: String, category: String?) {
        if (countryCode != currentCountry || category != currentCategory) {
            currentCountry = countryCode
            currentCategory = category
            headLinesPage = 1
            headLinesResponse = null
        }

        headlines.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response =
                    newsRepository.getHeadLines(currentCountry, currentCategory, headLinesPage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resource.Error("Network Failure"))
                else -> headlines.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication())) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable) {
            searchNews.postValue(Resource.Error("Search Error: ${t.message}"))
        }
    }


    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }
}