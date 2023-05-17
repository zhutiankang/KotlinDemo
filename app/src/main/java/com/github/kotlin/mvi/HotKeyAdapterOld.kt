package com.github.kotlin.mvi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.github.kotlin.databinding.ItemHotKeyBinding
import com.github.kotlin.mvi.data.HotKey

/**
 * HotKeyAdapter
 *
 * @author tiankang
 * @description:
 * @date :2023/2/16 10:38
 */
class BindViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

class HotKeyAdapterOld(private val itemClick: (String) -> Unit) :
    ListAdapter<HotKey, BindViewHolder>(HashItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder {
        return BindViewHolder(
            ItemHotKeyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BindViewHolder, position: Int) {
        val binding = holder.binding as ItemHotKeyBinding
        binding.title.text = getItem(position).name
        binding.root.setOnClickListener {
            itemClick.invoke(getItem(position).name)
        }
    }

}

class HashItemCallback : DiffUtil.ItemCallback<HotKey>() {
    override fun areItemsTheSame(oldItem: HotKey, newItem: HotKey) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: HotKey, newItem: HotKey) =
        oldItem.hashCode() == newItem.hashCode()

}