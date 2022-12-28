package base.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

/**
 * NineConcurrent
 *
 * @author tiankang
 * @description: 并发 在大型软件的架构当中，并发也是一个不可避免的问题
 * Kotlin 协程的并发思路
 * Kotlin 协程也需要处理多线程同步的问题。
 *
 * 对于 Java 当中的同步手段，我们并不能直接照搬到 Kotlin 协程当中来，其中最大的问题，就是 synchronized 不支持挂起函数。
 * 而对于协程并发手段，我也给你介绍了 4 种手段，这些你都需要掌握好。第一种手段，单线程并发，在 Java 世界里，并发往往意味着多线程，但在 Kotlin 协程当中，我们可以轻松实现单线程并发，这时候我们就不用担心多线程同步的问题了。
 * 第二种手段，Kotlin 官方提供的协程同步锁，Mutex，由于它的 lock 方法是挂起函数，所以它跟 JDK 当中的锁不一样，Mutex 是非阻塞的。需要注意的是，我们在使用 Mutex 的时候，应该使用 withLock{} 这个高阶函数，而不是直接使用 lock()、unlock()。
 * 第三种手段，Kotlin 官方提供的 Actor，这是一种普遍存在的并发模型。在目前的版本当中，Kotlin 的 Actor 只是 Channel 的简单封装，它的 API 会在未来的版本发生改变。
 * 第四种手段，借助函数式思维。我们之所以需要处理多线程同步问题，主要还是因为存在共享的可变状态。其实，共享可变状态，既不符合无副作用的特性，也不符合不变性的特性。当我们借助函数式编程思维，实现无副作用和不变性以后，并发代码也会随之变得安全。
 * @date :2022/12/27 18:05
 */

// 代码段2
fun main902() = runBlocking {
    var i = 0
    val jobs = mutableListOf<Job>()

    // 重复十次
    repeat(10){
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                i++
            }
        }
        jobs.add(job)
    }

    // 等待计算完成
    jobs.joinAll()

    println("i = $i")
}
/*
输出结果
i = 9972
建了 10 个协程任务，每个协程任务都会工作在 Default 线程池，这 10 个协程任务，都会分别对 i 进行 1000 次自增操作。如果一切正常的话，代码的输出结果应该是 10000。但如果你实际运行这段代码，你会发现结果大概率不会是 10000。
*/
/**借鉴 Java 的并发思路*/
//使用 @Synchronized 注解来修饰函数，也可以使用 synchronized(){} 的方式来实现同步代码块。
//synchronized 在协程当中也不是一直都很好用的。毕竟，synchronized 是线程模型下的产物。
//synchronized(){} 当中调用挂起函数，编译器会给你报错！
//即使 Kotlin 协程是基于 Java 线程的，但它其实已经脱离 Java 原本的范畴了。所以，单纯使用 Java 的同步手段，是无法解决 Kotlin 协程里所有问题的。

