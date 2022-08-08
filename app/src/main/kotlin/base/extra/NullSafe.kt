package base.extra

/**
 * NullSafe
 *
 * @author tiankang
 * @description: 空安全思维
 * @date :2022/8/5 17:34
 */

// 1. 警惕 Kotlin 以外的数据类型,这里主要分为两大类，第一种是：Kotlin 与其他语言的交互，比如和 Java 交互；第二种是：Kotlin 与外界环境的交互，比如 JSON 解析。
// 2. 绝不使用非空断言“!!.” 这里主要是两个场景需要注意，一个是：IDE 的“Convert Java File To Kotlin File”功能转换的 Kotlin 代码，一定要 review，消灭其中的非空断言；另一个是：当 Smart Cast 失效的时候，我们要用其他办法来解决，而不是使用非空断言
// 空安全调用语法“?.”，其实，Kotlin 还提供了一个非空安全的调用语法“!!.
// 在某些场景下，Smart Cast 失效了，即使我们判空了，也免不了还是要继续使用非空断言。
// 3. 尽可能使用非空类型。 借助 lateinit、懒加载，我们可以做到灵活初始化的同时，还能消灭可空类型
// 4. 明确泛型的可空性。我们不能被泛型 T 的外表所迷惑，当我们定义 的时候，一定要记住，它是可空的。在非空的场景下，我们一定要明确它的可空性，这一点，通过增加泛型的边界就能做到 。

fun testNPE(msg: String?) {
//            非空断言
//              ↓
    val i = msg!!.length
}

fun main() {
//    NullExample.testNPE(null)
}


class JavaConvertExample {
    private var name: String? = null
    fun init() {
        name = ""
    }

    fun foo() {
        name = null;
    }

    fun test() {
        if (name != null) {
            // 几百行代码
            foo()
            //几百行代码
            val count = name!!.length
        }
    }
}

// 第一种，避免直接访问成员变量或者全局变量，将其改为传参的形式,在 Kotlin 当中，函数的参数是不可变的，
// 因此，当我们将外部的成员变量或者全局变量以函数参数的形式传进来以后，它可以用于 Smart Cast 了。

//    改为函数参数
//        ↓
fun test(name: String?) {
    if (name != null) {
//             函数参数支持Smart Cast
//                      ↓
        val count = name.length
    }
}

// 第二种，避免使用可变变量 var，改为 val 不可变变量：
// 既然引发问题的根本原因是可变性导致的，我们直接将其改为不可变的即可。从这里，我们也可以看到“空安全”与“不可变性”之间的关联。
class NullSafe {
    private val name: String? = null


    fun test() {
        if (name != null) {
            // 不可变变量支持SmartCast
            val count = name.length
        }
    }
}

// 第三种，借助临时的不可变变量
class JavaConvertExample2 {
    private var name: String? = null

    fun test() {
//        不可变变量
//            ↓
        val _name = name
        if (_name != null) {
            // 在if当中，只使用_name这个临时变量
            val count = _name.length
        }
    }
}

// 第四种，是借助 Kotlin 提供的标准函数 let： 更优雅，类似第三种方式
class JavaConvertExample3 {
    private var name: String? = null

    fun test() {
//                      标准函数
//                         ↓
        val count = name?.let { it.length }
    }
}

//第五种，是借助 Kotlin 提供的 lateinit 关键字：于它的类型是不可能为空的，因此我们初始化的时候，
//必须传入一个非空的值，这就能保证：只要 name 初始化了，它的值就一定不为空。在这种情况下，我们就将判空问题变成了一个判断是否初始化的问题。
class JavaConvertExample4 {
    private lateinit var name: String

    fun init() {
        name = "Tom"
    }

    fun test() {
        if (this::name.isLateinit) {
            val count = name.length
        }
    }
}

// 第六种，使用 by lazy 委托：
class JavaConvertExample5 {
    private val name: String by lazy { init() }
    fun init() = "Tom"
    fun test() {
        val count = name.length
    }
}
//泛型可空性

// 泛型定义处              泛型使用处
//   ↓                      ↓
fun <T> saveSomething(data: T) {
    val set = sortedSetOf<T>() // Java TreeSet
    // 空指针异常
    set.add(data)
}

fun main1() {
//                 泛型实参自动推导为String
//                        ↓
    saveSomething("Hello world!")
    // 编译通过// ↓
    saveSomething(null)
}

// T 是非空的，而“T?”才是可空的。实际上，我们的 T 是等价于 的，因为 Any? 才是 Kotlin 的根类型。
// 这也就意味着，泛型的 T 是可以接收 null 作为实参的。
fun <T> saveSomething1(data: T) {}
//   ↑
//  等价
//   ↓
fun <T: Any?> saveSomething2(data: T) {}


// 增加泛型的边界限制  我们为泛型 T 增加了上界“Any”，由于 Any 是所有非空类型的“根类型”，这样就能保证我们的 data 一定是非空的
//       ↓
fun <T: Any> saveSomething3(data: T) {
    val set = sortedSetOf<T>()
    set.add(data)
}

fun main4() {
//              编译无法通过
//                  ↓
//    saveSomething3(null)
}