package base.coroutine

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

/**
 * FourCycleLife
 *
 * @author tiankang
 * @description: Job 协程的句柄 协程生命周期、结构化并发 Job 和协程的关系，就有点像“遥控器和空调的关系” 1. 空调遥控器可以监测空调的运行状态；Job 也可以监测协程的运行状态；2.空调遥控器可以操控空调的运行状态，Job 也可以简单操控协程的运行状态。
 * Job作用：1.使用 Job 监测协程的生命周期状态 2.使用 Job 操控协程。
 * “结构化并发”就是：带有结构和层级的并发。  结构化并发带来的最大优势就在于，我们可以实现只控制“父协程”，从而达到控制一堆子协程的目的
 * 协程是有生命周期的，同时也发现，协程其实是结构化的
 * 我们还可以通过 Job.invokeOnCompletion {} 来监听协程执行完毕的事件；通过 Job.join() 这个挂起函数，我们可以挂起当前协程的执行流程，等到协程执行完毕以后，再恢复执行后面的代码。
 * @date :2022/12/12 17:37
 */

/**
 * 学习心得：
 * 第一，横向对比。在初次学习 Kotlin 协程失败以后，我去粗略学习了其他语言的协程，在那个时候，C# 之类的协程学习资源更加丰富。通过对比 C#、Go 等语言的协程后，我理解了“广义协程”的概念，并且也知道 yield、async、await 只是一种广泛存在的协程模式。而当我理解了广义协程这个旧的知识之后，我突然发现 Kotlin 的协程就不难理解了。
 * 第二，建立思维模型。这是我的一个“习惯”，不管是计算机网络、操作系统、数据结构、设计模式，还是其他领域，比如说高中的电磁学、大学的线性代数，在学习抽象知识的时候，我都喜欢虚构一些思维模型，来模拟它们内部的运行机制。协程，就是一门非常抽象的技术，我喜欢用协程 API 编写一些简单的 Demo 来分析它们的行为模式，同时为其建立思维模型。这样一来，我脑海里的知识既不容易遗忘，也更成体系。比如，launch 就像射箭；async 就像钓鱼，这既是思维模型，同时也是在用旧知识学新知识。
 * 第三，纵向深入。当我通过建立思维模型，对协程有了全面认识之后，我开始深入研究协程的源码。这时候，我从源码当中找到了更多的细节，来完善、支撑我脑子里的思维模型，从此，它们就不再是我凭空编造出来的东西了，因为证据都在源码里。这是一个自顶向下、逐渐深入的过程，反之则行不通。
 * 所以，现在回过头来看，其实最重要的，还是用旧知识学新知识。希望我的方法能对你有所启发。
 */
// launch、async 的时候，我们知道它们两个返回值类型分别是 Job 和 Deferred 不管是 launch 还是 async，它们本质上都会返回一个 Job 对象
public interface Deferred<out T> : Job {
    public suspend fun await(): T
}
//使用 Job 监测协程的生命周期状态；使用 Job 操控协程。

//job.log()，其实就是在监测协程；job.cancel()，其实就是在操控协程。
fun main333() = runBlocking {
    val job = launch {
        delay(1000L)
    }
    job.log()       // ①
    job.cancel()    // ②
    job.log()       // ③
    delay(1500L)
}

/**
 * 打印Job的状态信息
 */
fun Job.log() {
    logX("""
        isActive = $isActive
        isCancelled = $isCancelled
        isCompleted = $isCompleted
    """.trimIndent())
}

/**
 * 控制台输出带协程信息的log
 */
fun logX(any: Any?) {
    println("""
================================
$any
Thread:${Thread.currentThread().name}
================================""".trimIndent())
}


/*
输出结果：
================================
isActive = true
isCancelled = false
isCompleted = false
Thread:main @coroutine#1
================================
================================
isActive = false
isCancelled = true
isCompleted = false
Thread:main @coroutine#1
================================
*/

//===========job.start() 来启动协程任务，一般来说，它都是搭配“CoroutineStart.LAZY”来使用 懒加载行为模式 被 launch 以后，并不会立即执行
//外部调用了 job.start() 以后，job 的状态才变成了 Active 活跃。而当调用了 cancel 以后，job 的状态才变成 isCancelled、isCompleted。
//如果 Job 是以懒加载的方式创建的，那么它的初始状态将会是 New；而如果一个协程是以非懒加载的方式创建的，那么它的初始状态就会是 Active。
fun main334() = runBlocking {
    //                  变化在这里
    //                      ↓
    val job = launch(start = CoroutineStart.LAZY) {
        logX("Coroutine start!")
        delay(1000L)
    }
    delay(500L)
    job.log()
    job.start()     // 变化在这里
    job.log()
    delay(500L)
    job.cancel()
    delay(500L)
    job.log()
    delay(2000L)
    logX("Process end!")
}

