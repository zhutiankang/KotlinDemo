package base.coroutine

import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * SixChannel
 * Channel 就是管道
 * @author tiankang
 * @description: channel一次返回多个结果
 * 挂起函数、async，它们一次都只能返回一个结果。但在某些业务场景下，我们往往需要协程返回多个结果，比如微信等软件的 IM 通道接收的消息，或者是手机 GPS 定位返回的经纬度坐标需要实时更新 Channel，就是专门用来做这种事情的
 * 不使用 Channel 而是用其他的并发手段配合集合来做的话，其实也能实现，但复杂度会大大增加
 *
 * Channel 是“热”的 Channel 称为“热数据流 前面挂起函数、async 返回的数据，就像是水滴一样，而 Channel 则像是自来水管当中的水流一样。
 *
 * Channel 是“热”的可能会导致一下几个问题：
1. 可能会导致数据的丢失。
2. 浪费不必要的程序资源，类似于非懒加载的情况。
3. 如果未及时 close 的话，可能会导致内存泄露。
 * @date :2022/12/27 17:14
 */

// Channel 这个管道的其中一端，是发送方；管道的另一端是接收方。而管道本身，则可以用来传输数据。
// Channel 可以跨越不同的协程进行通信。我们是在“coroutine#1”当中创建的 Channel，然后分别在 coroutine#2、coroutine#3 当中使用 Channel 来传递数据。
// 代码段1
fun main600() = runBlocking {
    // 1，创建管道
    val channel = Channel<Int>()

    launch {
        // 2，在一个单独的协程当中发送管道消息
        (1..3).forEach {
            channel.send(it) // 挂起函数
            logX("Send: $it")
        }
        //channel 其实也是一种协程资源，在用完 channel 以后，如果我们不去主动关闭它的话，是会造成不必要的资源浪费的。在上面的案例中，如果我们忘记调用“channel.close()”，程序将永远不会停下来。
//        channel.close() // 变化在这里
    }

    launch {
        // 3，在一个单独的协程当中接收管道消息
        for (i in channel) {  // 挂起函数
            logX("Receive: $i")
        }
    }

    logX("end")
}

/*
================================
end
Thread:main @coroutine#1
================================
================================
Receive: 1
Thread:main @coroutine#3
================================
================================
Send: 1
Thread:main @coroutine#2
================================
================================
Send: 2
Thread:main @coroutine#2
================================
================================
Receive: 2
Thread:main @coroutine#3
================================
================================
Receive: 3
Thread:main @coroutine#3
================================
================================
Send: 3
Thread:main @coroutine#2
================================
// 4，程序不会退出
*/


// 代码段3
// 调用“Channel()”的时候，感觉像是在调用一个构造函数，但实际上它却只是一个普通的顶层函数。这个函数带有一个泛型参数 E，另外还有三个参数。
// SUSPEND，当管道的容量满了以后，如果发送方还要继续发送，我们就会挂起当前的 send() 方法。由于它是一个挂起函数，所以我们可以以非阻塞的方式，将发送方的执行流程挂起，等管道中有了空闲位置以后再恢复。
// DROP_OLDEST，顾名思义，就是丢弃最旧的那条数据，然后发送新的数据；
// DROP_LATEST，丢弃最新的那条数据。这里要注意，这个动作的含义是丢弃当前正准备发送的那条数据，而管道中的内容将维持不变。
//public fun <E> Channel(
//    capacity: Int = RENDEZVOUS, Channel的容量默认为 0  UNLIMITED，代表了无限容量； CONFLATED，代表了容量为 1，新的数据会替代旧的数据；BUFFERED，代表了具备一定的缓存容量，默认情况下是 64，具体容量由这个 VM 参数决定 "kotlinx.coroutines.channels.defaultBuffer"。
//    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND, ，onBufferOverflow，也就是指当我们指定了 capacity 的容量，等管道的容量满了时，Channel 的应对策略是怎么样的
//    onUndeliveredElement: ((E) -> Unit)? = null 相当于一个异常处理回调。当管道中的某些数据没有被成功接收的时候，这个回调就会被调用。
//): Channel<E> {}


