package com.github.kotlin.domain

import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.data.entities.ResultX
import com.github.kotlin.data.repository.IRepository
import com.github.kotlin.data.repository.MainRepository

/**
 * GetRepoListUseCase
 *
 * @author tiankang
 * @description: 领域层，其实就像是业务逻辑的一个小单元，这里的小单元，我们可以将其称为 UseCase UseCase 是可以承载复杂的业务逻辑的
 * @date :2022/12/5 17:07
 */
class GetRepoListUseCase(private val repository: IRepository = MainRepository()) {

    suspend operator fun invoke(): ResultX<RepoList> {
        return repository.getRepoList()
    }
}