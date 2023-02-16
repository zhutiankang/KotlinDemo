package com.github.kotlin.mvi.network

import com.github.kotlin.mvi.data.Article
import com.github.kotlin.mvi.data.HotKey
import com.github.kotlin.mvi.data.Pager
import com.github.kotlin.mvi.data.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("article/query/{pageNum}/json")
    suspend fun searchArticles(@Path("pageNum") pageNum: Int, @Query("k") keyword: String) : Response<Pager<Article>>
}