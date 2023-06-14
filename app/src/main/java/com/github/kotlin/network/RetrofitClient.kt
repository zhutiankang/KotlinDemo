package com.github.kotlin.data

import android.text.TextUtils
import com.github.kotlin.mvi.network.WanAndroidAPI
import com.github.kotlin.network.moshi.NullStringAdapter
import com.github.kotlin.network.service.RepoService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
    private const val BASE_URL_YK = "https://tkui.smartlink.com.cn/tkui/api/faw/rest-businesss/"
    private const val TIME_OUT = 10
    private const val TYPE = "application/json; charset=utf-8"

    private const val KEY_TOKEN = "token"
    var token: String? = null
    var baseUrl: String = ""
    private val headersMap = mutableMapOf<String, Map<String, String>>()


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
    val ykService by lazy {
        getService(WanAndroidAPI::class.java, BASE_URL_YK)
    }

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
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

    class RequestInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            requestBuilder.header(KEY_TOKEN, token ?: "")
            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }


    fun addHeader(host: String, key: String, value: String): RetrofitClient {
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(key)) {
            return this
        }
        headersMap[host] = mapOf(key to value)
        return this
    }

    class RequestInterceptor2 : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            var host: Map<String, String>? = null
            if (baseUrl.contains(originalRequest.url.host)) {
                host = headersMap[originalRequest.url.host]
            }
            if (host != null) {
                for ((key, value) in host) {
                    requestBuilder.header(key, value)
                }
            }
            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }

    fun getRequestBody(json: String): RequestBody {
        return json.toRequestBody(TYPE.toMediaType())
//        return RequestBody.create(MediaType.parse(TYPE), json)
    }
}