package com.github.kotlin.data.source.remote

import android.util.Log
import com.github.kotlin.data.RepoDataSource
import com.github.kotlin.data.RetrofitClient
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX
import com.github.kotlin.mvi.data.Article
import com.github.kotlin.mvi.network.WanAndroidAPI
import com.github.kotlin.network.https.ArticleTag
import com.github.kotlin.network.https.FlowMapper
import com.github.kotlin.network.https.HttpBean
import com.github.kotlin.network.https.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext

/**
 * RemoteRepoDataSource
 *
 * @author tiankang
 * @description:  catch只捕获一次异常 后面的方法就不会调用了 onComplete方法里面也能获取到异常
 * @date :2022/12/5 16:58
 * .catch { e ->
Log.e(TAG, e.message, e)
throw FlowMapper.transformException(e)
}
 */
object RemoteRepoDataSourceFlow {
    const val TAG = "RemoteRepoDataSourceFlow"
    suspend fun getArticle(name: String, url: String): Flow<List<Article>> = flow {
        val article = RetrofitClient.wanService.getArticle(ArticleTag(name, url))
        val result = FlowMapper.transformResult(article)
        if (result is List<*>) {
            emit(result as List<Article>)
        } else {
            throw result as ApiException
        }
    }.flowOn(Dispatchers.IO)
        .onCompletion {
            Log.e(TAG, "getArticle onCompletion")
            it?.let {
                Log.e(TAG, it.message, it)
                throw FlowMapper.transformException(it)
            }
        }

    //flowof
    suspend fun getArticle2(name: String, url: String): Flow<HttpBean<List<Article>>> = flowOf(RetrofitClient.wanService.getArticle(ArticleTag(name, url))) .flowOn(Dispatchers.IO)
        .onCompletion {
            Log.e(TAG, "getArticle onCompletion")
            it?.let {
                Log.e(TAG, it.message, it)
                throw FlowMapper.transformException(it)
            }
        }

    //捕获异常的问题，上面封装再调用的时候是每次都要加catch方法，如果不加则出现错误就会崩溃，比如IO异常，Api异常等
    suspend fun <T> requestFow(
        showLoading: Boolean = true,
        request: suspend WanAndroidAPI.() -> HttpBean<T>
    ): Flow<T> {
        //        if (showLoading) {
//            showLoading()
//        }
        return flow {
            val response = request(RetrofitClient.ykService)
            val result = FlowMapper.transformResult(response)
            if (result is ApiException) {
                throw result
            } else {
                emit(result as T)
            }
        }.flowOn(Dispatchers.IO)
            .onCompletion {
//                closeLoading()
                Log.e(TAG, "onCompletion")
                // 异常的默认统一处理在onComplete方法里面
                it?.let {
                    Log.e(TAG, it.message, it)
                    // // throw it //可以对异常二次加工 然后抛出去
                    if (it is ApiException) {
                        throw it
                    } else {
                        throw FlowMapper.transformException(it)
                    }
                }
            }
    }



//    suspend fun <T> requestFow(
//        showLoading: Boolean = true,
//        request: suspend ApiInterface.() -> BaseResponse<T>?
//    ): Flow<BaseResponse<T>> {
//        if (showLoading) {
//            showLoading()
//        }
//        return flow {
//            val response = request(Api) ?: throw IllegalArgumentException("数据非法，获取响应数据为空")
//            if (response.errorCode != 0) {
//                throw  ApiException(response.errorCode, response.errorMsg ?: "")
//            }
//            emit(response)
//        }.flowOn(Dispatchers.IO)
//            .onCompletion { cause ->
//                run {
//                    closeLoading()
//                    Log.e("requestFow", "==onCompletion==cause==>${cause}")
//                    cause?.let {
//                        toast(it.message?:"")
//                        // throw it //可以对异常二次加工 然后抛出去
//                    }
//                }
//            }
//    }

}