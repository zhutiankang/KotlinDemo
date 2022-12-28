package base.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/**
 * EightSelect
 *
 * @author tiankang
 * @description: select 实验性的特性
 * select 与 Deferred、Channel 结合以后，在大大提升程序的响应速度的同时，还可以提高程序的灵活性、扩展性。
 * 场景：“两个挂起函数同时执行，谁返回的速度更快，我们就选择谁”。这其实就是 select 的典型使用场景。
 * 关于 select 的 API，我们完全不需要去刻意记忆，只需要在 Deferred、Channel 的 API 基础上加上 on 这个前缀即可。
 * @date :2022/12/27 17:15
 */
// 代码段1 第一步：查询缓存信息；第二步：缓存服务返回信息，更新 UI；第三步：查询网络服务；第四步：网络服务返回信息，更新 UI。
fun main801() = runBlocking {
    suspend fun getCacheInfo(productId: String): Product? {
        delay(100L)
        return Product(productId, 9.9)
    }

    suspend fun getNetworkInfo(productId: String): Product? {
        delay(200L)
        return Product(productId, 9.8)
    }

    fun updateUI(product: Product) {
        println("${product.productId}==${product.price}")
    }

    val startTime = System.currentTimeMillis()

    val productId = "xxxId"
    // 查询缓存
    val cacheInfo = getCacheInfo(productId)
    if (cacheInfo != null) {
        updateUI(cacheInfo)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }

    // 查询网络
    val latestInfo = getNetworkInfo(productId)
    if (latestInfo != null) {
        updateUI(latestInfo)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }
}


// 代码段4
data class Product(
    val productId: String,
    val price: Double,
    // 是不是缓存信息
    val isCache: Boolean = false
)

/*
输出结果
xxxId==9.9
Time cost: 112
xxxId==9.8
Time cost: 314
整个流程都是建立在“缓存服务一定更快”的前提下的，万一我们的缓存服务出了问题，它的速度变慢了，甚至是超时、无响应呢？

getCacheInfo() 它是一个挂起函数，只有这个程序执行成功以后，才可以继续 执行后面的任务，把 getCacheInfo() 当中的 delay 时间修改成 2000 毫秒，去验证一下。
/*
执行结果：
xxxId==9.9
Time cost: 2013
xxxId==9.8
Time cost: 2214
*/
*/

// 代码段2 async 搭配 select 来使用。async 可以实现并发，select 则可以选择最快的结果。
fun main802() = runBlocking {
    suspend fun getCacheInfo(productId: String): Product? {
        delay(100L)
        return Product(productId, 9.9)
    }

    suspend fun getNetworkInfo(productId: String): Product? {
        delay(200L)
        return Product(productId, 9.8)
    }

    fun updateUI(product: Product) {
        println("${product.productId}==${product.price}")
    }
    val startTime = System.currentTimeMillis()
    val productId = "xxxId"
    //          1，注意这里
    //               ↓
    val product = select<Product?> {
        // 2，注意这里
        async { getCacheInfo(productId) }
            .onAwait { // 3，注意这里
                it
            }
        // 4，注意这里
        async { getNetworkInfo(productId) }
            .onAwait {  // 5，注意这里
                it
            }
    }

    if (product != null) {
        updateUI(product)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }
}

/*
输出结果
xxxId==9.9
Time cost: 127
假设，我们的缓存服务出现了问题，需要 2000 毫秒才能返回

// 代码段3 select 可以在缓存服务出现问题的时候，灵活选择网络服务的结果。从而避免用户等待太长的时间，得到糟糕的体验
suspend fun getCacheInfo(productId: String): Product? {
    // 注意这里
    delay(2000L)
    return Product(productId, 9.9)
}

/*
输出结果
xxxId==9.8
Time cost: 226
*/
*/

