package com.github.kotlin.data.entities

/**
 * ResultX
 *
 * @author tiankang
 * @description: 利用密封类和泛型，将数据的成功、失败、加载中都统一了起来
 * @date :2022/12/5 16:07
 */
sealed class ResultX<out R : Any> {

    data class Success<out T : Any>(val data: T) : ResultX<T>()
    data class Error(val exception: Exception) : ResultX<Nothing>()
    object Loading : ResultX<Nothing>()


    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}
