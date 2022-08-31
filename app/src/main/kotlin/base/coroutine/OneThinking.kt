package base.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

/**
 * author : tiankang
 * date : 2022/8/7 11:24
 * desc : 协程思维 互相协作的程序 在线程当中的、更加轻量的 Task
 * 协程，可以理解为更加轻量的线程，成千上万个协程可以同时运行在一个线程当中；
 * 协程，其实是运行在线程当中的轻量的 Task；
 * 协程，不会与特定的线程绑定，它可以在不同的线程之间灵活切换。
 * Kotlin 协程的“非阻塞”？答案是：挂起和恢复
 * 协程不会与特定的线程绑定，它可以在不同的线程之间灵活切换，而这其实也是通过“挂起和恢复”来实现的。
 *
 * 协程框架，是独立于 Kotlin 标准库的一套框架，它封装了 Java 的线程，对开发者暴露了协程的 API。
 */

fun main() = runBlocking {
    val sequence = getSequence()
    printSequence(sequence)

    val channel = getProducer(this)
    testConsumer(channel)
}

//Sequence实现
fun getSequence() = sequence {
    println("Add 1")
    //让步 挂起（Suspend）让出执行权 产出
    yield(1)

    println("Add 2")
    yield(2)

    println("Add 3")
    yield(3)

    println("Add 4")
    yield(4)
}

fun printSequence(sequence: Sequence<Int>) {
    val iterator = sequence.iterator()
    val i = iterator.next()
    println("Get$i")

    val j = iterator.next()
    println("Get$j")

    val k = iterator.next()
    println("Get$k")

    val m = iterator.next()
    println("Get$m")
}

/*输出结果：Add 1Get1Add 2Get2Add 3Get3Add 4Get4*/

//Channel实现
fun getProducer(scope: CoroutineScope) = scope.produce {
    println("Send:1")
    send(1)
    println("Send:2")
    send(2)
    println("Send:3")
    send(3)
    println("Send:4")
    send(4)
}

suspend fun testConsumer(channel: ReceiveChannel<Int>) {
    delay(100)
    val i = channel.receive()
    println("Receive$i")
    delay(100)
    val j = channel.receive()
    println("Receive$j")
    delay(100)
    val k = channel.receive()
    println("Receive$k")
    delay(100)
    val m = channel.receive()
    println("Receive$m")
}

/*
输出结果：
Send:1
Receive1
Send:2
Receive2
Send:3
Receive3
Send:4
Receive4
*/

//协程代表了程序当中被创建的协程；协程框架则是一个整体的框架。

//Kotlin 协程其实就是一个封装的线程框架 ：协程框架将线程池进一步封装，对开发者暴露出统一的协程 AP
// 代码中一共启动了两个线程
fun main2() {
    println(Thread.currentThread().name)
    thread {
        println(Thread.currentThread().name)
        Thread.sleep(100)
    }
    Thread.sleep(1000L)
}

/*
输出结果：
main
Thread-0
*/


// 代码中一共启动了两个协程
fun main3() = runBlocking {
    println(Thread.currentThread().name)

    launch {
        println(Thread.currentThread().name)
        delay(100L)
    }

    Thread.sleep(1000L)
}

/*
输出结果：
main @coroutine#1
main @coroutine#2

这里要配置特殊的VM参数：-Dkotlinx.coroutines.debug
这样一来，Thread.currentThread().name就能会包含：协程的名字@coroutine#1
*/

//Kotlin 的协程，我们可以将其想象成一个“更加轻量的线程”
//协程跟线程的关系，有点像线程与进程的关系运行协程不可能脱离线程运行
//在线程当中的、更加轻量的 Task
// thread1 包括 coroutine1...coroutine1000


//协程虽然运行在线程之上，但协程并不会和某个线程绑定，在某些情况下，协程是可以在不同的线程之间切换的

//协程对比线程还有一个特点，那就是非阻塞

fun main4() {
    repeat(3) {
        Thread.sleep(1000L)
        println("Print-1:${Thread.currentThread().name}")
    }

    repeat(3) {
        Thread.sleep(900L)
        println("Print-2:${Thread.currentThread().name}")
    }
}

/*
输出结果：
Print-1:main
Print-1:main
Print-1:main
Print-2:main
Print-2:main
Print-2:main
*/

fun main5() = runBlocking {
    launch {
        repeat(3) {
            delay(1000L)
            println("Print-1:${Thread.currentThread().name}")
        }
    }

    launch {
        repeat(3) {
            delay(900L)
            println("Print-2:${Thread.currentThread().name}")
        }
    }
    delay(3000L)
}

/*
输出结果：
Print-2:main @coroutine#3
Print-1:main @coroutine#2
Print-2:main @coroutine#3
Print-1:main @coroutine#2
Print-2:main @coroutine#3
Print-1:main @coroutine#2

“coroutine#2”、“coroutine#3”这两个协程是并行的
于协程的 delay() 方法是非阻塞的，所以，即使 Print-1 会先执行 delay(1000L)，但它也并不会阻塞 Print-2 的 delay(900L) 的运行
Thread.sleep() 的时候，它仍然会变成阻塞式的  尽量使用 delay，而不是 sleep。

程的 sleep 之所以是阻塞式的，是因为它会阻挡后续 Task 的执行。
而协程之所以是非阻塞式的，是因为它可以支持挂起和恢复。
当 Task 由于某种原因被挂起后，后续的 Task 并不会因此被阻塞。
*/