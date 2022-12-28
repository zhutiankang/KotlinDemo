package base.coroutine

import kotlinx.coroutines.*
import java.util.concurrent.Executors

/**
 * TenException
 *
 * @author tiankang
 * @description: 异常 协程就是互相协作的程序，协程是结构化的    而协程的结构化并发，最大的优势就在于：如果我们取消了父协程，子协程也会跟着被取消
 * 在 Kotlin 协程当中，异常主要分为两大类，一类是协程取消异常（CancellationException），另一类是其他异常。为了处理这两大类问题，我们一共总结出了 6 大准则，这些我们都要牢记在心
 * 因为协程是“结构化的”，所以异常传播也是“结构化的”
 * 第一条准则：协程的取消需要内部的配合。
 * 第二条准则：不要轻易打破协程的父子结构！              这一点，其实不仅仅只是针对协程的取消异常，而是要贯穿于整个协程的使用过程中。我们知道，协程的优势在于结构化并发，它的许多特性都是建立在这个特性之上的，如果我们无意中打破了它的父子结构，就会导致协程无法按照预期执行。
 * 第三条准则：捕获了 CancellationException 以后，要考虑是否应该重新抛出来。   在协程体内部，协程是依赖于 CancellationException 来实现结构化取消的，有的时候我们出于某些目的需要捕获 CancellationException，但捕获完以后，我们还需要思考是否需要将其重新抛出来。
 * 第四条准则：不要用 try-catch 直接包裹 launch、async。   这一点是很多初学者会犯的错误，考虑到协程代码的执行顺序与普通程序不一样，我们直接使用 try-catch 包裹 launch、async，是不会有任何效果的。
 * 第五条准则：灵活使用 SupervisorJob，控制异常传播的范围。    SupervisorJob 是一种特殊的 Job，它可以控制异常的传播范围。普通的 Job，它会因为子协程当中的异常而取消自身，而 SupervisorJob 则不会受到子协程异常的影响。在很多业务场景下，我们都不希望子协程影响到父协程，所以 SupervisorJob 的应用范围也非常广。比如说 Android 当中的 viewModelScope，它就使用了 SupervisorJob，这样一来，我们的 App 就不会因为某个子协程的异常导致整个应用的功能出现紊乱。
 * 第六条准则：使用 CoroutineExceptionHandler 处理复杂结构的协程异常，它仅在顶层协程中起作用。     我们都知道，传统的 try-catch 在协程当中并不能解决所有问题，尤其是在协程嵌套层级较深的情况下。这时候，Kotlin 官方为我们提供了 CoroutineExceptionHandler 作为补充。有了它，我们可以轻松捕获整个作用域内的所有异常。
 * @date :2022/12/27 18:06
 */
/**
 * 为什么 cancel() 不起作用？
 */
// 场景 1：cancel() 不被响应

//协程是互相协作的程序。因此，对于协程任务的取消，也是需要互相协作的。协程外部取消，协程内部需要做出响应才行

// 代码段1

fun main101() = runBlocking {
    val job = launch(Dispatchers.Default) {
        var i = 0
        while (true) {
            Thread.sleep(500L)
            i ++
            println("i = $i")
        }
    }

    delay(2000L)

    job.cancel()
    job.join()

    println("End")
}

/*
输出结果

i = 1
i = 2
i = 3
i = 4
i = 5
// 永远停不下来 代码段 1 无法取消的原因了：当我们调用 job.cancel() 以后，协程任务已经不是活跃状态了，但代码并没有把 isActive 作为循环条件，因此协程无法真正取消。
*/

// 代码段2

fun main102() = runBlocking {
    val job = launch(Dispatchers.Default) {
        var i = 0
        // 变化在这里
        while (isActive) {
            Thread.sleep(500L)
            i ++
            println("i = $i")
        }
    }

    delay(2000L)

    job.cancel()
    job.join()

    println("End")
}

/*
输出结果
i = 1
i = 2
i = 3
i = 4
i = 5
End
*/

//场景 2：结构被破坏

// 代码段3

val fixedDispatcher = Executors.newFixedThreadPool(2) {
    Thread(it, "MyFixedThread").apply { isDaemon = false }
}.asCoroutineDispatcher()

