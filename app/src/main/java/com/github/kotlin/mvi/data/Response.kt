package com.github.kotlin.mvi.data

/**
 * Response
 *
 * @author tiankang
 * @description:
 * @date :2023/2/14 9:30
 */
data class Response<T>(
    val data: T,
    val errorCode: Int,
    val errorMsg: String
)

data class HotKey(
    val id: String,
    val link: String,
    val name: String,
    val order: String,
    val visible: String,
)

data class Pager<T>(
    val curPage: Int,
    val datas: List<T>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)

data class Article(
    val id: String,
    val title: String,
    val link: String,
    val chapterName: String
)