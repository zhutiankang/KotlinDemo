package com.github.kotlin.data

import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX

/**
 * RepoDataSource
 *
 * @author tiankang
 * @description: 数据层
 * @date :2022/12/5 16:24
 */
// 数据源
interface RepoDataSource {
    suspend fun getRepos(): ResultX<RepoList>
}