fun main() = runBlocking {
    // 父协程
    val parentJob = launch(fixedDispatcher) {

        // 1，注意这里 创建子协程的时候，使用了 launch(Job()){}。而这种创建方式，就打破了原有的协程结构。
        // 子协程 1”已经不是 parentJob 的子协程了，而对应的，它的父 Job 是我们在 launch 当中传入的 Job() 对象
        launch(Job()) { // 子协程1
            var i = 0
            while (isActive) {
                Thread.sleep(500L)
                i ++
                println("First i = $i")
            }
        }

        launch { // 子协程2
            var i = 0
            while (isActive) {
                Thread.sleep(500L)
                i ++
                println("Second i = $i")
            }
        }
    }

    delay(2000L)

    parentJob.cancel()
    parentJob.join()

    println("End")
}

/*
输出结果
First i = 1
Second i = 1
First i = 2
Second i = 2
Second i = 3
First i = 3
First i = 4
Second i = 4
End
First i = 5
First i = 6
// 子协程1永远不会停下来
*/

//场景 3：未正确处理 CancellationException

// 代码段5
fun main105() = runBlocking {

    val parentJob = launch(Dispatchers.Default) {
        launch {
            var i = 0
            while (true) {
                // 变化在这里 对于 Kotlin 提供的挂起函数，它们是可以自动响应协程的取消的，比如说，当我们把 Thread.sleep(500) 改为 delay(500) 以后，我们就不需要在 while 循环当中判断 isActive 了。
                delay(500L)
                //把“throw e”这行代码注释掉，重新运行之后，程序就永远无法终止了。这主要是因为，我们捕获了 CancellationException 以后没有重新抛出去，就导致子协程无法正常取消。
                //很多开发者喜欢在代码里捕获 Exception 这个父类，比如这样：catch(e: Exception){}，这也是很危险的。平时写 Demo 为了方便这样写没问题，但在生产环境则应该禁止。
//                try { delay(500L) } catch (e: CancellationException) { println("Catch CancellationException") // 1，注意这里 // throw e }
                i ++
                println("First i = $i")
            }
        }

        launch {
            var i = 0
            while (true) {
                // 变化在这里
                delay(500L)
                i ++
                println("Second i = $i")
            }
        }
    }

    delay(2000L)

    parentJob.cancel()
    parentJob.join()

    println("End")
}

/*
输出结果
First i = 1
Second i = 1
First i = 2
Second i = 2
First i = 3
Second i = 3
End
对于 delay() 函数来说，它可以自动检测当前的协程是否已经被取消，如果已经被取消的话，它会抛出一个 CancellationException，从而终止当前的协程。
*/

/**
 * 普通异常
 */
//try-catch 不起作用

// 代码段8

fun main108() = runBlocking {
    try {
        launch {
            delay(100L)
            1 / 0 // 故意制造异常
        }
    } catch (e: ArithmeticException) {
        println("Catch: $e")
    }

    delay(500L)
    println("End")
}

/*
输出结果：
崩溃
Exception in thread "main" ArithmeticException: / by zero
码段 8 当中的 launch 换成 async，结果也是差不多的
当协程体当中的“1/0”执行的时候，我们的程序已经跳出 try-catch 的作用域了
*/

// 代码段10

fun main110() = runBlocking {

    launch {
        try {
            delay(100L)
            1 / 0 // 故意制造异常
        } catch (e: ArithmeticException) {
            println("Catch: $e")
        }
    }

    delay(500L)
    println("End")
}

/*
输出结果：
Catch: java.lang.ArithmeticException: / by zero
End
以把 try-catch 挪到 launch{} 协程体内部。这样一来，它就可以正常捕获到 ArithmeticException 这个异常了
*/

// 代码段11
fun main111() = runBlocking {
    var deferred: Deferred<Unit>? = null

    deferred = async {
        try {
            delay(100L)
            1 / 0
        } catch (e: ArithmeticException) {
            println("Catch: $e")
        }
    }

    deferred?.await()

    delay(500L)
    println("End")
}

// 代码段12
// async 当中产生异常，即使我们不调用 await() 同样是会导致程序崩溃的
fun main112() = runBlocking {
    val deferred = async {
        delay(100L)
        1 / 0
    }

    try {
        deferred.await()
    } catch (e: ArithmeticException) {
        println("Catch: $e")
    }

    delay(500L)
    println("End")
}