// 代码段3
fun main903() = runBlocking {
    suspend fun prepare(){ // 模拟准备工作
         }
    var i = 0
    val lock = Any() // 变化在这里

    val jobs = mutableListOf<Job>()

    repeat(10){
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                // 变化在这里
                synchronized(lock) {
                    // 编译器报错！ 因为这里的挂起函数会被翻译成带有 Continuation 的异步函数，从而就造成了 synchronid 代码块无法正确处理同步。
                    // prepare()
                    i++
                }
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()

    println("i = $i")
}

/*
输出结果
i = 10000
*/
/**
 * 协程的并发思路
 */
//单线程并发

// 代码段5
fun main905() = runBlocking {
    suspend fun getResult1(): String {
        logX("Start getResult1")
        delay(1000L) // 模拟耗时操作
        logX("End getResult1")
        return "Result1"
    }

    suspend fun getResult2(): String {
        logX("Start getResult2")
        delay(1000L) // 模拟耗时操作
        logX("End getResult2")
        return "Result2"
    }

    suspend fun getResult3(): String {
        logX("Start getResult3")
        delay(1000L) // 模拟耗时操作
        logX("End getResult3")
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
输出结果
================================
Start getResult1
Thread:main
================================
================================
Start getResult2
Thread:main
================================
================================
Start getResult3
Thread:main
================================
================================
End getResult1
Thread:main
================================
================================
End getResult2
Thread:main
================================
================================
End getResult3
Thread:main
================================
Time: 1066
[Result1, Result2, Result3]
启动了三个协程，它们之间是并发执行的，每个协程执行耗时是 1000 毫秒，程序总耗时也是接近 1000 毫秒。而且，这几个协程是运行在同一个线程 main 之上的。
当我们在协程中面临并发问题的时候，首先可以考虑：是否真的需要多线程？如果不需要的话，其实是可以不考虑多线程同步问题的。
*/


// 代码段6
fun main906() = runBlocking {
    val mySingleDispatcher = Executors.newSingleThreadExecutor {
        Thread(it, "MySingleThread").apply { isDaemon = true }
    }.asCoroutineDispatcher()

    var i = 0
    val jobs = mutableListOf<Job>()

    repeat(10) {
        val job = launch(mySingleDispatcher) {
            repeat(1000) {
                i++
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()

    println("i = $i")
}

/*
输出结果
i = 10000
我们使用“launch(mySingleDispatcher)”，把所有的协程任务都分发到了单线程的 Dispatcher 当中，这样一来，我们就不必担心同步问题了。另外，如果仔细分析的话，上面创建的 10 个协程之间，其实仍然是并发执行的。
*/

//Mutex
// Kotlin 官方提供了“非阻塞式”的锁  Mutex 对比 JDK 当中的锁，最大的优势就在于支持挂起和恢复
//在 Java 当中，其实还有 Lock 之类的同步锁。但由于 Java 的锁是阻塞式的，会大大影响协程的非阻塞式的特性。所以，在 Kotlin 协程当中，我们也是不推荐直接使用传统的同步锁的，甚至在某些场景下，在协程中使用 Java 的锁也会遇到意想不到的问题。

// 代码段7
fun main907() = runBlocking {
    val mutex = Mutex()

    var i = 0
    val jobs = mutableListOf<Job>()

    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                // 变化在这里
                mutex.lock()
                i++
//                i/0 // 故意制造异常 代码会在 mutex.lock()、mutex.unlock() 之间发生异常，从而导致 mutex.unlock() 无法被调用。这个时候，整个程序的执行流程就会一直卡住，无法结束。
                mutex.unlock()
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()

    println("i = $i")
}
//直接使用 mutex.lock()、mutex.unlock()不安全，包裹了需要同步的计算逻辑，这样一来，代码就可以实现多线程同步了，程序的输出结果也会是 10000。

// 代码段10
fun main910() = runBlocking {
    val mutex = Mutex()

    var i = 0
    val jobs = mutableListOf<Job>()

    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                // 变化在这里
                mutex.withLock {
                    i++
                }
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()

    println("i = $i")
}

// withLock的定义
//public suspend inline fun <T> Mutex.withLock(owner: Any? = null, action: () -> T): T {
//    lock(owner)
//    try {
//        return action()
//    } finally {
//        unlock(owner)
//    }
//}

// Actor 并发同步模型 本质上是基于 Channel 管道消息实现的
// Kotlin 当中的 Actor 其实就是 Channel 的简单封装。Actor 的多线程同步能力都源自于 Channel
// 代码段11

sealed class Msg
object AddMsg : Msg()

class ResultMsg(
    val result: CompletableDeferred<Int>
) : Msg()

fun main911() = runBlocking {

    suspend fun addActor() = actor<Msg> {
        var counter = 0
        for (msg in channel) {
            when (msg) {
                is AddMsg -> counter++
                is ResultMsg -> msg.result.complete(counter)
            }
        }
    }

    val actor = addActor()
    val jobs = mutableListOf<Job>()

    repeat(10) {
        val job = launch(Dispatchers.Default) {
            repeat(1000) {
                actor.send(AddMsg)
            }
        }
        jobs.add(job)
    }

    jobs.joinAll()

    val deferred = CompletableDeferred<Int>()
    actor.send(ResultMsg(deferred))

    val result = deferred.await()
    actor.close()

    println("i = ${result}")
}

//反思：可变状态 避免共享可变状态  多线程并发，一定需要同步机制吗？
//自函数式编程的思想，因为在函数式编程当中，就是追求不变性、无副作用
//函数式编程的一大优势就在于，它具有不变性、无副作用的特点，所以无惧并发编程。上面的这个代码案例，其实就体现出了 Kotlin 函数式编程的这个优势。
// 代码段12
// 我们不再共享可变状态 i，对应的，在每一个协程当中，都有一个局部的变量 i，同时将 launch 都改为了 async，让每一个协程都可以返回计算结果。
fun main612() = runBlocking {
    val deferreds = mutableListOf<Deferred<Int>>()

    repeat(10) {
        val deferred = async (Dispatchers.Default) {
            var i = 0
            repeat(1000) {
                i++
            }
            return@async i
        }
        deferreds.add(deferred)
    }

    var result = 0
    deferreds.forEach {
        result += it.await()
    }

    println("i = $result")
}

// 代码段13
//我们使用函数式风格代码重构了代码段 12，我们仍然创建了 10 个协程，并发了计算了 10000 次自增操作。
fun main913() = runBlocking {
    val result = (1..10).map {
        async (Dispatchers.Default) {
            var i = 0
            repeat(1000) {
                i++
            }
            return@async i
        }
    }.awaitAll()
        .sum()

    println("i = $result")
}