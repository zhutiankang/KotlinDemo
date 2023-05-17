package com.github.kotlin.mvi

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import by.kirich1409.viewbindingdelegate.viewBinding
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.navigation.navGraphViewModel
import com.airbnb.mvrx.withState
import com.github.kotlin.R
import com.github.kotlin.databinding.FragmentMainBinding
import com.github.kotlin.mvi.articles.Args
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable
import kotlin.random.Random

class MainFragmentOld : Fragment(R.layout.fragment_main), MavericksView {

    private val mainViewModel: MainViewModel by fragmentViewModel()

    private val binding: FragmentMainBinding by viewBinding()

    private val navViewMode: NavViewModel by navGraphViewModel(R.id.mavericks_nav)

    private val adapter: HotKeyAdapterOld by lazy {
        //参数是函数，高阶函数，可以lambda表达
        HotKeyAdapterOld {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                navViewMode.incCount()
                navigate(R.id.action_mainFragment_to_articleListFragment, Args(it))
            }
        }
    }

    private fun navigate(@IdRes id: Int, args: Serializable? = null) {
        findNavController().navigate(id, Bundle().apply {
            putSerializable(Mavericks.KEY_ARG, args)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.onAsync(
            MainState::request,
            deliveryMode = uniqueOnly(),
            onFail = {
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    Snackbar.make(
                        binding.root,
                        "HotKey request failed.",
                        Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        setAction("DISMISS") {
                            this.dismiss()
                        }
                        show()
                    }
                }
            },
            onSuccess = {
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    Snackbar.make(
                        binding.root,
                        "HotKey request successfully.",
                        Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        setAction("DISMISS") {
                            this.dismiss()
                        }
                        show()
                    }
                }
            }

        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.list.adapter = adapter
        binding.list.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        //匿名函数 lambda表达
        binding.refresh.setOnRefreshListener {
            mainViewModel.getHotKeys()
        }
    }

    override fun invalidate() {
        withState(mainViewModel) {
            binding.refresh.isRefreshing = !it.request.complete
            adapter.submitList(if (Random.nextBoolean()) it.hotKeys.reversed() else it.hotKeys)
        }
    }


}