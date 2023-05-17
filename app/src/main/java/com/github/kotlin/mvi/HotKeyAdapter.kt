package com.github.kotlin.mvi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.github.kotlin.R
import com.github.kotlin.databinding.ItemArticleBinding
import com.github.kotlin.databinding.ItemHotKeyBinding
import com.github.kotlin.mvi.data.HotKey
import com.github.kotlin.ui.adapter.BaseRecyclerAdapter
import com.github.kotlin.ui.adapter.BaseViewHolder

/**
 * HotKeyAdapter
 *
 * @author tiankang
 * @description: 多布局尝试
 * @date :2023/2/16 10:38
 */
class HotKeyAdapter(dataList: List<HotKey>) :
    BaseRecyclerAdapter<HotKey>(dataList) {
//    private val LAYOUT_KEY = 123
//    private val LAYOUT_ARTICLE = 456
    override fun onBindData(holder: BaseViewHolder, position: Int) {
//        val title = dataList.getOrNull(position)?.name ?: "null"
//        when (holder.binding) {
//            is ItemHotKeyBinding ->
//                holder.binding.title.text = title
//
//            is ItemArticleBinding ->
//                holder.binding.chapter.text = title
//        }

        val binding = holder.binding as ItemHotKeyBinding
        binding.title.text = dataList.getOrNull(position)?.name ?: "null"
    }

    override fun onGenerateLayout(parent: ViewGroup, viewType: Int): ViewBinding {

//        return when (viewType) {
//            LAYOUT_KEY -> ItemHotKeyBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//
//            else -> ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        }

        return ItemHotKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onCreateLayoutManager(context: Context): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

//    override fun getItemViewType(position: Int): Int {
//        return when (position) {
//            0 -> LAYOUT_KEY
//            else -> LAYOUT_ARTICLE
//        }
//    }


}