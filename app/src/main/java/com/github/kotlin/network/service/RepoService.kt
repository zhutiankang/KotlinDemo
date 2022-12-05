package com.github.kotlin.network.service

import com.github.kotlin.data.entities.RepoList
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * RepoService
 *
 * @author tiankang
 * @description:
 * @date :2022/12/5 18:31
 */
interface RepoService {
    @GET("repo")
    suspend fun repos(
        @Query("lang") lang: String = "Kotlin",
        @Query("since") since: String = "weekly"
    ): RepoList
}