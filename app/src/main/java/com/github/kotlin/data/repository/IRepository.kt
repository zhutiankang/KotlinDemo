package com.github.kotlin.data.repository

import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX

/**
 * IRepository
 *
 * @author tiankang
 * @description:// 数据仓库
 * @date :2022/12/5 18:26
 */
interface IRepository {

    suspend fun getRepoList(): ResultX<RepoList>
}