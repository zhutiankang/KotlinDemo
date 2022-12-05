package com.github.kotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.kotlin.R
import com.github.kotlin.data.entities.RepoList

/**
 * RepoAdapter
 *
 * @author tiankang
 * @description:
 * @date :2022/12/5 17:38
 */
class RepoAdapter(private val repoList: RepoList) : RecyclerView.Adapter<RepoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoHolder {
        return RepoHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_repo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RepoHolder, position: Int) {
        holder.text.text = repoList.items.getOrNull(position)?.repo
    }

    override fun getItemCount(): Int = repoList.count
}

class RepoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.findViewById(R.id.text)
}