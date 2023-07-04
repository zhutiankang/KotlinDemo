package base.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*

/**
 * SevenFlow
 *
 * @author tiankang
 * @description: Flow 就是“数据流” 流下去 Flow 极其强大、极其灵活 在异步、并发任务 Flow 是真的香啊！ Flow 已经开始占领 RxJava 原本的领地，在 Android 领域，Flow 甚至还要取代原本 LiveData 的地位
 * 简单的异步场景，我们可以直接使用挂起函数、launch、async；至于复杂的异步场景，我们就可以使用 Flow。
 * Flow 的数据发送方，我们称之为“上游”；数据接收方称之为“下游” 中转站
 * Flow 这样的数据模型，在现实生活中也存在，比如说长江，它有发源地和下游，中间还有很多大坝、水电站，甚至还有一些污水净化厂。
 *
 * Flow，就是数据流。整个 Flow 的 API 设计，可以大致分为三个部分，上游的源头、中间操作符、下游终止操作符。对于上游源头来说，它主要负责：创建 Flow，并且产生数据。而创建 Flow，主要有三种方式：flow{}、flowOf()、asFlow()。
 * 对于中间操作符来说，它也分为几大类。第一类是从集合“抄”过来的操作符，比如 map、filter；第二类是生命周期回调，比如 onStart、onCompletion；第三类是功能型 API，比如说 flowOn 切换 Context、catch 捕获上游的异常。
 * 对于下游的终止操作符，也是分为三大类。首先，就是 collect 这个最基础的终止操作符；其次，就是从集合 API“抄”过来的操作符，比如 fold、reduce；最后，就是 Flow 转换成集合的 API，比如说 flow.toList()。
 * @date :2022/12/27 17:14
 * 对于 Flow 的上游、中间操作符而言，它们其实根本就不需要协程作用域，只有在下游调用 collect{} 的时候，才需要协程作用域。
 */

// 代码段1
fun main700() = runBlocking {
    flow {                  // 上游，发源地
        emit(1)             // 挂起函数 往下游发送数据
        emit(2)
        emit(3)
        emit(4)
        emit(5)
    }.filter { it > 2 }     // 中转站1  中间操作符
        .map { it * 2 }     // 中转站2
        .take(2)            // 中转站3
        .collect{           // 下游 终止操作符或者末端操作符
            println(it)
        }
}

/*
输出结果：
6
8
*/

// 代码段2 Flow API 与集合 API 之间的共性。listOf 创建 List，flowOf 创建 Flow。遍历 List，我们使用 forEach{}；遍历 Flow，我们使用 collect{}。
fun main701() = runBlocking {
    flowOf(1, 2, 3, 4, 5).filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .collect {
            println(it)
        }

    listOf(1, 2, 3, 4, 5).filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .forEach {
            println(it)
        }
}

/*
输出结果
6
8
6
8
*/
// 代码段3 在某些场景下，我们甚至可以把 Flow 当做集合来使用，或者反过来，把集合当做 Flow 来用。Flow.toList()、List.asFlow() 这两个扩展函数，让数据在 List、Flow 之间来回转换
fun main702() = runBlocking {
    // Flow转List
    flowOf(1, 2, 3, 4, 5)
        .toList()
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .forEach {
            println(it)
        }

    // List转Flow
    listOf(1, 2, 3, 4, 5)
        .asFlow()
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .collect {
            println(it)
        }
}

/*
输出结果
6
8
6
8
*/
/**
 * Flow 生命周期
 */
// 代码段4 在 Flow 的中间操作符当中，onStart、onCompletion 这两个是比较特殊的。它们是以操作符的形式存在，但实际上的作用，是监听生命周期回调。
fun main703() = runBlocking {
    flowOf(1, 2, 3, 4, 5)
        .filter {
            println("filter: $it")
            it > 2
        }
        .map {
            println("map: $it")
            it * 2
        }
        .take(2)
        .onStart { println("onStart") } // 注意这里 onStart，它的作用是注册一个监听事件：当 flow 启动以后，它就会被回调
        .collect {
            println("collect: $it")
        }
}

/*
输出结果
onStart onStart 的执行顺序，并不是严格按照上下游来执行的。虽然 onStart 的位置是处于下游，而 filter、map、take 是上游，但 onStart 是最先执行的。因为它本质上是一个回调，不是一个数据处理的中间站。
filter: 1
filter: 2
filter: 3
map: 3
collect: 6
filter: 4
map: 4
collect: 8
*/
// 代码段5 filter、map、take 这类操作符，它们的执行顺序是跟它们的位置相关的。最终的执行结果，也会受到位置变化的影响
fun main704() = runBlocking {
    flowOf(1, 2, 3, 4, 5)
        .take(2) // 注意这里
        .filter {
            println("filter: $it")
            it > 2
        }
        .map {
            println("map: $it")
            it * 2
        }
        .onStart { println("onStart") }
        .collect {
            println("collect: $it")
        }
}
/*
输出结果
onStart
filter: 1
filter: 2
*/

