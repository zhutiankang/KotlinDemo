package base.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * FiveContext
 *
 * @author tiankang
 * @description: CoroutineContext 协程的上下文 实际开发中它最常见的用处就是切换线程池
 *
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

