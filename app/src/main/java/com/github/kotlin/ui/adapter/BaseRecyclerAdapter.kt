package com.github.kotlin.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding

/**
 * BaseRecyclerAdapter
 *
 * @author tiankang
 * @description:
 * @date :2023/5/10 16:38
 */

abstract class BaseRecyclerAdapter<T>(val dataList: List<T>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    private lateinit var itemClick: (Int) -> Unit
    private lateinit var itemLongClick: (Int) -> Unit


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = onGenerateLayout(parent, viewType)
        val holder = BaseViewHolder(binding)
        holder.itemView.setOnClickListener {
            itemClick.invoke(holder.bindingAdapterPosition)
        }
        holder.itemView.setOnLongClickListener {
            itemLongClick.invoke(holder.bindingAdapterPosition)
            return@setOnLongClickListener true
        }
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        onBindData(holder, position)
    }

    fun setClickListener(itemClick: (Int) -> Unit){
        this.itemClick = itemClick
    }

    fun setLongClickListener(itemLongClick: (Int) -> Unit){
        this.itemLongClick = itemLongClick
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    abstract fun onBindData(holder: BaseViewHolder, position: Int)
    abstract fun onGenerateLayout(parent: ViewGroup, viewType: Int): ViewBinding
    abstract fun onCreateLayoutManager(context: Context): RecyclerView.LayoutManager
}


class BaseViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)