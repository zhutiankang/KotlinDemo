package com.github.kotlin.mvi

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.PersistState

/**
 * NavViewModel
 *
 * @author tiankang
 * @description:
 * @date :2023/2/16 9:48
 */
data class NavState(@PersistState val count: Int = 0) : MavericksState
class NavViewModel(initState: NavState) : MavericksViewModel<NavState>(initState) {
    fun incCount() {
        setState {
            copy(count = count.plus(1))
        }
    }
}