// 案例 1：capacity = UNLIMITED 代码段4 对于发送方来说，由于 Channel 的容量是无限大的，所以发送方可以一直往管道当中塞入数据，等数据都塞完以后，接收方才开始接收。这跟之前的交替执行是不一样的。
fun main601() = runBlocking {
    // 变化在这里
    val channel = Channel<Int>(capacity = Channel.Factory.UNLIMITED)
    launch {
        (1..3).forEach {
            channel.send(it)
            println("Send: $it")
        }
        channel.close() // 变化在这里
    }
    launch {
        for (i in channel) {
            println("Receive: $i")
        }
    }
    println("end")
}

/*
输出结果：
end
Send: 1
Send: 2
Send: 3
Receive: 1
Receive: 2
Receive: 3
*/

// 代码段5  capacity = CONFLATED 发送方也会一直发送数据，而且，对于接收方来说，它永远只能接收到最后一条数据
fun main602() = runBlocking {
    // 变化在这里
    val channel = Channel<Int>(capacity = Channel.Factory.CONFLATED)

    launch {
        (1..3).forEach {
            channel.send(it)
            println("Send: $it")
        }

        channel.close()
    }

    launch {
        for (i in channel) {
            println("Receive: $i")
        }
    }

    println("end")
}

/*
输出结果：
end
Send: 1
Send: 2
Send: 3
Receive: 3
*/


// 代码段6 用 onBufferOverflow 与 capacity，来实现 CONFLATED 的效果
fun main603() = runBlocking {
    // 变化在这里
    val channel = Channel<Int>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    launch {
        (1..3).forEach {
            channel.send(it)
            println("Send: $it")
        }

        channel.close()
    }

    launch {
        for (i in channel) {
            println("Receive: $i")
        }
    }

    println("end")
}

/*
输出结果：
end
Send: 1
Send: 2
Send: 3
Receive: 3
*/