// 代码段5 用户大概率是会展示旧的缓存信息。但实际场景下，我们是需要进一步更新最新信息的。
fun main805() = runBlocking {
    suspend fun getCacheInfo(productId: String): Product? {
        delay(100L)
        return Product(productId, 9.9)
    }

    suspend fun getNetworkInfo(productId: String): Product? {
        delay(200L)
        return Product(productId, 9.8)
    }

    fun updateUI(product: Product) {
        println("${product.productId}==${product.price}")
    }

    val startTime = System.currentTimeMillis()
    val productId = "xxxId"

    // 1，缓存和网络，并发执行
    val cacheDeferred = async { getCacheInfo(productId) }
    val latestDeferred = async { getNetworkInfo(productId) }

    // 2，在缓存和网络中间，选择最快的结果
    val product = select<Product?> {
        cacheDeferred.onAwait {
            it?.copy(isCache = true)
        }

        latestDeferred.onAwait {
            it?.copy(isCache = false)
        }
    }

    // 3，更新UI
    if (product != null) {
        updateUI(product)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }

    // 4，如果当前结果是缓存，那么再取最新的网络服务结果
    if (product != null && product.isCache) {
        val latest = latestDeferred.await()?: return@runBlocking
        updateUI(latest)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }
}

/*
输出结果：
xxxId==9.9
Time cost: 120
xxxId==9.8
Time cost: 220
不使用 select 同样也可以实现。不过，select 这样的代码模式的优势在于，扩展性非常好。

*/
// 代码段7 现在我们有了多个缓存服务
fun main807() = runBlocking {
    suspend fun getCacheInfo(productId: String): Product? {
        delay(100L)
        return Product(productId, 9.9)
    }
    // 变化在这里
    suspend fun getCacheInfo2(productId: String): Product? {
        delay(100L)
        return Product(productId, 19.9)
    }

    suspend fun getNetworkInfo(productId: String): Product? {
        delay(200L)
        return Product(productId, 9.8)
    }

    fun updateUI(product: Product) {
        println("${product.productId}==${product.price}")
    }

    val startTime = System.currentTimeMillis()
    val productId = "xxxId"

    val cacheDeferred = async { getCacheInfo(productId) }
    // 变化在这里
    val cacheDeferred2 = async { getCacheInfo2(productId) }
    val latestDeferred = async { getNetworkInfo(productId) }

    val product = select<Product?> {
        cacheDeferred.onAwait {
            it?.copy(isCache = true)
        }

        // 变化在这里
        cacheDeferred2.onAwait {
            it?.copy(isCache = true)
        }

        latestDeferred.onAwait {
            it?.copy(isCache = false)
        }
    }

    if (product != null) {
        updateUI(product)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }

    if (product != null && product.isCache) {
        val latest = latestDeferred.await() ?: return@runBlocking
        updateUI(latest)
        println("Time cost: ${System.currentTimeMillis() - startTime}")
    }
}

/*
输出结果
xxxId==9.9
Time cost: 125
xxxId==9.8
Time cost: 232
*/

// 代码段8
fun main808() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce {
        send(1)
        delay(200L)
        send(2)
        delay(200L)
        send(3)
        delay(150L)
    }

    val channel2 = produce {
        delay(100L)
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
    }

    channel1.consumeEach {
        println(it)
    }

    channel2.consumeEach {
        println(it)
    }

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

/*通过普通的方式，我们的代码是串行执行的，执行结果并不符合预期。channel1 执行完毕以后，才会执行 channel2，程序总体的执行时间，也是两者的总和。
最关键的是，如果 channel1 当中如果迟迟没有数据的话，我们的程序会一直卡着不执行
输出结果
1
2
3
a
b
c
Time cost: 989
*/

// 代码段9
fun main809() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce {
        send("1")
        delay(200L)
        send("2")
        delay(200L)
        send("3")
        delay(150L)
    }

    val channel2 = produce {
        delay(100L)
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
    }

    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String = select<String> {
        // 1， 选择channel1 onReceive{} 是 Channel 在 select 当中的语法，当 Channel 当中有数据以后，它就会被回调
        channel1.onReceive{
            it.also { println(it) }
        }
        // 2， 选择channel1
        channel2.onReceive{
            it.also { println(it) }
        }
    }

    repeat(6){// 3， 选择6次结果 执行了 6 次 select，目的是要把两个管道中的所有数据都消耗掉
        selectChannel(channel1, channel2)
    }

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

/*
输出结果
1
a
2
b
3
c
Time cost: 540
假设 channel1 出了问题，它不再产生数据了
*/

