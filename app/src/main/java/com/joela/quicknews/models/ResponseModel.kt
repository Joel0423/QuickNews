/*
Models the API response sent from newsapi.org
 */

package com.joela.quicknews.models

data class ResponseModel(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)