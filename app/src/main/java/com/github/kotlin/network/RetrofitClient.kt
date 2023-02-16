package com.github.kotlin.data

import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.mvi.network.WanAndroidAPI
import com.github.kotlin.network.moshi.NullStringAdapter
import com.github.kotlin.network.service.RepoService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient
 *
 * @author tiankang
 * @description:
 * @date :2022/12/5 16:33
 */
object RetrofitClient {

    private const val TAG = "OkHttp"
    private const val BASE_URL = "https://github.com/trending/"
    private const val BASE_URL_WAN = "https://www.wanandroid.com/"
    private const val TIME_OUT = 10

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(NullStringAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val service by lazy {
        getService(RepoService::class.java, BASE_URL)
    }

    val wanService by lazy {
        getService(WanAndroidAPI::class.java, BASE_URL_WAN)
    }

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
        builder.build()
    }

    private fun <S> getService(
        serviceClass: Class<S>,
        baseUrl: String,
        client: OkHttpClient = this.client
    ): S {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
            .build().create(serviceClass)
    }
}