package base.extra

/**
 * Expression
 *
 * @author tiankang
 * @description: 表达式思维 时刻记住 Kotlin 大部分的语句都是可以作为表达式的
 * @date :2022/8/5 15:25
 */

/*
表达式，是一段可以产生值的代码；而语句，则是一句不产生值的代码

Kotlin 大部分的语句都是表达式，它是可以产生返回值的,利用这种思维，往往可以大大简化代码逻辑。for while 语句
Any 是所有非空类型的根类型，而“Any?”才是所有类型的根类型 object

Unit 与 Java 的 void 类型，代表一个函数不需要返回值，return；而“Unit?”这个类型则没有太多实际的意义只能有 3 种实现方式，即 null、Unit 单例、Nothing

当 Nothing 作为函数返回值的时候，意味着这个函数永远不会返回结果，而且还会截断程序的后续流程。Kotlin 编译器也会根据这一点，进行流程分析。
当 Nothing 作为函数参数的时候，就意味着这个函数永远无法被正常调用。这在泛型星投影的时候是有一定应用的。
另外，Nothing 可以看作是“Nothing?”子类型，因此，
Any?”是所有的 Kotlin 类型的父类，Nothing 则是所有类型的子类
Nothing 可以看作是 Kotlin 所有类型的底类型，子类
正是因为 Kotlin 在类型系统当中，加入了 Unit、Nothing 这两个类型，才让大部分无法产生值的语句摇身一变，成为了表达式。
这也是“Kotlin 大部分的语句都是表达式”的根本原因。

*/
fun main() {


}

class Expression {


//    val i = data ?: 0
//    val j = data ?: getDefault().also { println(it) }
//
//    val k = data?: throw NullPointerException()
//
//
//    val x = when (data) {
//        is Int -> data
//        else -> 0
//    }
//
//    val y = try {
//        "Kotlin".toInt()
//    } catch (e: NumberFormatException) {
//        0
//    }

//    返回值类型是 Unit 的时候，我们既可以选择不写 return，也可以选择 return 一个 Unit 的单例对象。
    fun funUnit(): Unit { }

    fun funUnit1(): Unit { return Unit }

    interface Task<T> {
        fun excute(any: Any): T
    }

    class PrintTask: Task<Unit> {
        override fun excute(any: Any) {
            println(any)
            // 这里写不写return都可以
        }
    }

//    Nothing throw 这个表达式的返回值是 Nothing 类型 Nothing 是所有类型的子类型，那么它当然是可以赋值给任意其他类型的。
//    当一个表达式的返回值是 Nothing 的时候，就往往意味着它后面的语句不再有机会被执行

    //       函数返回值类型是Int，实际上却抛出了异常，没有返回Int
//                ↓       ↓
    fun calculate(): Int = throw NotImplementedError() // 不会报错

    //       函数返回值类型是Any，实际上却抛出了异常，没有返回Any
//                ↓       ↓
    fun calculate1(): Any = throw Exception() // 不会报错

    //       函数返回值类型是Unit，实际上却抛出了异常，没有返回Unit
//                 ↓       ↓
    fun calculate2(): Unit = throw Exception() // 不会报错


    // 不会报错
    fun calculate3(): Nothing = throw NotImplementedError()

    // 不会报错
    fun calculate4(): Nothing = throw Exception()

    // Nothing构造函数是私有的，因此我们无法构造它的实例
    public class Nothing private constructor()


    //               变化在这里
//                   ↓
    fun show(msg: Nothing?) {
        val a = println("Hello World.")
    }

//    由于 Kotlin 存在 Unit 这个类型，因此 println(“Hello World.”) 这行代码也可以变成表达式，它所产生的值就是 Unit 这个单例。
//    由于 Kotlin 存在 Nothing 这个类型，因此 throw 也可以作为表达式，它所产生的值就是 Nothing 类型。

    //Unit 和 Nothing 填补了原本 Java 当中的类型系统，让 Kotlin 的类型系统更加全面。也正因为如此，Kotlin 才可以拥有真正的函数类型
//    Kotlin 的类型系统让大部分的语句都变成了表达式，同时也让无返回值的函数有了类型。
    val f: (String) -> Unit = ::println
}