// 代码段7 onBufferOverflow = BufferOverflow.DROP_LATEST 就意味着，当 Channel 容量满了以后，之后再继续发送的内容，就会直接被丢弃。
fun main604() = runBlocking {
    // 变化在这里
    val channel = Channel<Int>(
        capacity = 3,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    launch {
        (1..3).forEach {
            channel.send(it)
            println("Send: $it")
        }

        channel.send(4) // 被丢弃
        println("Send: 4")
        channel.send(5) // 被丢弃
        println("Send: 5")

        channel.close()
    }

    launch {
        for (i in channel) {
            println("Receive: $i")
        }
    }

    println("end")
}

/*
输出结果：
end
Send: 1
Send: 2
Send: 3
Send: 4
Send: 5
Receive: 1
Receive: 2
Receive: 3
*/


// 代码段8 onUndeliveredElement onUndeliveredElement 的作用，就是一个回调，当我们发送出去的 Channel 数据无法被接收方处理的时候，就可以通过 onUndeliveredElement 这个回调，来进行监听。
// 接收方对数据是否被消费特别关心的场景”。比如说，我发送出去的消息，接收方是不是真的收到了？对于接收方没收到的信息，发送方就可以灵活处理了，比如针对这些没收到的消息，发送方可以先记录下来，等下次重新发送。
fun main605() = runBlocking {
    // 无限容量的管道
    val channel = Channel<Int>(Channel.UNLIMITED) {
        println("onUndeliveredElement = $it")
    }

    // 等价这种写法
//    val channel = Channel<Int>(Channel.UNLIMITED, onUndeliveredElement = { println("onUndeliveredElement = $it") })

    // 放入三个数据
    (1..3).forEach {
        channel.send(it)
    }

    // 取出一个，剩下两个
    channel.receive()

    // 取消当前channel
    channel.cancel()
}

/*
输出结果：
onUndeliveredElement = 2
onUndeliveredElement = 3
*/

/**
 * Channel 关闭引发的问题 另一种创建 Channel 的方式，也就是 produce{} 高阶函数
 */

// 代码段9 我们使用 produce{} 以后，就不用再去调用 close() 方法了，因为 produce{} 会自动帮我们去调用 close() 方法
fun main606() = runBlocking {
    // 变化在这里
    val channel: ReceiveChannel<Int> = produce {
        (1..3).forEach {
            send(it)
            logX("Send: $it")
        }
    }

    launch {
        // 3，接收数据
        for (i in channel) {
            logX("Receive: $i")
        }
    }

    logX("end")
}


// 代码段10 channel 还有一个 receive() 方法，它是与 send(it) 对应的。在上面代码中，我们只调用了 3 次 send()，却调用 4 次 receive()。
fun main607() = runBlocking {
    // 1，创建管道
    val channel: ReceiveChannel<Int> = produce {
        // 发送3条数据
        (1..3).forEach {
            send(it)
        }
    }

    // 调用4次receive()
    channel.receive() // 1
    channel.receive() // 2
    channel.receive() // 3
    channel.receive() // 异常

    logX("end")
}
// 直接使用 receive() 是很容易出问题的。这也是我在前面的代码中一直使用 for 循环，而没有用 receive() 的原因。 以上代码除了可以使用 for 循环以外，还可以使用 Kotlin 为我们提供的另一个高阶函数：channel.consumeEach {}
// 对于发送方，我们可以使用“isClosedForSend”来判断当前的 Channel 是否关闭；对于接收方来说，我们可以用“isClosedForReceive”来判断当前的 Channel 是否关闭。
// 最好不要用 channel.receive()。即使配合 isClosedForReceive 这个判断条件，我们直接调用 channel.receive() 仍然是一件非常危险的事情！ ，如果我们必须要自己来调用 channel.receive()，那么可以考虑使用 receiveCatching()，它可以防止异常发生。
/*
输出结果：
ClosedReceiveChannelException: Channel was closed
*/


// 代码段14

fun main608() = runBlocking {
    val channel: ReceiveChannel<Int> = produce(capacity = 3) {
        (1..300).forEach {
            send(it)
            println("Send $it")
        }
    }

    // 变化在这里
    channel.consumeEach {
        println("Receive $it")
    }

    logX("end")
}

/*
输出结果：

正常
*/

/**
 * channel 热的 “不管有没有接收方，发送方都会工作”的模式，就是我们将其认定为“热”的原因
 * 思维模型：
 * 1. 一个热心的饭店服务员，不管你有没有提要求，服务员都会给你端茶送水，把茶水摆在你的饭桌上。当你想要喝水的时候，就可以直接从饭桌上拿了（当你想要数据的时候，就可以直接从管道里取出来了）
 * 2. 你可以接着前面的水龙头的思维模型去思考，Channel 的发送方，其实就像是“自来水厂”，不管你是不是要用水，自来水厂都会把水送到你家门口的管道当中来。这样当你想要用水的时候，打开水龙头就会马上有水了。
 */

// 代码段15
fun main609() = runBlocking {
    // 只发送不接受
    val channel = produce<Int>(capacity = 10) {
        (1..3).forEach {
            send(it)
            println("Send $it")
        }
    }

    println("end")
}

/*
输出结果：
end
Send 1
Send 2
Send 3
程序结束
*/


// 代码段16
fun main610() = runBlocking {
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
Befour send 1
程序将无法退出
这个程序将无法退出，一直运行下去 由于接收方还未就绪，且管道容量为 0，所以它会被挂起。所以，它仍然还是有在工作的
不管接收方是否存在，Channel 的发送方一定会工作。
对应的，你可以想象成：虽然你的饭桌已经没有空间了，但服务员还是端来了茶水站在了你旁边，只是没有把茶水放在你桌上，等饭桌有了空间，或者你想喝水了，你就能马上喝到。
至于自来水的那个场景，你可以想象成，你家就在自来水厂的门口，你们之间的管道容量为 0，但这并不意味着自来水厂没有工作。
*/


// 代码段19 对外暴露不变性集合的思路吗？其实对于 Channel 来说，我们也可以做到类似的事情
// 不变性思维 对于 Channel 来说，它的 send() 就相当于集合的写入 API，当我们想要做到“对写入封闭，对读取开放”的时候
class ChannelModel {
    // 对外只提供读取功能
    val channel: ReceiveChannel<Int> by ::_channel
    private val _channel: Channel<Int> = Channel()

    suspend fun init() {
        (1..3).forEach {
            _channel.send(it)
        }
    }
}

fun main611() = runBlocking {
    val model = ChannelModel()
    launch {
        model.init()
    }

    model.channel.consumeEach {
        println(it)
    }
}