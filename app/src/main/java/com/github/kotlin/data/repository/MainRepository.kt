package com.github.kotlin.data.repository

import com.github.kotlin.data.source.remote.RemoteRepoDataSource
import com.github.kotlin.data.RepoDataSource
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX

/**
 * MainRepository
 *
 * @author tiankang
 * @description: 统筹缓存数据、远程数据
 * @date :2022/12/5 17:03
 */
class MainRepository(
    private val dataSource: RepoDataSource = RemoteRepoDataSource,
    private val localDataSource: RepoDataSource? = null
) : IRepository {
    override suspend fun getRepoList(): ResultX<RepoList> {
        // 暂不处理缓存逻辑
        return dataSource.getRepos()
    }
}