package com.github.kotlin.network.https

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * FlowExt
 *
 * @author tiankang
 * @description:
 * @date :2023/6/13 9:57
 */

suspend fun <T> Flow<T>.next(bloc: suspend T.() -> Unit): Unit = catch { }.collect { bloc(it) }
