package base.coroutine

import kotlinx.coroutines.*
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * FourCycleLife
 *
 * @author tiankang
 * @description: Job 协程的句柄 协程生命周期、结构化并发 Job 和协程的关系，就有点像“遥控器和空调的关系” 1. 空调遥控器可以监测空调的运行状态；Job 也可以监测协程的运行状态；2.空调遥控器可以操控空调的运行状态，Job 也可以简单操控协程的运行状态。
 * Job作用：1.使用 Job 监测协程的生命周期状态 2.使用 Job 操控协程。
 * @date :2022/12/12 17:37
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
// deferred.await() 这个方法，不仅可以帮助我们获取协程的执行结果，它还会阻塞当前协程的执行流程，直到协程任务执行完毕。在这一点的行为上，await() 和 join() 是类似的。
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
//====================================结构化并发===================================