// 代码段10
fun main810() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce<String> {
        // 变化在这里
        delay(15000L)
    }

    val channel2 = produce {
        delay(100L)
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
    }

    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String = select<String> {
        channel1.onReceive{
            it.also { println(it) }
        }
        channel2.onReceive{
            it.also { println(it) }
        }
    }

    // 变化在这里
    repeat(3){
        selectChannel(channel1, channel2)
    }

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

/*
输出结果
a
b
c
Time cost: 533

// 代码段11

// 仅改动这里
repeat(6){
    selectChannel(channel1, channel2)
}
/*
崩溃：
Exception in thread "main" ClosedReceiveChannelException: Channel was closed
*/
*/

// 代码段12 即使我们不知道管道里有多少个数据，我们也不用担心崩溃的问题了。在 onReceiveCatching{} 这个高阶函数当中，我们可以使用 it.getOrNull() 来获取管道里的数据，如果获取的结果是 null，就代表管道已经被关闭了。
fun main812() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce<String> {
        delay(15000L)
    }

    val channel2 = produce {
        delay(100L)
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
    }

    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String =
        select<String> {
            channel1.onReceiveCatching {
                it.getOrNull() ?: "channel1 is closed!"
            }
            channel2.onReceiveCatching {
                it.getOrNull() ?: "channel2 is closed!"
            }
        }

    repeat(6) {
        val result = selectChannel(channel1, channel2)
        println(result)
    }

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}

/*
输出结果
a
b
c
channel2 is closed!
channel2 is closed!
channel2 is closed!
Time cost: 541
程序不会立即退出
*/

// 代码段13
fun main813() = runBlocking {
    val startTime = System.currentTimeMillis()
    val channel1 = produce<String> {
        delay(15000L)
    }

    val channel2 = produce {
        delay(100L)
        send("a")
        delay(200L)
        send("b")
        delay(200L)
        send("c")
    }

    suspend fun selectChannel(channel1: ReceiveChannel<String>, channel2: ReceiveChannel<String>): String =
        select<String> {
            channel1.onReceiveCatching {
                it.getOrNull() ?: "channel1 is closed!"
            }
            channel2.onReceiveCatching {
                it.getOrNull() ?: "channel2 is closed!"
            }
        }

    repeat(6) {
        val result = selectChannel(channel1, channel2)
        println(result)
    }

    // 变化在这里
    channel1.cancel()
    channel2.cancel()

    println("Time cost: ${System.currentTimeMillis() - startTime}")
}
/**
 * 思考与实战
 * 你会发现，当我们的 Deferred、Channel 与 select 配合的时候，它们原本的 API 会多一个 on 前缀。
 * 只要你记住了 Deferred、Channel 的 API，你是不需要额外记忆 select 的 API 的，只需要在原本的 API 的前面加上一个 on 就行了。
 */
//当 select 与 Deferred 结合使用的时候，当并行的 Deferred 比较多的时候，你往往需要在得到一个最快的结果以后，去取消其他的 Deferred。
//vararg varage 关键字声明可变参数 下面都是正确的
// fun main() {
//    compute()
//    compute("leavesC")
//    compute("leavesC", "leavesc")
//    compute("leavesC", "leavesc", "叶")
//}
//
//fun compute(vararg name: String) {
//    name.forEach { println(it) }
//}
//
fun main814() = runBlocking {
    suspend fun <T> fastest(vararg deferreds: Deferred<T>): T = select {
        fun cancelAll() = deferreds.forEach { it.cancel() }

        for (deferred in deferreds) {
            deferred.onAwait {
                cancelAll()
                it
            }
        }
    }

    val deferred1 = async {
        delay(100L)
        println("done1")    // 没机会执行
        "result1"
    }

    val deferred2 = async {
        delay(50L)
        println("done2")
        "result2"
    }

    val deferred3 = async {
        delay(10000L)
        println("done3")    // 没机会执行
        "result3"
    }

    val deferred4 = async {
        delay(2000L)
        println("done4")    // 没机会执行
        "result4"
    }

    val deferred5 = async {
        delay(14000L)
        println("done5")    // 没机会执行
        "result5"
    }

    val result = fastest(deferred1, deferred2, deferred3, deferred4, deferred5)
    println(result)
}

/*
输出结果
done2
result2
*/