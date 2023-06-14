package com.github.kotlin.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kotlin.adapter.RepoAdapter
import com.github.kotlin.databinding.ActivityGitHubBinding
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.viewmodel.GitHubViewModel

/**
 * MVVM+Clean架构的组合使得原先在VM里的数据处理逻辑转移到了usecase，这样VM的逻辑更加精简且清晰，并且由于clean架构的引入，数据层的代码将更加可测。但是，clean架构的缺点也很明显：代码臃肿、结构复杂。实际项目中很少会使用clean架构来实现一个业务模块。
 * 慢就是快，技术成长没有捷径
 * 虽然我们每个人的起点各不相同，但终点都是殊途同归的。我们唯一能做的，就是享受其中的过程，去追求来自内心的那份宁静和快乐。与其活在世俗的教条当中让自己难受，还不如跟随自己的内心，去做一些开心的事情。
 */
class GitHubActivity : AppCompatActivity() {

    private val viewModel: GitHubViewModel by viewModels()

    private lateinit var binding: ActivityGitHubBinding

    private lateinit var adapter: RepoAdapter

    private val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGitHubBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        viewModel.loadRepos()
        viewModel.getAppTag("J167")
        observeData()
    }

    private fun observeData() {
        viewModel.repos.observe(this) {
            display(it)
        }
        viewModel.repos3.observe(this) {
            Log.d("TAG", "tag observeData: $it")
        }
    }

    private fun display(repoList: RepoList) {
        adapter = RepoAdapter(repoList)
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter
    }
}