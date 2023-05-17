package base

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

    //Elvis 表达式 ?.  安全调用符  !! 强⾏调⽤符
    fun getLength2(text: String?): Int {
        return text?.length ?: 0
    }
    //Any Kotlin 的顶层⽗类是 Any ，对应 Java 当中的 Object ，但是⽐ Object 少了 wait()/notify() 等函数
    //Unit Kotlin 中的 Unit 对应 Java 中的 void

//    lateinit 只能修饰 var 可读可写变量(思考下为什么)
//    lateinit 关键字声明的变量的类型必须是「不可空类型」
//    lateinit 声明的变量不能有「初始值」
//    lateinit 声明的变量不能是「基本数据类型」
//    在构造器中初始化的属性不需要 lateinit 关键字

//    类型判断
//    is 判断属于某类型
//    !is 判断不属于某类型
//    as 类型强转，失败时抛出类型强转失败异常
//    as? 类型强转，但失败时不会抛出异常⽽是返回 null

//    获取 Class 对象
//    使⽤ 类名::class 获取的是 Kotlin 的类型是 KClass
//    使⽤ 类名::class.java 获取的是 Java 的类型

    companion object {
        //常量  static final String str = "hello"
        // const 只能在object中使用  val final 不可变
        const val str = "hello"
    }

    //val 是只读的意味着 在运行时已知的不可变  final
    // var 是可变的，在运行时已知
    // const 是不可变的，并且是在编译时已知的变量 static  不可赋值函数，函数需要运行时执行，而不是编译时
}