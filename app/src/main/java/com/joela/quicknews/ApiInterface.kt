/*
API Interface for newsapi.org
defines functions along with query parameters, HTTP Request type and URL path to send request
 */

package com.joela.quicknews

import com.joela.quicknews.models.ResponseModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("v2/top-headlines")
    fun getData(@Query("country") country: String, @Query("category") category: String, @Query("apiKey") key: String): Call<ResponseModel>

    @GET("v2/everything")
    fun getSearch(@Query("q") query: String, @Query("apiKey") key: String): Call<ResponseModel>
}