/*
输出结果：
================================
isActive = false
isCancelled = false
isCompleted = false
Thread:main @coroutine#1
================================
================================
isActive = true
isCancelled = false
isCompleted = false
Thread:main @coroutine#1
================================
================================
Coroutine start!
Thread:main @coroutine#2
================================
================================
isActive = false
isCancelled = true
isCompleted = true
Thread:main @coroutine#1
================================
================================
Process end!
Thread:main @coroutine#1
================================
*/
//因为大部分情况下，我们很难从外部判断协程需要多长的时间才能结束（比如网络请求任务、下载任务）。
//为了更加灵活地等待和监听协程的结束事件，我们可以用 job.join() 以及 invokeOnCompletion {} 来优化上面的代码

// 代码段6

fun main335() = runBlocking {
    suspend fun download() {
        // 模拟下载任务
        val time = (Random.nextDouble() * 1000).toLong()
        logX("Delay time: = $time")
        delay(time)
    }
    val job = launch(start = CoroutineStart.LAZY) {
        logX("Coroutine start!")
        download()
        logX("Coroutine end!")
    }
    delay(500L)
    job.log()
    job.start()
    job.log()
    job.invokeOnCompletion {
        job.log() // 协程结束以后就会调用这里的代码
    }
    job.join()      // 等待协程执行完毕
    logX("Process end!")
}
//invokeOnCompletion {} 的作用，其实就是监听协程结束的事件。需要注意的是，它和前面的 isCompleted 类似，如果 job 被取消了，invokeOnCompletion {} 这个回调仍然会被调用

//job.join() 其实是一个“挂起函数”，它的作用就是：挂起当前的程序执行流程，等待 job 当中的协程任务执行完毕，然后再恢复当前的程序执行流程
/*
运行结果：
================================
isActive = false
isCancelled = false
isCompleted = false
Thread:main @coroutine#1
================================
================================
isActive = true
isCancelled = false
isCompleted = false
Thread:main @coroutine#1
================================
================================
Coroutine start!
Thread:main @coroutine#2
================================
================================
Delay time: = 252
Thread:main @coroutine#2
================================
================================
Coroutine end!
Thread:main @coroutine#2
================================
================================
isActive = false
isCancelled = false
isCompleted = true
Thread:main @coroutine#2
================================
================================
Process end!
Thread:main @coroutine#1
================================
*/


// 代码段8 Deferred 只是比 Job 多了一个 await() 挂起函数而已，通过这个挂起函数，我们可以等待协程执行完毕的同时，还可以直接拿到协程的执行结果。
// deferred.await() 这个方法，不仅可以帮助我们获取协程的执行结果，它还会阻塞当前协程的执行流程【看起来阻塞】，直到协程任务执行完毕。在这一点的行为上，await() 和 join() 是类似的。
// 如果当前的 Deferred 任务还没执行完毕，那么，await() 就会挂起当前的协程执行流程，等待 Deferred 任务执行完毕，再恢复执行后面剩下的代码。
// 看到这里，也许你会觉得奇怪，挂起函数不是非阻塞的吗？怎么这里又出现了阻塞？注意，这里其实只是看起来像是阻塞了，但它实际上是将剩下的代码存了起来，留在后面才执行了。
// 虽然看起来是阻塞了，但它只是执行流程被挂起和恢复的一种表现
fun main336() = runBlocking {
    val deferred = async {
        logX("Coroutine start!")
        delay(1000L)
        logX("Coroutine end!")
        "Coroutine result!"
    }
    val result = deferred.await()
    println("Result = $result")
    logX("Process end!")
}

/*
输出结果：
================================
Coroutine start!
Thread:main @coroutine#2
================================
================================
Coroutine end!
Thread:main @coroutine#2
================================
Result = Coroutine result!
================================
Process end!
Thread:main @coroutine#1
================================
*/
//====================================结构化并发  “结构化并发”就是：带有结构和层级的并发。===================================
//使用 launch 创建出来的协程，是存在父子关系的
//协程不像我们之前学过的线程，线程之间是不存在父子关系的，但协程之间是会存在父子关系的
fun main337() = runBlocking {
    val parentJob: Job
    var job1: Job? = null
    var job2: Job? = null
    var job3: Job? = null

    parentJob = launch {
        job1 = launch {
            delay(1000L)
        }

        job2 = launch {
            delay(3000L)
        }

        job3 = launch {
            delay(5000L)
        }
    }

    delay(500L)

    parentJob.children.forEachIndexed { index, job ->
        when (index) {
            0 -> println("job1 === job is ${job1 === job}")
            1 -> println("job2 === job is ${job2 === job}")
            2 -> println("job3 === job is ${job3 === job}")
        }
    }

    parentJob.join() // 这里会挂起大约5秒钟
    logX("Process end!")
}

/* 调用的是 parentJob 的 join() 方法，但是，它会等待其内部的 job1、job2、job3 全部执行完毕，才会恢复执行
。换句话说，只有当 job1、job2、job3 全部执行完毕，parentJob 才算是执行完毕了
输出结果：
job1 === job is true
job2 === job is true
job3 === job is true
// 等待大约5秒钟
================================
Process end!
Thread:main @coroutine#1
================================
*/


