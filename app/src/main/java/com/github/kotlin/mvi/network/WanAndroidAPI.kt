package com.github.kotlin.mvi.network

import com.github.kotlin.mvi.data.Article
import com.github.kotlin.mvi.data.HotKey
import com.github.kotlin.mvi.data.Pager
import com.github.kotlin.mvi.data.Response
import com.github.kotlin.network.https.ArticleTag
import com.github.kotlin.network.https.BasicUserInfo
import com.github.kotlin.network.https.Channel
import com.github.kotlin.network.https.Hotkey
import com.github.kotlin.network.https.HttpBean
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.Objects

/**
 * WanAndroidAPI
 *
 * @author tiankang
 * @description:
 * @date :2023/2/14 10:32
 */
interface WanAndroidAPI {

    @GET("hotkey/json")
    suspend fun hotKey(): Response<List<HotKey>>

    @GET("hotkey/json")
    suspend fun hotKey2(@QueryMap paramMap: Map<String, Any>): Response<List<HotKey>>

    @POST("article/query/{pageNum}/json")
    suspend fun searchArticles(
        @Path("pageNum") pageNum: Int,
        @Query("k") keyword: String
    ): Response<Pager<Article>>

    @Headers("Content-Type: application/json", "charset: UTF-8")
    @POST("app/queryTripData")
    suspend fun getArticle(@Body articleTag: ArticleTag): HttpBean<List<Article>>

    @Headers("Content-Type: application/json", "charset: UTF-8")
    @POST("app/queryTripData")
    suspend fun getArticle2(@Body paramMap: Map<String, Any>): HttpBean<List<Article>>

    @Headers("Content-Type: application/json", "charset: UTF-8")
    @POST("app/store/management/channel/tag")
    suspend fun getAppTag(@Body channel: Channel): HttpBean<String>

}