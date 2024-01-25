package com.github.kotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import base.coroutine.EventManager
import com.github.kotlin.domain.GetRepoListUseCase
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX
import com.github.kotlin.data.source.remote.RemoteRepoDataSourceFlow
import com.github.kotlin.mvi.data.Article
import com.github.kotlin.mvi.network.WanAndroidAPI
import com.github.kotlin.network.https.ArticleTag
import com.github.kotlin.network.https.Channel
import com.github.kotlin.network.https.FlowMapper
import com.github.kotlin.network.https.exception.ApiException
import com.github.kotlin.network.https.next
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * GitHubViewModel
 *
 * @author tiankang
 * @description: 表现层 实体层 数据层 领域层
 * @date :2022/12/5 17:10
 */
class GitHubViewModel(
    val getRepoListUseCase: GetRepoListUseCase = GetRepoListUseCase()
) : ViewModel() {

    val repos: LiveData<RepoList>
        get() = _repos

    private val _repos = MutableLiveData<RepoList>()

    val repos2: LiveData<List<Article>>
        get() = _repos2

    private val _repos2 = MutableLiveData<List<Article>>()

    val repos3: LiveData<String>
        get() = _repos3

    private val _repos3 = MutableLiveData<String>()

    fun observeBluetooth(): Flow<String> = EventManager.getBluetoothFlow2()

    fun observeNetwork(): Flow<String> = EventManager.getNetworkFlow()

    // Backing property to avoid state updates from other classes
//    private val _uiState = MutableStateFlow(LatestNewsUiState.Success(emptyList()))
//    // The UI collects from this StateFlow to get its state updates
//    val uiState: StateFlow<LatestNewsUiState> = _uiState

    //viewModelScope.launch {} 来启动协程。经过上节课的学习，我们知道以这种方式启动的协程是不会发生泄漏的，其中的协程任务会随着 Activity 的销毁而取消。
    fun loadRepos() {
        viewModelScope.launch {
            when (val result = getRepoListUseCase()) {
                is ResultX.Success -> {
                    _repos.value = result.data
                }
                is ResultX.Error -> {
                    _repos.value = RepoList()
                }
                ResultX.Loading -> {
                    // 展示Loading
                }
            }
        }
    }

    fun getArticle(name:String, url:String) {
        viewModelScope.launch {
            RemoteRepoDataSourceFlow.getArticle(name, url).catch {
                //异常处理
            e ->
                val apiException = FlowMapper.transformException(e)
                Log.e("TAG", apiException.toString())
            }.next {
                _repos2.value = this
            }
        }
    }

    fun getArticle2(name:String, url:String) {
        viewModelScope.launch {
            RemoteRepoDataSourceFlow.requestFow { getArticle(ArticleTag(name, url)) }.catch {
                //异常处理
                    exception  ->
                val apiException = FlowMapper.transformException(exception)
                Log.e("TAG", apiException.toString())
            }.next {
                _repos2.value = this
            }
        }
    }

    fun getAppTag(name:String) {
        viewModelScope.launch {
            RemoteRepoDataSourceFlow.requestFow { getAppTag(Channel(name)) }.catch {
                //如果异常需要单独处理就调用的时候加catch
                    exception  ->
                //不需要转换 已经是apiException
//                val apiException = FlowMapper.transformException(exception)
                Log.e("TAG", exception.toString())
            }.next {
                //不需要单独处理异常就不调用 ，这时候出异常了走next方法的catch捕获
                _repos3.value = this
            }
        }
    }

}