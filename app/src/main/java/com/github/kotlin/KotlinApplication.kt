package com.github.kotlin

import android.app.Application
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.navigation.DefaultNavigationViewModelDelegateFactory
import dagger.hilt.android.HiltAndroidApp

/**
 * com.github.kotlin.KotlinApplication
 *
 * @author tiankang
 * @description:
 * @date :2023/2/10 14:32
 */
@HiltAndroidApp
class KotlinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(
            this,
            viewModelDelegateFactory = DefaultNavigationViewModelDelegateFactory()
        )
    }

}