package base.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * ThreeHang
 *
 * @author tiankang
 * @description: 挂起函数：Kotlin协程的核心
 * @date :2022/8/31 10:48
 * 仅只是因为“轻量”“非阻塞”，我们就应该放弃线程，拥抱协程吗
 * Kotlin 协程最大的优势，就在于它的挂起函数 suspend 挂起函数的本质，就是 Callback。
 * Kotlin 挂起函数的核心原理，它的挂起和恢复，其实也是通过 CPS 转换来实现的
 *
 * 要定义挂起函数，我们只需在普通函数的基础上，增加一个 suspend 关键字。suspend 这个关键字，是会改变函数类型的，“suspend (Int) -> Double”与“(Int) -> Double”并不是同一个类型。
 * 挂起函数，由于它拥有挂起和恢复的能力，因此对于同一行代码来说，“=”左右两边的代码分别可以执行在不同的线程之上。而这一切，都是因为 Kotlin 编译器这个幕后的翻译官在起作用。
 * 挂起函数的本质，就是 Callback。只是说，Kotlin 底层用了一个更加高大上的名字，叫 Continuation。而 Kotlin 编译器将 suspend 翻译成 Continuation 的过程，则是 CPS 转换。这里的 Continuation 是代表了，“程序继续运行下去需要执行的代码”，“接下来要执行的代码”，或者是 “剩下的代码”。
 */

//可读性差、扩展性差、维护性差，极易出错！
//getUserInfo(new CallBack() {
//    @Override
//    public void onSuccess(String user) {
//        if (user != null) {
//            System.out.println(user);
//            getFriendList(user, new CallBack() {
//                @Override
//                public void onSuccess(String friendList) {
//                    if (friendList != null) {
//                        System.out.println(friendList);
//                        getFeedList(friendList, new CallBack() {
//                            @Override
//                            public void onSuccess(String feed) {
//                                if (feed != null) {
//                                    System.out.println(feed);
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//        }
//    }
//});
// 用同步的方式来写异步代码，对比起前面“回调地狱”式的代码，挂起函数写出来的代码可读性更好、扩展性更好、维护性更好，并且更难出错。

// withContext(Dispatchers.IO)，作用是控制协程执行的线程池
// 代码段3
// 挂起，只是将程序执行流程转移到了其他线程，主线程不会被阻塞。如果以上代码运行在 Android 系统，我们的 App 仍然可以响应用户的操作，主线程并不繁忙。
suspend fun main() {
    // 以同步的方式完成异步任务。
    // 调用挂起函数的位置，我们叫做是挂起点 一行代码，切换了两个线程, 其中“=”左边的代码运行在主线程，而“=”右边的代码运行在 IO 线程。
    // 每一次从主线程到 IO 线程，都是一次协程挂起
    // 每一次从 IO 线程到主线程，都是一次协程恢复
    // 挂起和恢复，这是挂起函数特有的能力，普通函数是不具备的。
    val user = getUserInfo()
    val friendList = getFriendList(user)
    val feedList = getFeedList(friendList)
}

// delay(1000L)用于模拟网络请求 挂起 恢复
// 挂起函数 所谓的挂起函数，其实就是比普通的函数多了一个 suspend 关键字而已
// suspend，是 Kotlin 当中的一个关键字，它主要的作用是用于定义“挂起函数
// ↓
suspend fun getUserInfo(): String {
    withContext(Dispatchers.IO) {
        delay(1000L)
    }
    return "BoyCoder"
}

//挂起函数
// ↓
suspend fun getFriendList(user: String): String {
    withContext(Dispatchers.IO) {
        delay(1000L)
    }
    return "Tom, Jack"
}

//挂起函数
// ↓
suspend fun getFeedList(list: String): String {
    withContext(Dispatchers.IO) {
        delay(1000L)
    }
    return "{FeedList..}"
}

// 挂起函数的本质，就是 Callback。 Continuation 本质上也就是一个带有泛型参数的 CallBack kotlin反编译成java Continuation等价于CallBack
// 高阶函数 Kotlin 的函数类型，其实只跟参数、返回值、接收者相关，不过现在又加了一条：还跟 suspend 相关
// suspend 反编译成java Continuation Continuation 本质上也就是一个带有泛型参数的 CallBack

// 从挂起函数转换成 CallBack 函数”的过程，被叫做是 CPS 转换
// 握住Continuation 的词源 Continue 即可  Continue 是“继续”的意思，Continuation 则是“接下来要做的事情”。放到程序中，Continuation 就代表了，“程序继续运行下去需要执行的代码”，“接下来要执行的代码”
// 解了 Continuation 以后，CPS 也就容易理解了，它其实就是将程序接下来要执行的代码进行传递的一种模式。
// CPS 转换，就是将原本的同步挂起函数转换成 CallBack 异步代码的过程。这个转换是编译器在背后做的，我们程序员对此并无感知。Continuation-Passing-Style Transformation


// =================================================协程与挂起函数你可能觉得，既然协程和挂起函数都是支持挂起和恢复的，那它们两个是不是同一个东西呢？答案当然是否定的。=======================================
// 协程之所以是非阻塞，是因为它支持“挂起和恢复”；而挂起和恢复的能力，主要是源自于“挂起函数”；而挂起函数是由 CPS 实现的，其中的 Continuation，本质上就是 Callback。
// 挂起函数，只能在协程当中被调用，或者是被其他挂起函数调用 协程和挂起函数”都可以调用“挂起函数”，但是协程的 Lambda，也是挂起函数。所以，它们本质上都是因为“挂起函数可以调用挂起函数”。


// 代码段9

// 在协程中调用getUserInfo()
fun main40() = runBlocking {
    val user = getUserInfo()
}

// 在另一个挂起函数中调用getUserInfo()
suspend fun anotherSuspendFunc() {
    val user = getUserInfo()
}
// 代码段10 原来 block 也是一个挂起函数的类型！
// 虽然“协程和挂起函数”都可以调用“挂起函数”，但是协程的 Lambda，也是挂起函数。所以，它们本质上都是因为“挂起函数可以调用挂起函数
//public actual fun <T> runBlocking(
//    context: CoroutineContext,
//    block: suspend CoroutineScope.() -> T
//): T {
//}

// 挂起和恢复，是协程的一种底层能力；而挂起函数，是这种底层能力的一种表现形式，
// 通过暴露出来的 suspend 关键字，我们开发者可以在上层，非常方便地使用这种底层能力。

//public interface Continuation<in T> { suspend函数的入参Continuation，看源码可以知道需要有一个协程上下文CoroutineContext信息，只有在协程作用域里才能传递
//    /**
//     * The context of the coroutine that corresponds to this continuation.
//     */
//    public val context: CoroutineContext
//
//    /**
//     * Resumes the execution of the corresponding coroutine passing a successful or failed [result] as the
//     * return value of the last suspension point.
//     */
//    public fun resumeWith(result: Result<T>)
//}
