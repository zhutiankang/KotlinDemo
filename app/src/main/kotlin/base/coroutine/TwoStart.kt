package base.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.Deferred

/**
 * TwoStart
 *
 * @author tiankang
 * @description: 启动协程
 * @date :2022/8/22 17:36
 *
 * “suspend”关键字，这代表了它是一个挂起函数。而这也就意味着，delay 将会拥有“挂起和恢复”的能力。
 * launch，是典型的“Fire-and-forget”场景，它不会阻塞当前程序的执行流程，使用这种方式的时候，我们无法直接获取协程的执行结果。它有点像是生活中的射箭。
 * runBlocking，我们可以获取协程的执行结果，但这种方式会阻塞代码的执行流程，因为它一般用于测试用途，生产环境当中是不推荐使用的。
 * async，则是很多编程语言当中普遍存在的协程模式。它像是结合了 launch 和 runBlocking 两者的优点。它既不会阻塞当前的执行流程，还可以直接获取协程的执行结果。它有点像是生活中的钓鱼。
 */

// 不必关心代码逻辑，关心输出结果即可
fun main() {
    //launch 创建的协程
    GlobalScope.launch(Dispatchers.IO) {
        println("Coroutine started:${Thread.currentThread().name}")
        delay(1000L)
        println("Hello World!")
    }
    println("After launch:${Thread.currentThread().name}")
    //它的作用是让当前线程休眠 2 秒钟, 为了不让我们的主线程退出   删掉线程休眠的代码以后，协程代码就无法正常工作了
    Thread.sleep(2000L)
}

//---------------------------GlobalScope.launch启动协程 返回JOB 句柄 不建议使用---------------------------------
/*
输出结果：
After launch:main
Coroutine started:DefaultDispatcher-worker-1 @coroutine#1
Edit Configuration 将 VM 参数设置成“-Dkotlinx.coroutines.debug”
在 log 当中打印“Thread.currentThread().name”的时候，如果当前代码是运行在协程当中的，那么它就会带上协程的相关信息
*/
// 1. launch 启动一个协程以后，并没有让协程为我们返回一个执行结果
// 2. launch 一个协程任务，就像猎人射箭一样。协程一旦被 launch，那么它当中执行的任务也不会被中途改变。
// 3. 箭如果命中了猎物，猎物也不会自动送到我们手上来；launch 的协程任务一旦完成了，即使有了结果，也没办法直接返回给调用方。
// GlobalScope 是不建议使用的，因此，后面的案例我们将不再使用 GlobalScope。


fun main0() {
    GlobalScope.launch {                // 1
        println("Coroutine started!")   // 2
        delay(1000L)                    // 3
        println("Hello World!")         // 4
    }

    println("After launch!")            // 5
    Thread.sleep(2000L)                 // 6
    println("Process end!")             // 7
}

/*
输出结果：
After launch!
Coroutine started!
Hello World!
Process end!
运行顺序是 1、5、6、2、3、4、7
*/
//CoroutineStart 其实是一个枚举类，一共有：DEFAULT、LAZY 立即执行、懒加载执行
//(Int) -> Double”代表了参数类型是 Int，返回值类型是 Double 的函数
fun func1(num: Int): Double {
    return num.toDouble()
}

val f1: (Int) -> Double = ::func1


fun CoroutineScope.func2(num: Int): Double {
    return num.toDouble()
}
// 这个函数应该是 CoroutineScope 类的成员方法或是扩展方法，并且，它的参数类型必须是 Int，返回值类型必须是 Double
val f2: CoroutineScope.(Int) -> Double = CoroutineScope::func2


suspend fun func3(num: Int): Double {
    delay(100L)
    return num.toDouble()
}
// 代表了一个“挂起函数”，同时它的参数类型是 Int，返回值类型是 Double。
val f3: suspend (Int) -> Double = ::func3

//--------------------------runBlocking 不建议使用-----------------------------

// 使用 runBlocking 启动的协程会阻塞当前线程的执行，这样一来，所有的代码就变成了顺序执行
// 删掉了末尾的“Thread.sleep(2000L)”，而程序仍然按照顺序执行了。这就进一步说明，runBlocking 确实会阻塞当前线程的执行。
// 对于这一点，Kotlin 官方也强调了：runBlocking 只推荐用于连接线程与协程，并且，大部分情况下，都只应该用于编写 Demo 或是测试代码。
// 所以，请不要在生产环境当中使用 runBlocking。

fun main11() {
    runBlocking {                       // 1
        println("Coroutine started!")   // 2
        delay(1000L)                    // 3
        println("Hello World!")         // 4
    }

    println("After launch!")            // 5
    Thread.sleep(2000L)                 // 6
    println("Process end!")             // 7
}

/*
输出结果：
Coroutine started!
Hello World!
After launch!
Process end!
顺序执行：1、2、3、4、5、6、7。这其实就是 runBlocking 与 launch 的最大差异
*/


fun main12() {
    runBlocking {
        println("First:${Thread.currentThread().name}")
        delay(1000L)
        println("Hello First!")
    }

    runBlocking {
        println("Second:${Thread.currentThread().name}")
        delay(1000L)
        println("Hello Second!")
    }

    runBlocking {
        println("Third:${Thread.currentThread().name}")
        delay(1000L)
        println("Hello Third!")
    }

    // 删掉了 Thread.sleep
    println("Process end!")
}

/*
输出结果：
First:main @coroutine#1
Hello First!
Second:main @coroutine#2
Hello Second!
Third:main @coroutine#3
Hello Third!
Process end!
*/
// runBlocking 其实是可以从协程当中返回执行结果的
// runBlocking 是对 launch 的一种补充，但由于它是阻塞式的，因此，runBlocking 并不适用于实际的工作当中

fun main13() {
    val result = runBlocking {
        delay(1000L)
        // return@runBlocking 可写可不写
        return@runBlocking "Coroutine done!"
    }

    println("Result is: $result")
}
/*
输出结果：
Result is: Coroutine done!
*/

//------------------------------async{} 启动协程 推荐--------------------------------------------
//拿到协程当中的执行结果 async{} 创建协程，并且还能通过它返回的句柄拿到协程的执行结果
// async，就更加像是“钓鱼
//在我们钓鱼的时候，我们手里的鱼竿，就有点像是 async 当中的 Deferred 对象。只要我们手里有这根鱼竿，一旦有鱼儿上钩了，我们就可以直接拿到结果
// async 类比钓鱼，鱼钩已经扔出去了，钓鱼的这个动作已经开始了，只是我并没有拉杆。

fun main14() = runBlocking {
    println("In runBlocking:${Thread.currentThread().name}")

    val deferred: Deferred<String> = async {
        println("In async:${Thread.currentThread().name}")
        delay(1000L) // 模拟耗时操作
        return@async "Task completed!"
    }

    println("After async:${Thread.currentThread().name}")
    //await挂起
    val result = deferred.await()
    println("Result is: $result")
}
/*
输出结果：
In runBlocking:main @coroutine#1
After async:main @coroutine#1 // 注意，它比“In async”先输出  async 启动协程以后，它也不会阻塞当前程序的执行流程
In async:main @coroutine#2
Result is: Task completed!
*/