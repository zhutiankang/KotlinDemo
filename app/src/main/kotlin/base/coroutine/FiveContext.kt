package base.coroutine

import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * FiveContext
 *
 * @author tiankang
 * @description: 万物皆有 Context 万物皆为 Context
 * CoroutineContext 协程的上下文 实际开发中它最常见的用处就是切换线程池
 * Dispatcher 的本质仍然还是线程  协程运行在线程之上
 * CoroutineContext，是 Kotlin 协程当中非常关键的一个概念。它本身是一个接口，但它的接口设计与 Map 的 API 极为相似，我们在使用的过程中，也可以把它当作 Map 来用。
 * 协程里很多重要的类，它们本身都是 CoroutineContext。比如 Job、Deferred、Dispatcher、ContinuationInterceptor、CoroutineName、CoroutineExceptionHandler，
 * 它们都继承自 CoroutineContext 这个接口。也正因为它们都继承了 CoroutineContext 接口，所以我们可以通过操作符重载的方式，写出更加灵活的代码，比如“Job() + mySingleDispatcher+CoroutineName(“MyFirstCoroutine!”)”。
 * 协程当中的 CoroutineScope，本质上也是 CoroutineContext 的一层简单封装。
 * 另外，协程里极其重要的“挂起函数”，它与 CoroutineContext 之间也有着非常紧密的联系。
 * @date :2022/12/14 16:24
 */

// 代码段2

fun main402() = runBlocking {
    val user = getUserInfo11()
    logX(user)
}

suspend fun getUserInfo11(): String {
    logX("Before IO Context.")
    withContext(Dispatchers.IO) {
        logX("In IO Context.")
        delay(1000L)
    }
    logX("After IO Context.")
    return "BoyCoder"
}

/*
输出结果：
================================
Before IO Context.
Thread:main @coroutine#1
================================
================================
In IO Context.
Thread:DefaultDispatcher-worker-1 @coroutine#1
================================
================================
After IO Context.
Thread:main @coroutine#1
================================
================================
BoyCoder
Thread:main @coroutine#1
================================
*/


// 代码段3
suspend fun main() {
    val user = getUserInfo()
    logX(user)
}

fun main555() = runBlocking(Dispatchers.IO) {
    val user = getUserInfo()
    logX(user)
}
/*
输出结果：
================================
Before IO Context.
Thread:DefaultDispatcher-worker-1 @coroutine#1
================================
================================
In IO Context.
Thread:DefaultDispatcher-worker-1 @coroutine#1
================================
================================
After IO Context.
Thread:DefaultDispatcher-worker-1 @coroutine#1
================================
================================
BoyCoder
Thread:DefaultDispatcher-worker-1 @coroutine#1
================================
*/

/**
 * 1. Dispatchers.Main，它只在 UI 编程平台才有意义，在 Android、Swing 之类的平台上，一般只有 Main 线程才能用于 UI 绘制。这个 Dispatcher 在普通的 JVM 工程当中，是无法直接使用的。
 * 2. Dispatchers.Unconfined，代表无所谓，当前协程可能运行在任意线程之上。改变代码执行顺序 当前协程可能运行在任何线程之上，不作强制要求。由此可见，Dispatchers.Unconfined 其实是很危险的。所以，我们不应该随意使用 Dispatchers.Unconfined
 * 3. Dispatchers.Default，它是用于 CPU 密集型任务的线程池。一般来说，它内部的线程个数是与机器 CPU 核心数量保持一致的，不过它有一个最小限制 2。
 * 4. Dispatchers.IO，它是用于 IO 密集型任务的线程池。它内部的线程数量一般会更多一些（比如 64 个），具体线程的数量我们可以通过参数来配置：kotlinx.coroutines.io.parallelism。
 * ===Dispatchers.IO 底层是可能复用 Dispatchers.Default 当中的线程的。如果你足够细心的话，会发现前面我们用的都是 Dispatchers.IO，但实际运行的线程却是 DefaultDispatcher 这个线程池。
 * ==== Dispatchers.Default 线程池当中有富余线程的时候，它是可以被 IO 线程池复用的=====
 */


// 代码段6
// 通过 asCoroutineDispatcher() 这个扩展函数，创建了一个 Dispatcher
val mySingleDispatcher = Executors.newSingleThreadExecutor {
    Thread(it, "MySingleThread").apply { isDaemon = true }
}.asCoroutineDispatcher()

//                          变化在这里
//                             ↓
fun main556() = runBlocking(mySingleDispatcher) {
    val user = getUserInfo()
    logX(user)
}
/*
输出结果：
================================
Before IO Context.
Thread:MySingleThread @coroutine#1
================================
================================
In IO Context.
Thread:DefaultDispatcher-worker-1 @coroutine#1
================================
================================
After IO Context.
Thread:MySingleThread @coroutine#1
================================
================================
BoyCoder
Thread:MySingleThread @coroutine#1
================================
*/

/**
 * 都或多或少跟 CoroutineContext 有关系：Job、Dispatcher、CoroutineExceptionHandler、CoroutineScope，甚至挂起函数，它们都跟 CoroutineContext 有着密切的联系。甚至，它们之中的 Job、Dispatcher、CoroutineExceptionHandler 本身，就是 Context。
 */

// ====如果要调用 launch，就必须先有“协程作用域”，也就是 CoroutineScope
// CoroutineScope就是一个简单的接口，而这个接口只有唯一的成员，就是 CoroutineContext。所以，CoroutineScope 只是对 CoroutineContext 做了一层封装而已，它的核心能力其实都来自于 CoroutineContext。
// CoroutineScope 最大的作用，就是可以方便我们批量控制协程。
// CoroutineScope 源码
//public interface CoroutineScope {
//    public val coroutineContext: CoroutineContext
//}