// 代码段6
fun main705() = runBlocking {
    flowOf(1, 2, 3, 4, 5)
        .onCompletion { println("onCompletion") } // 注意这里 onCompletion 的执行顺序，跟它在 Flow 当中的位置无关  onCompletion 只会在 Flow 数据流执行完毕以后，才会回调
        .filter {
            println("filter: $it")
            it > 2
        }
        .take(2)
        .collect {
            println("collect: $it")
        }
}
/*
输出结果
filter: 1
filter: 2
filter: 3
collect: 3
filter: 4
collect: 4
onCompletion
onCompletion{} 在面对以下三种情况时都会进行回调：情况 1，Flow 正常执行完毕；情况 2，Flow 当中出现异常；情况 3，Flow 被取消。
*/
// 代码段7
fun main706() = runBlocking {
    launch {
        flow {
            emit(1)
            emit(2)
            emit(3)
        }.onCompletion { println("onCompletion first: $it") }
            .collect {
                println("collect: $it")
                if (it == 2) {
                    cancel()            // 1 Flow 被取消
                    println("cancel")
                }
            }
    }

    delay(100L)

    flowOf(4, 5, 6)
        .onCompletion { println("onCompletion second: $it") }
        .collect {
            println("collect: $it")
            // 仅用于测试，生产环境不应该这么创建异常
            throw IllegalStateException() // 2 Flow 当中出现异常
        }
}

/*
collect: 1
collect: 2
cancel
onCompletion first: JobCancellationException: // 3
collect: 4
onCompletion second: IllegalStateException    // 4
*/
/**
 * catch 异常处理
 * catch 的作用域，仅限于 catch 的上游。换句话说，发生在 catch 上游的异常，才会被捕获，发生在 catch 下游的异常，则不会被捕获
 * 长江上面的污水处理厂，当然只能处理它上游的水，而对于发生在下游的污染，是无能为力的。
 *
 * 发生在下游的异常
 * 不能用 catch 操作符了。那么最简单的办法，其实是使用 try-catch，把 collect{} 当中可能出现问题的代码包裹起来
 *
 * 针对 Flow 当中的异常处理，我们主要有两种手段：一个是 catch 操作符，它主要用于上游异常的捕获；而 try-catch 这种传统的方式，更多的是应用于下游异常的捕获。
 *
 * onCompletion 不能捕获异常，只能用于判断是否有异常
 * catch 可以捕获异常,异常不捕获就会崩溃  catch 操作符可以捕获来自上游的异常 包括在上游的onCompletion里面抛出的  catch只捕获一次异常，后面的方法就不会调用了
 *
 */
fun main777() = runBlocking {
    flow {
        emit(1)
        throw RuntimeException()
    }.onCompletion { cause ->
        if (cause != null)
            println("Flow completed exceptionally")
        else
            println("Done")
    }.collect { println(it) }
}

/**
 * 1
Flow completed exceptionally
Exception in thread "main" java.lang.RuntimeException
 */
//catch 操作符可以捕获来自上游的异常 包括在上游的onCompletion里面抛出的

fun main888() = runBlocking {
    flow {
        emit(1)
        throw RuntimeException()
    }
        .onCompletion { cause ->
            if (cause != null)
                println("Flow completed exceptionally")
            else
                println("Done")
        }
        .catch{ println("catch exception") }
        .collect { println(it) }
}

/**
 * 1
Flow completed exceptionally
catch exception
 */
// onCompletion 放在catch的下面，则 catch 操作符捕获到异常后，异常就不会影响到下游 onCompletion就会收不到异常
// catch 操作符用于实现异常透明化处理。例如在 catch 操作符内，可以使用 throw 再次抛出异常、可以使用 emit() 转换为发射值、可以用于打印或者其他业务逻辑的处理等等。
// 对于 collect 内的异常，除了传统的 try...catch 之外，还可以借助 onEach 每次对应一个emit值 操作符。把业务逻辑放到 onEach 操作符内，在 onEach 之后是 catch 操作符，最后是 collect()。
fun main666() = runBlocking {
    flow {
        emit(1)
        throw RuntimeException()
    }
        .catch{ println("catch exception") }
        .onCompletion { cause ->
            if (cause != null)
                println("Flow completed exceptionally")
            else
                println("Done")
        }
        .collect { println(it) }
}

