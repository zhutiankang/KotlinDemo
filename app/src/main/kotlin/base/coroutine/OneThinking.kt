package base.coroutine

import kotlinx.coroutines.runBlocking

/**
 * author : tiankang
 * date : 2022/8/7 11:24
 * desc : 协程思维 互相协作的程序
 */

fun main() = runBlocking {
    val sequence = getSequence()
    printSequence(sequence)
}

fun getSequence() = sequence {
    println("Add 1")
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
