package com.github.kotlin.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kotlin.adapter.RepoAdapter
import com.github.kotlin.databinding.ActivityGitHubBinding
import com.github.kotlin.data.entities.RepoList
import com.github.kotlin.viewmodel.GitHubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

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

        viewModel.observeBluetooth().onEach {
            Log.d("TAG","observeBluetooth: $it")
//            binding.tvBondContent.text = it
        }.launchIn(lifecycleScope)


        viewModel.observeNetwork().onEach {
            Log.d("TAG","observeNetwork: $it")
//            binding.tvWifiContent.text = it
        }.launchIn(lifecycleScope)
    }

    private fun display(repoList: RepoList) {
        adapter = RepoAdapter(repoList)
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter
    }



    /**
     * flow 定时任务
     */

    private var mCountdownJob: Job? = null
    private var endTime: Long = 0	//秒杀截止时间

    fun countDown() {
        //什么是协程作用域呢? 类似于生命周期, 当页面销毁时, 协程应当随之停止 , 这样可以避免内存泄漏
        // lifecycleScope: 默认主线程. 它会监听Activity生命周期, 不需要手动 cancel()
        //withContext(): 不新建协程, 它只指定 执行当前代码块 所需的线程;
        //async: 创建一个协程; 它返回一个Deferred, 而lanuch返回Job对象. 多个async可以支持并发, await()是等待任务执行完毕,并返回结果.
        mCountdownJob?.cancel()
        mCountdownJob = lifecycleScope.launchWhenResumed {////onPause 的时候会暂停. onResume的时候会恢复
            var count = 10
            while (count > 0) {
//                binding.tvCountdown.text = "倒计时：$count"
                count--
                delay(1000)
            }
//            binding.tvCountdown.text = "倒计时：0"

            (1..10).forEach {
                delay(1000)
                println("countDownTimer3 $it")
            }

            repeat(1000) {
//                _repos3.value = "倒计时: $it"
                Log.d("TAG", "tag countDown: $it")
                delay(1000)
            }
            repeat(Int.MAX_VALUE){
                delay(1000L)
//                binding.tvTitle.text = "变变变, 我是百变小魔女$it"

                //假设我们的任务比较繁重, 每次需要消耗300ms 显而易见, 此时至少要 1300ms 才能循环一次.
                // 所以上面写的1000ms只能算是间隔时间. 而任务执行需要时间, 协程挂起,恢复需要时间, 任务进入队列到线程执行也需要时间.
                //当定时任务的精确度要求不高, 每次执行的代码任务比较轻便. 耗时较少时, 可以用这种方式.
                delay(300L)
//                binding.tvTitle.text = "变变变, 我是百变小魔女$it"
            }

            //有的时候, 我们锁屏了, 或者跳到别的页面了, 我们不需要定时一直执行, 即便更新了UI, 我们也看不到啊! 所以,我们希望页面离开时, 定时取消. 页面显示时,重新启动定时即可:
            //我们就以秒杀倒计时为例;
            repeat(Int.MAX_VALUE){
                val now = System.currentTimeMillis()
//                binding.tvTitle.text = if(now >= endTime){
//                    "秒杀结束"
//                }else{
//                    val second = (endTime - now) / 1000 + 1
//                    "秒杀倒计时($second); it=$it"
//                }
                delay(1000L)
            }
        // 有时, 我们需要精确的定时器. 可以用, java.util 包下的 Timer, 如下所示; 也可以用 CountDownTimer
        }

        mCountdownJob = countDownCoroutines(60, lifecycleScope,
            onTick = { second ->
//                mBinding.text.text = "${second}s后重发"
            }, onStart = {
                // 倒计时开始
            }, onFinish = {
                // 倒计时结束，重置状态
//                mBinding.text.text = "发送验证码"
            })

        // asFlow包含emit了 相当于flow的数值封装
        (1..10).asFlow().onEach {
            delay(1000)
            println("countDownTimer2 $it")
        }.launchIn(lifecycleScope)

    }

    fun countDownCoroutines(
        total: Int,
        scope: CoroutineScope,
        onTick: (Int) -> Unit,
        onStart: (() -> Unit)? = null,
        onFinish: (() -> Unit)? = null,
    ): Job {
        return flow {
            for (i in total downTo 0) {
                emit(i)
                delay(1000)
            }
            var total2 = total
            while (total2 >= 0) {
                emit(total2--)
                delay(1000)
            }
        }.flowOn(Dispatchers.Main)
            .onStart { onStart?.invoke() }
            .onCompletion { onFinish?.invoke() }
            .onEach { onTick.invoke(it) }
            .launchIn(scope)
    }

}