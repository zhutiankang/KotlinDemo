package com.github.kotlin.mvi

import androidx.lifecycle.ViewModel
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.github.kotlin.data.RetrofitClient
import kotlinx.coroutines.Dispatchers

class MainViewModel(initState: MainState) : MavericksViewModel<MainState>(initState) {

    init {
        getHotKeys()
    }

    fun getHotKeys() = withState {
        if (it.request is Loading) return@withState
        suspend {
            RetrofitClient.wanService.hotKey()
        }.execute(Dispatchers.IO, retainValue = MainState::request) { state ->
            copy(request = state, hotKeys = state()?.data ?: emptyList())
        }
    }

}