// 代码段10
// 创建了一个简单的 CoroutineScope，接着，我们使用这个 scope 连续创建了三个协程，在 500 毫秒以后，我们就调用了 scope.cancel()，这样一来，代码中每个协程的“end”日志就不会输出了。
fun main557() = runBlocking {
    // 仅用于测试，生成环境不要使用这么简易的CoroutineScope
    val scope = CoroutineScope(Job())

    scope.launch {
        logX("First start!")
        delay(1000L)
        logX("First end!") // 不会执行
    }

    scope.launch {
        logX("Second start!")
        delay(1000L)
        logX("Second end!") // 不会执行
    }

    scope.launch {
        logX("Third start!")
        delay(1000L)
        logX("Third end!") // 不会执行
    }

    delay(500L)

    scope.cancel()

    delay(1000L)
}

/*
输出结果：
================================
First start!
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Third start!
Thread:DefaultDispatcher-worker-3 @coroutine#4
================================
================================
Second start!
Thread:DefaultDispatcher-worker-2 @coroutine#3
================================
*/

//如果说 CoroutineScope 是封装了 CoroutineContext，那么 Job 就是一个真正的 CoroutineContext 了。
// 代码段11
//public interface Job : CoroutineContext.Element {}
//
//public interface CoroutineContext {
//    public interface Element : CoroutineContext {}
//}

// 代码段13
//public interface CoroutineContext {
//
//    public operator fun <E : Element> get(key: Key<E>): E?
//
//    public operator fun plus(context: CoroutineContext): CoroutineContext {}
//
//    public fun minusKey(key: Key<*>): CoroutineContext
//
//    public fun <R> fold(initial: R, operation: (R, Element) -> R): R
//
//    public interface Key<E : Element>
//}
//get()、plus()、minusKey()、fold() 这几个方法，我们可以看到 CoroutineContext 的接口设计，就跟集合 API 一样。准确来说，它的 API 设计和 Map 十分类似。
// 把 CoroutineContext 当作 Map 来用


// 代码段14

@OptIn(ExperimentalStdlibApi::class)
fun main558() = runBlocking {
    // 注意这里
    val scope = CoroutineScope(Job() + mySingleDispatcher)

    scope.launch {
        // 注意这里
        logX(coroutineContext[CoroutineDispatcher] == mySingleDispatcher)
        delay(1000L)
        logX("First end!")  // 不会执行
    }

    delay(500L)
    scope.cancel()
    delay(1000L)
}
// 输出结果：
// ================================
// true
// Thread:MySingleThread @coroutine#2
// ================================
// 实际上，Kotlin 官方的源代码当中大量使用了操作符重载来简化代码逻辑，而 CoroutineContext 就是一个最典型的例子。
// //operator 关键字，如果少了它，我们就得换一种方式了：mySingleDispatcher.plus(Job())。因为，当我们用 operator 修饰 plus() 方法以后，就可以用“+”来重载这个方法
// //     操作符重载
// //        ↓
// public operator fun <E : Element> plus(key: Key<E>):


// 代码段17
//Dispatchers 其实是一个 object 单例，它的内部成员的类型是 CoroutineDispatcher，而它又是继承自 ContinuationInterceptor，这个类则是实现了 CoroutineContext.Element 接口。由此可见，Dispatcher 确实就是 CoroutineContext。
//public actual object Dispatchers {
//
//    public actual val Default: CoroutineDispatcher = DefaultScheduler
//
//    public actual val Main: MainCoroutineDispatcher get() = MainDispatcherLoader.dispatcher
//
//    public actual val Unconfined: CoroutineDispatcher = kotlinx.coroutines.Unconfined
//
//    public val IO: CoroutineDispatcher = DefaultIoScheduler
//
//    public fun shutdown() {    }
//}
//
//public abstract class CoroutineDispatcher :
//    AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {}
//
//public interface ContinuationInterceptor : CoroutineContext.Element {}

//CoroutineName，当我们创建协程的时候，可以传入指定的名称

// 代码段18

@OptIn(ExperimentalStdlibApi::class)
fun main559() = runBlocking {
    val scope = CoroutineScope(Job() + mySingleDispatcher)
    // 注意这里
    scope.launch(CoroutineName("MyFirstCoroutine!")) {
        logX(coroutineContext[CoroutineDispatcher] == mySingleDispatcher)
        delay(1000L)
        logX("First end!")
    }

    delay(500L)
    scope.cancel()
    delay(1000L)
}

/*
输出结果：

================================
true
Thread:MySingleThread @MyFirstCoroutine!#2  // 注意这里
================================
*/
//CoroutineExceptionHandler，它主要负责处理协程当中的异常。
//可以看到，CoroutineExceptionHandler 的接口定义其实很简单，我们基本上一眼就能看懂。CoroutineExceptionHandler 真正重要的，其实只有 handleException() 这个方法，如果我们要自定义异常处理器，我们就只需要实现该方法即可。

// 代码段20

//  这里使用了挂起函数版本的main()
suspend fun main550() {
    val myExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Catch exception: $throwable")
    }
    val scope = CoroutineScope(Job() + mySingleDispatcher)

    val job = scope.launch(myExceptionHandler) {
        val s: String? = null
        s!!.length // 空指针异常
    }

    job.join()
}
/*
输出结果：
Catch exception: java.lang.NullPointerException
*/


// 代码段21
// suspend方法需要在协程中执行，协程又一定有上下文，所以可以访问的到哈~ 也就是在suspend方法中可以访问当前协程上下文，并且拿到一些有用的信息

//                        挂起函数能可以访问协程上下文吗？
//                                 ↓
//suspend fun testContext() = coroutineContext
//
//fun main5500() = runBlocking {
//    println(testContext())
//}