/*
输出结果
Catch: java.lang.ArithmeticException: / by zero
崩溃：
Exception in thread "main" ArithmeticException: / by zero
*/

// SupervisorJob 如果我们要使用 try-catch 包裹“deferred.await()”的话，还需要配合 SupervisorJob 一起使用
// SupervisorJob() 其实不是构造函数，它只是一个普通的顶层函数
// SupervisorJob 与 Job 最大的区别就在于，当它的子 Job 发生异常的时候，其他的子 Job 不会受到牵连
// 代码段14
fun main114() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    scope.async {
        delay(100L)
        1 / 0
    }

    delay(500L)
    println("End")
}

/*
输出结果
End
“不调用 await() 就不会产生异常而崩溃
*/

// 代码段15
fun main115() = runBlocking {
    val scope = CoroutineScope(SupervisorJob())
    // 变化在这里
    val deferred = scope.async {
        delay(100L)
        1 / 0
    }

    try {
        deferred.await()
    } catch (e: ArithmeticException) {
        println("Catch: $e")
    }

    delay(500L)
    println("End")
}

/*
输出结果
Catch: java.lang.ArithmeticException: / by zero
End
*/

//CoroutineExceptionHandler CoroutineContext 的元素之一，我们在创建协程的时候，可以指定对应的 CoroutineExceptionHandler。
//我模拟了一个复杂的协程嵌套场景。对于这样的情况，我们其实很难一个个在每个协程体里面去写 try-catch。所以这时候，为了捕获到异常，我们就可以使用 CoroutineExceptionHandler 了。
// 代码段17
fun main117() = runBlocking {

    val scope = CoroutineScope(coroutineContext)

    scope.launch {
        async {
            delay(100L)
        }

        launch {
            delay(100L)

            launch {
                delay(100L)
                1 / 0 // 故意制造异常
            }
        }

        delay(100L)
    }

    delay(1000L)
    println("End")
}

/*
输出结果
Exception in thread "main" ArithmeticException: / by zero
*/

// 代码段18
fun main118() = runBlocking {
    val myExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Catch exception: $throwable")
    }

    // 注意这里
    val scope = CoroutineScope(coroutineContext + Job() + myExceptionHandler)

    scope.launch {
        async {
            delay(100L)
        }

        launch {
            delay(100L)

            launch {
                delay(100L)
                1 / 0 // 故意制造异常
            }
        }

        delay(100L)
    }

    delay(1000L)
    println("End")
}

/*
Catch exception: ArithmeticException: / by zero
End
*/

// 代码段19
fun main119() = runBlocking {
    val myExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("Catch exception: $throwable")
    }

    // 不再传入myExceptionHandler
    val scope = CoroutineScope(coroutineContext)
    scope.launch {
        async {
            delay(100L)
        }
        launch {
            delay(100L)
            // 变化在这里 不起作用 myExceptionHandler 直接定义在发生异常的位置反而不生效，而定义在最顶层却可以生效！
            launch(myExceptionHandler) {
                delay(100L)
                1 / 0
            }
        }
        delay(100L)
    }
    delay(1000L)
    println("End")
}
/*
输出结果
崩溃：
Exception in thread "main" ArithmeticException: / by zero

CoroutineExceptionHandler 只在顶层的协程当中才会起作用。也就是说，当子协程当中出现异常以后，它们都会统一上报给顶层的父协程，然后顶层的父协程才会去调用 CoroutineExceptionHandler，来处理对应的异常。

（1）Cancel依赖Cancel异常。
（2）SupervisorJob重写了childCancelled=false，导致取消不会向上和兄弟传播。
（3）异常的传播应该是先向上传播，然后都没人处理才会触发协程的CoroutineExceptionHandler，在触发全局默认的CoroutineExceptionHandler。
（4）ExceptionHandler代替try catch不合理，无法清晰的对业务异常有一个认知，不知道是哪里来的，只能通用处理；同时我认为ExceptionHandler或者作为兜底策略也是合理的，
子协程对自己的业务进行异常处理，同时顶层协程有一个兜底策略，上报后需要及时让子协程进行处理；这个问题就像Java线程要不要加UnCaughtExceptionHandler【协程也可以加默认的】。
*/