//fun main667() = runBlocking<Unit> {
//    flow {
//        ......
//    }
//        .onEach {
//            ......
//        }
//        .catch { ... }
//        .collect()
//}

/**
 * 1
catch exception
Done
 */
// 代码段8
fun main707() = runBlocking {
    val flow = flow {
        emit(1)
        emit(2)
        throw IllegalStateException()
        emit(3)
    }

    flow.map { it * 2 }
        .catch { println("catch: $it") } // 注意这里 ，catch 这个操作符的作用是和它的位置强相关的
        .collect {
            println(it)
        }
}
/*
输出结果：
2
4
catch: java.lang.IllegalStateException
*/

// 代码段9
fun main708() = runBlocking {
    val flow = flow {
        emit(1)
        emit(2)
        emit(3)
    }

    flow.map { it * 2 }
        .catch { println("catch: $it") }
        .filter { it / 0 > 1}  // 故意制造异常
        .collect {
            println(it)
        }
}

/*
输出结果
Exception in thread "main" ArithmeticException: / by zero
*/

// 代码段10
fun main709() = runBlocking {
    flowOf(4, 5, 6)
        .onCompletion { println("onCompletion second: $it") }
        .collect {
            try {
                println("collect: $it")
                throw IllegalStateException()
            } catch (e: Exception) {
                println("Catch $e")
            }
        }
}
/**
 * 切换 Context：flowOn、launchIn
 * Flow 非常适合复杂的异步任务。在大部分的异步任务当中，我们都需要频繁切换工作的线程。对于耗时任务，我们需要线程池当中执行，对于 UI 任务，我们需要在主线程执行。
 * flowOn 操作符也是和它的位置强相关的。它的作用域跟前面的 catch 类似：flowOn 仅限于它的上游  类似 catch 的困境：如果想要指定 collect 当中的 Context withContext{}
 * 不过，这种写法终归是有些丑陋，因此，Kotlin 官方还为我们提供了另一个操作符，launchIn
 * launchIn 从严格意义来讲，应该算是一个下游的终止操作符，因为它本质上是调用了 collect()。
 *
 * flow 当中直接使用 withContext 是很容易引发其他问题的，因此，withContext 在 Flow 当中是不被推荐的，即使要用，也应该谨慎再谨慎。
 */

// 代码段11
fun main710() = runBlocking {
    val flow = flow {
        logX("Start")
        emit(1)
        logX("Emit: 1")
        emit(2)
        logX("Emit: 2")
        emit(3)
        logX("Emit: 3")
    }

    flow.filter {
        logX("Filter: $it")
        it > 2
    }
        .flowOn(Dispatchers.IO)  // 注意这里
        .collect {
            logX("Collect $it")
        }


// 代码段13

// 不推荐
    flow.flowOn(Dispatchers.IO)
        .filter {
            logX("Filter: $it")
            it > 2
        }
        .collect {
            withContext(mySingleDispatcher) {
                logX("Collect $it")
            }
        }
/*
输出结果：
collect{}将运行在MySingleThread
filter{}运行在main
flow{}运行在DefaultDispatcher
*/


// 代码段14

// 不推荐
    withContext(mySingleDispatcher) {
        flow.flowOn(Dispatchers.IO)
            .filter {
                logX("Filter: $it")
                it > 2
            }
            .collect{
                logX("Collect $it")
            }
    }

/*
输出结果：
collect{}将运行在MySingleThread
filter{}运行在MySingleThread
flow{}运行在DefaultDispatcher
*/


// 代码段15 借助了 onEach{} 来实现类似 collect{} 的功能。同时我们在最后使用了 launchIn(scope)，把它下游的代码都分发到指定的线程当中。
    val scope = CoroutineScope(mySingleDispatcher)
    flow.flowOn(Dispatchers.IO)
        .filter {
            logX("Filter: $it")
            it > 2
        }
        .onEach {
            logX("onEach $it")
        }
        .launchIn(scope)
//    scope.launch { // 注意这里 flow.collect() } 等价

/*
输出结果：
onEach{}将运行在MySingleThread
filter{}运行在MySingleThread
flow{}运行在DefaultDispatcher

// 代码段16
public fun <T> Flow<T>.launchIn(scope: CoroutineScope): Job = scope.launch {
    collect() // tail-call
}
*/

}

/*
输出结果
================================
Start
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Filter: 1
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Emit: 1
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Filter: 2
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Emit: 2
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Filter: 3
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Emit: 3
Thread:DefaultDispatcher-worker-1 @coroutine#2
================================
================================
Collect 3
Thread:main @coroutine#1
================================*/

