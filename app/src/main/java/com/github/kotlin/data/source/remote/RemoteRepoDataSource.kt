package com.github.kotlin.data.source.remote

import android.util.Log
import com.github.kotlin.data.RepoDataSource
import com.github.kotlin.data.RetrofitClient
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RemoteRepoDataSource
 *
 * @author tiankang
 * @description: 用 withContext{} 将 API 请求分发到了 IO 线程池，然后根据实际的运行结果，将数据封装成对应的 ResultX 的子类型
 * @date :2022/12/5 16:58
 */
object RemoteRepoDataSource : RepoDataSource {
    const val TAG = "RemoteRepoDataSource"
    override suspend fun getRepos(): ResultX<RepoList> =
        withContext(Dispatchers.IO) {
            try {
                ResultX.Success(RetrofitClient.service.repos())
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                ResultX.Error(e)
            }
        }

}