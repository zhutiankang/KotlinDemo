package com.github.kotlin.mvi.articles

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.github.kotlin.mvi.data.Article
import com.github.kotlin.mvi.data.Pager
import com.github.kotlin.mvi.data.Response

/**
 * ArticleListState
 *
 * @author tiankang
 * @description:
 * @POST("article/query/{pageNum}/json")
suspend fun searchArticles(@Path("pageNum") pageNum: Int, @Query("k") keyword: String) : Response<Pager<Article>>
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
 * @date :2023/2/16 14:26
 */
data class ArticleListState(
    val articles: List<Article> = emptyList(),
    val request: Async<Response<Pager<Article>>> = Uninitialized,
    val keyword: String,
    val nextPage: Int = 0,
    val isRefresh: Boolean = false,
    val isLoadMore: Boolean = false,
    val isLoadMoreCompleted: Boolean = false,
) : MavericksState {
    constructor(args: Args) : this(keyword = args.keyword)
}
