package com.github.kotlin.base

/**
 * First
 *
 * @author tiankang
 * @description:
 * @date :2021/12/31 11:18
 */
class First {
    //Kotlin 用过了就回不去
    var price = 100

    var i = 1.toDouble()

    val j: Double? = null

    private val int = 1
    var long = 123456789L
    val double = 13.14
    val float = 13.14F
    val hexadecimal = 0xAF
    val binary = 0b01010101
    val name = "Kotlin"

    fun main() {

        if (j != null) {
            i = j;
        }
        price = 100
        long = int.toLong()

        // 隐式转换被抛弃
        val a = 1
        val b: Long = a.toLong()

        val c = 1
        val d = 2
        val e = 3
        val isTrue = c < d && d < e

        val f: Char = 'A'


        print("Hello $name!")

        val array = arrayOf("Java", "Kotlin")
        print("Hello ${array.get(1)}!")

        val s = """
            当我们的字符串有复杂的格式时 
            原始字符串非常的方便 
            因为它可以做到所见即所得。"""
        print(s)

        val arrayInt = arrayOf(1, 2, 3)
        val arrayString = arrayOf("apple", "pear")

        //数组长度 size
        println("Size is ${arrayString.size}")
        println("First element is ${arrayString[0]}")


        helloFunction("Kotlin")
        //命名参数
        hello(name = "Kotlin")

        createUser("Tom", 30, 1, 78, 2093, 10937, 3285)
        createUser(
            name = "Tom",
            age = 30,
            gender = 1,
            friendCount = 78,
            feedCount = 2093,
            likeCount = 10937,
            commentCount = 3285
        )
        createUser2(name = "Tom", age = 30, commentCount = 3285)
    }

    fun helloFunction(name: String): String {
        return "Hello $name!"
    }

    fun hello(name: String): String = "Hello $name!"

    //以纵向的方式排列
    fun createUser(
        name: String,
        age: Int,
        gender: Int,
        friendCount: Int,
        feedCount: Int,
        likeCount: Long,
        commentCount: Int
    ) {
        //..
    }

    fun createUser2(
        name: String,
        age: Int,
        gender: Int = 1,
        friendCount: Int = 0,
        feedCount: Int = 0,
        likeCount: Long = 0L,
        commentCount: Int = 0
    ) {
        //..
    }

    //流程控制
    fun ifWhenForWhile() {
        val i = 1
        if (i > 0) {
            print("Big")
        } else {
            print("Small")
        }

        val message = if (i > 0) "Big" else "Small"
        print(message)

        when (i) {
            1 -> print("一")
            2 -> print("二")
            else -> print("i 不是一也不是二")
        }

        val desc = when (i) {
            1 -> "一"
            2 -> "二"
            else -> "i 不是一也不是二"
        }

        //和 Java 也没有什么区别
        var j = 0
        do {
            println(j)
            j++
        } while (j <= 2)

        while (j <= 2) {
            println(i)
            j++
        }

        val array = arrayOf(1, 2, 3)
        for (a in array) {
            println(i)
        }
        val oneToThree = 1..3 //[1,3]
        for (o in oneToThree) {
            println(o)
        }
        //逆序迭代
        for (f in 6 downTo 0 step 2) {
            println(f)
        }


    }

    //空安全 可能为 null 的变量 变量类型后面加一个问号“?”
    fun getLength(text: String?): Int {
        return if (text != null) text.length else 0
    }

    //Elvis 表达式 ?.  安全调用符
    fun getLength2(text: String?): Int {
        return text?.length ?: 0
    }
}