// 代码段12

fun main338() = runBlocking {
    val parentJob: Job
    var job1: Job? = null
    var job2: Job? = null
    var job3: Job? = null

    parentJob = launch {
        job1 = launch {
            logX("Job1 start!")
            delay(1000L)
            logX("Job1 done!") // ①，不会执行
        }

        job2 = launch {
            logX("Job2 start!")
            delay(3000L)
            logX("Job2 done!") // ②，不会执行
        }

        job3 = launch {
            logX("Job3 start!")
            delay(5000L)
            logX("Job3 done!")// ③，不会执行
        }
    }

    delay(500L)

    parentJob.children.forEachIndexed { index, job ->
        when (index) {
            0 -> println("job1 === job is ${job1 === job}")
            1 -> println("job2 === job is ${job2 === job}")
            2 -> println("job3 === job is ${job3 === job}")
        }
    }

    parentJob.cancel() // 变化在这里
    logX("Process end!")
}

/*将“parentJob.join”改为了“parentJob.cancel()”。从运行结果中我们可以看到，
即使我们调用的只是 parentJob 的 cancel() 方法，并没有碰过 job1、job2、job3，但是它们内部的协程任务也全都被取消了。
以结构化的方式构建协程以后，我们的 join()、cancel() 等操作，也会以结构化的模式来执行。
输出结果：
================================
Job1 start!
Thread:main @coroutine#3
================================
================================
Job2 start!
Thread:main @coroutine#4
================================
================================
Job3 start!
Thread:main @coroutine#5
================================
job1 === job is true
job2 === job is true
job3 === job is true
================================
// 这里不会等待5秒钟
Process end!
Thread:main @coroutine#1
================================
*/

//当我们总是拿 launch 和 async 来做对比的时候，就会不自觉地认为 async 是用来替代 launch 的。
// 但实际上，async 最常见的使用场景是：与挂起函数结合，优化并发。

//在实际工作中，如果你仔细去分析嵌套的异步代码，你会发现，很多异步任务之间都是没有互相依赖的，
// 这样的代码结合挂起函数后，再通过 async 并发来执行，是可以大大提升代码运行效率的。
// 代码段13

fun main339() = runBlocking {
    suspend fun getResult1(): String {
        delay(1000L) // 模拟耗时操作
        return "Result1"
    }

    suspend fun getResult2(): String {
        delay(1000L) // 模拟耗时操作
        return "Result2"
    }

    suspend fun getResult3(): String {
        delay(1000L) // 模拟耗时操作
        return "Result3"
    }

    val results = mutableListOf<String>()

    val time = measureTimeMillis {
        results.add(getResult1())
        results.add(getResult2())
        results.add(getResult3())
    }
    println("Time: $time")
    println(results)
}

/*
输出结果：
Time: 3018
[Result1, Result2, Result3]
*/

fun main400() = runBlocking {
    suspend fun getResult1(): String {
        delay(1000L) // 模拟耗时操作
        return "Result1"
    }

    suspend fun getResult2(): String {
        delay(1000L) // 模拟耗时操作
        return "Result2"
    }

    suspend fun getResult3(): String {
        delay(1000L) // 模拟耗时操作
        return "Result3"
    }

    val results: List<String>

    val time = measureTimeMillis {
        val result1 = async { getResult1() }
        val result2 = async { getResult2() }
        val result3 = async { getResult3() }

        results = listOf(result1.await(), result2.await(), result3.await())
    }

    println("Time: $time")
    println(results)
}

/*
输出结果：
Time: 1032
[Result1, Result2, Result3]
*/

//============================思考题=============================//

// 代码段15

fun main401() = runBlocking {
    val job = launch {
        logX("First coroutine start!")
        delay(1000L)
        logX("First coroutine end!")
    }

    job.join()
    val job2 = launch(job) {
        logX("Second coroutine start!")
        delay(1000L)
        logX("Second coroutine end!")
    }
    job2.join()
    logX("Process end!")
}
//代码的执行结果是：
//> First coroutine start!
//> First coroutine end!
//> Process end!
//可见 job2 的代码块并没有被执行。
//
//分析原因：
//分别打印出 job2 在 job2.join() 前后的状态：
//
//job2 before join: isActive === false
//job2 before join: isCancelled === true
//job2 before join: isCompleted === false
//// job2.join()
//job2 after join: isActive === false
//job2 after join: isCancelled === true
//job2 after join: isCompleted === true
//
//可见 job2 创建后并没有被激活。
//
//val job2 = launch(job) {} 这一行代码指示 job2 将运行在 job 的 CoroutineContext 之下, 而之前的代码 job.join() 时 job 已经执行完毕了，
// 根据协程结构化的特性，job2 在创建后不会被激活，并且标记为Cancelled，然后执行 job2 时，发现 job2 未被激活，并且已经被取消，则不会执行 job2 的代码块， 但是会将 job2 标记为 Completed