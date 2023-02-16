package com.github.kotlin.mvi

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.github.kotlin.mvi.data.HotKey
import com.github.kotlin.mvi.data.Response

/**
 * MainState
 *
 * @author tiankang
 * @description:
 * @date :2023/2/16 9:53
 */
data class MainState(
    val hotKeys: List<HotKey> = emptyList(),
    val request: Async<Response<List<HotKey>>> = Uninitialized
) : MavericksState
