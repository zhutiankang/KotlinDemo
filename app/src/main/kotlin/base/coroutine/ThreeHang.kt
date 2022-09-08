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
 */

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


// 代码段3
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

// 挂起函数的本质，就是 Callback。
// Kotlin 的函数类型，其实只跟参数、返回值、接收者相关，不过现在又加了一条：还跟 suspend 相关
// suspend 反编译成java Continuation Continuation 本质上也就是一个带有泛型参数的 CallBack
// 从挂起函数转换成 CallBack 函数”的过程，被叫做是 CPS 转换
// Continue 是“继续”的意思，Continuation 则是“接下来要做的事情”。放到程序中，Continuation 就代表了，“程序继续运行下去需要执行的代码”，“接下来要执行的代码”
// 协程之所以是非阻塞，是因为它支持“挂起和恢复”；而挂起和恢复的能力，主要是源自于“挂起函数”；而挂起函数是由 CPS 实现的，其中的 Continuation，本质上就是 Callback。
// 挂起函数，只能在协程当中被调用，或者是被其他挂起函数调用


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