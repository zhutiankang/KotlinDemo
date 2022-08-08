package base.extra

import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

/**
 * FinalVal
 *
 * @author tiankang
 * @description: 不变性思维
 * @date :2022/8/5 16:06
 */
// 戴着镣铐跳舞 尽可能消灭那些非必要可变性

// 尽可能将 var 变成 val
// 1. 尽可能使用条件表达式消灭 var 消灭数据类当中的可变性

fun testExp(data: Any) {
    var i = 0

    if (data is Number) {
        i = data.toInt()
    } else if (data is String) {
        i = data.length
    } else {
        i = 0
    }
}


fun testExp1(data: Any) {
    val i = when (data) {
        is Number -> {
            data.toInt()
        }
        is String -> {
            data.length
        }
        else -> {
            0
        }
    }
}

// 2.使用数据类来存储数据，消灭数据类的可变性
data class Person(
    var name: String?,
    var age: Int?
)


// var -> val
data class Person1(
    val name: String?,
    val age: Int?
)


// 修改Person的name，然后返回Person对象
fun changeUserName(person: Person, newName: String): Person {
    person.name = newName // 报错，val无法修改
    return person
}

// 修改Person的name，然后返回Person对象
fun changeUserName(person: Person1, newName: String): Person1 {
//    person.name = newName // 报错，val无法修改
    return person
}

fun changeUserName1(person: Person1, newName: String): Person1 =
    person.copy(name = newName)

// 3.尽可能对外暴露只读集合
// List 也是一个接口，但是它代表了一个不可变的列表，或者说是“只读的列表”。在它的接口当中，是没有 add()、remove() 方法的，当我们想要使用可变列表的时候，必须使用 MutableList
class Model {
    val data: MutableList<String> = mutableListOf()

    private fun load() {
        // 网络请求
        data.add("Hello")
    }
}

fun main() {
    val model = Model()
    // 类的外部仍然可以修改data
    model.data.add("World")
}


class Model2 {
    val data: List<String> by ::_data
    private val _data: MutableList<String> = mutableListOf()

    fun load() {
        _data.add("Hello")
    }
}

class Model3 {
    val data: List<String>
        get() = _data //自定义get
    private val _data: MutableList<String> = mutableListOf()

}


class Model4 {
    private val data: MutableList<String> = mutableListOf()

    fun load() {
        data.add("Hello")
    }

    // 变化在这里
    fun getData(): List<String> = data
}
//存在一定的风险，那就是外部可以进行类型转换

fun main2() {
    val model = Model4()
    println("Before:${model.getData()}")
    val data = model.getData()
    (data as? MutableList)?.add("Some data")
    println("After:${model.getData()}")
}

// 其实要解决这个问题的话也很容易，我们只需要借助 Kotlin 提供的 toList 函数，
// 让 data 变成真正的 List 类型即可
class Model5 {
    private val data: MutableList<String> = mutableListOf()

    fun load() {
        data.add("Hello")
    }

    // 变化在这里
    fun getData(): List<String> = data.toList()
}

// 当只读集合在 Java 代码中被访问的时候，它的不变性将会被破坏，因为 Java 当中不存在“不可变的集合”的概念
// 当我们在与 Java 混合编程的时候，Java 里使用 Kotlin 集合的时候一定要足够小心，最好要有详细的文档。
// 就比如说在上面的例子当中，虽然我们可以在 Java 当中调用 set() 方法，但是这行代码最终会抛出异常，引起崩溃。
// 而引起崩溃的原因，是 Kotlin 的 List 最终会变成 Java 当中的“SingletonList”，
// 它是 Java 当中的不可变 List，在它的 add()、remove() 方法被调用的时候，会抛出一个异常。

//public List<String> test() {
//    Model model = new Model();
//    List<String> data = model.getData();
//    data.set(0， "Some Data"); // 抛出异常 UnsupportedOperationException
//    return data;
//}
// 4.Kotlin 当中的只读集合，在 Java 看来和普通的可变集合是一样的,只读集合底层不一定是不可变的，要警惕 Java 代码中的只读集合访问行为


// 5. val 不一定不可变 不管是全局变量还是局部变量

object TestVal {
    val a: Double
        get() = Random.nextDouble()

    fun testVal() {
        println(a)
        println(a)
    }
}

class RandomDelegate() : ReadOnlyProperty<Any?, Double> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return Random.nextDouble()
    }
}

fun testLocalVal() {
    val i: Double by RandomDelegate()

    println(i)
    println(i)
}