/**
 * 下游：终止操作符 first()、single()、fold{}、reduce{}。collect{} toList
 * map，因为 filter 本身就是一个“中间”操作符 collect 操作符之后，我们无法继续使用 map 之类的操作，因为 collect 是一个“终止”操作符，代表 Flow 数据流的终止。
 */

// 代码段18  当我们调用了 toList() 以后，往后所有的操作符，都不再是 Flow 的 API 调用了，虽然它们的名字没有变，filter、map，这些都只是集合的 API。所以，严格意义上讲，toList 也算是一个终止操作符。
fun main717() = runBlocking {
    // Flow转List
    flowOf(1, 2, 3, 4, 5)
        .toList()           // 注意这里
        .filter { it > 2 }
        .map { it * 2 }
        .take(2)
        .forEach {
            println(it)
        }
}
/**
 * Flow 是“冷”的
 * Channel 之所以被认为是“热”的原因，是因为不管有没有接收方，发送方都会工作。
 * 那么对应的，Flow 被认为是“冷”的原因，就是因为只有调用终止操作符之后，Flow 才会开始工作。
 *
 * Flow 还是“懒”的 Flow 不仅是“冷”的，它还是“懒”的
 * Flow 一次只会处理一条数据。虽然它也是 Flow“冷”的一种表现，但这个特性准确来说是“懒”。
 *
 * “服务员端茶送水”的场景来思考的话，Flow 不仅是一个“冷淡”的服务员，还是一个“懒惰”的服务员：明明饭桌上有 3 个人需要喝水，但服务员偏偏不一次性上 3 杯水，而是要这 3 个人，每个人都叫服务员一次，服务员才会一杯一杯地送 3 杯水过来。
 * ：Flow 默认情况下是“懒惰”的，但也可以通过配置让它“勤快”起来。
 */

// 代码段19
fun main718() = runBlocking {
    // 冷数据流
    val flow = flow {
        (1..3).forEach {
            println("Before send $it")
            emit(it)
            println("Send $it")
        }
    }

    // 热数据流
    val channel = produce<Int>(capacity = 0) {
        (1..3).forEach {
            println("Before send $it")
            send(it)
            println("Send $it")
        }
    }

    println("end")
}

/*
输出结果：
end
Before send 1
// Flow 当中的代码并未执行
*/

// 代码段20
fun main719() = runBlocking {
    flow {
        println("emit: 3")
        emit(3)
        println("emit: 4")
        emit(4)
        println("emit: 5")
        emit(5)
    }.filter {
        println("filter: $it")
        it > 2
    }.map {
        println("map: $it")
        it * 2
    }.collect {
        println("collect: $it")
    }
}
/*
输出结果：
emit: 3
filter: 3
map: 3
collect: 6
emit: 4
filter: 4
map: 4
collect: 8
emit: 5
filter: 5
map: 5
collect: 10
*/

/**
 * 实践
 */

// 代码段22 通过监听 onStart、onCompletion 的回调事件，就可以实现 Loading 弹窗的显示和隐藏。而对于出现异常的情况，我们也可以在 catch{} 当中调用 emit()，给出一个默认值，这样就可以有效防止 UI 界面出现空白。
fun main720() = runBlocking {
    fun loadData() = flow {
        repeat(3) {
            delay(100L)
            emit(it)
            logX("emit $it")
        }
    }
    fun updateUI(it: Int) {}
    fun showLoading() { println("Show loading") }
    fun hideLoading() { println("Hide loading") }
    // 模拟Android、Swing的UI
    val uiScope = CoroutineScope(mySingleDispatcher)

    loadData()
        .onStart { showLoading() }          // 显示加载弹窗
        .map { it * 2 }
        .flowOn(Dispatchers.IO)             // 1，耗时任务
        .catch { throwable ->
            println(throwable)
            hideLoading()                   // 隐藏加载弹窗
            emit(-1)                   // 发生异常以后，指定默认值
        }
        .onEach { updateUI(it) }            // 更新UI界面
        .onCompletion { hideLoading() }     // 隐藏加载弹窗
        .launchIn(uiScope)                  // 2，UI任务

    delay(10000L)
}

/**
 * 思考题
 */

// 代码段23 flow本身已经提供了线程切换的中间操作符flowOn和launchIn，来确定不同部分的线程边界并优化，withContext要再次切换线程，势必打破flow规划好的线程边界，估计要出错，抛出异常来提前报错。
fun main722() = runBlocking {
    flow {
        withContext(Dispatchers.IO) {
            emit(1)
        }
    }.map { it * 2 }
        .collect()
}

/*
输出结果
IllegalStateException: Flow invariant is violated
*/