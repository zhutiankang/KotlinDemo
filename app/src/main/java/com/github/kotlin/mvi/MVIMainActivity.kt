package com.github.kotlin.mvi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.kotlin.R
import com.github.kotlin.databinding.ActivityMvimainBinding

class MVIMainActivity : AppCompatActivity(R.layout.activity_mvimain) {

    private val binding: ActivityMvimainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NavigationUI.setupWithNavController(
            binding.toolbar,
            findNavController(R.id.nav_host_fragment)
        )
    }
}