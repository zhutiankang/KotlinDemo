package com.github.kotlin.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kotlin.adapter.RepoAdapter
import com.github.kotlin.databinding.ActivityGitHubBinding
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.viewmodel.GitHubViewModel

class GitHubActivity : AppCompatActivity() {

    private val viewModel: GitHubViewModel by viewModels()

    private lateinit var binding: ActivityGitHubBinding

    private lateinit var adapter: RepoAdapter

    private val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGitHubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.loadRepos()
        observeData()
    }

    private fun observeData() {
        viewModel.repos.observe(this) {
            display(it)
        }
    }

    private fun display(repoList: RepoList) {
        adapter = RepoAdapter(repoList)
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter
    }
}