package com.github.kotlin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kotlin.domain.GetRepoListUseCase
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX
import kotlinx.coroutines.launch

/**
 * GitHubViewModel
 *
 * @author tiankang
 * @description: 表现层
 * @date :2022/12/5 17:10
 */
class GitHubViewModel(
    val getRepoListUseCase: GetRepoListUseCase = GetRepoListUseCase()
) : ViewModel() {

    val repos: LiveData<RepoList>
        get() = _repos

    private val _repos = MutableLiveData<RepoList>()

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
}