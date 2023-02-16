package com.github.kotlin.mvi.articles

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.github.kotlin.databinding.ItemArticleBinding
import com.github.kotlin.mvi.BindViewHolder
import com.github.kotlin.mvi.data.Article

/**
 * ArticleAdapter
 *
 * @author tiankang
 * @description:
 * @date :2023/2/16 15:02
 */
class ArticleAdapter : ListAdapter<Article, BindViewHolder>(ArticleItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder {
        return BindViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BindViewHolder, position: Int) {
        val binding = holder.binding as ItemArticleBinding
        binding.chapter.text = getItem(position).chapterName
        binding.title.text = Html.fromHtml(getItem(position).title)
    }
}

class ArticleItemCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Article, newItem: Article) =
        oldItem.hashCode() == newItem.hashCode()

}