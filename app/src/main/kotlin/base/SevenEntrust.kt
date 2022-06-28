package base

import android.widget.TextView
import kotlin.reflect.KProperty

/**
 * author : tiankang
 * date : 2022/6/20 21:43
 * desc : 委托
 */
class SevenEntrust {


    interface DB {
        fun save()
    }

    class SqlDB() : DB {
        override fun save() {
            println("save to sql")
        }
    }

    class GreenDaoDB() : DB {
        override fun save() {
            println("save to GreenDao")
        }
    }

    // 委托类
    // 通过by将接口实现委托给db
    class UniversalDB(db: DB) : DB by db

    fun main() {
        UniversalDB(SqlDB()).save()
        UniversalDB(GreenDaoDB()).save()

        println(data)
        val owner = Owner2()
        println(owner.normalText)
        println(owner.logText)
    }

    //委托属性 getter、setter 委托
    //标准委托  两个属性之间的直接委托、by lazy 懒加载委托、Delegates.observable 观察者委托，以及 by map 映射委托
    class Item {
        var count: Int = 0

        //::count 是属性的引用 类似函数引用
        // by 代表 total 属性的 getter、setter 会被委托出去
        // 软件版本之间的兼容 total count 完全一样
        // 假设 Item 是服务端接口的返回数据，1.0 版本的时候，我们的 Item 当中只 count 这一个变量
        val total: Int by ::count
    }

    val data: String by lazy {
        request()
    }

    fun request(): String {
        println("执行网络请求")
        return "网络数据"
    }

    //自定义委托
    class StringDelegate(var s: String = "Hello") {
        //     ①                           ②                              ③
        //     ↓                            ↓                               ↓
        operator fun getValue(thisRef: Owner, property: KProperty<*>): String {
            return s
        }

        //      ①                          ②                                     ③
        //      ↓                           ↓                                      ↓
        operator fun setValue(thisRef: Owner, property: KProperty<*>, value: String) {
            s = value
        }
    }

    //      ②
    //      ↓
    class Owner {
        //               ③
        // ¬              ↓
        var text: String by StringDelegate()
    }

    //优化版本

    //val
    fun interface ReadOnlyProperty<in T, out V> {
        public operator fun getValue(thisRef: T, property: KProperty<*>): V
    }

    //var
    interface ReadWriteProperty<in T, V> : ReadOnlyProperty<T, V> {
        public override operator fun getValue(thisRef: T, property: KProperty<*>): V

        public operator fun setValue(thisRef: T, property: KProperty<*>, value: V)
    }

    class StringDelegate2(private var s: String = "Hello") : ReadWriteProperty<Owner2, String> {
        override fun getValue(thisRef: Owner2, property: KProperty<*>): String {
            return s
        }

        override fun setValue(thisRef: Owner2, property: KProperty<*>, value: String) {
            s = value
        }

    }

    //提供委托（provideDelegate）
    class SmartDelegator {
        operator fun provideDelegate(
            thisRef: Owner2,
            prop: KProperty<*>
        ): ReadWriteProperty<Owner2, String> {
            return if (prop.name.contains("log")) {
                StringDelegate2("log")
            } else {
                StringDelegate2("normal")
            }
        }
    }

    class Owner2 {
        var normalText: String by SmartDelegator()
        var logText: String by SmartDelegator()
    }

    //案例 1：属性可见性封装 外部只可读 不可写
    class Model {
        var data: String = ""
            private set

        private fun load() {
            data = "123"
        }
    }

    class Model2 {
        val data: MutableList<String> = mutableListOf()
        private fun load() {
            // 网络请求
            data.add("Hello")
        }
    }

    fun main2() {
        val model = Model2()
        // 类的外部仍然可以修改data
        model.data.add("World")


    }

    class Model3 {
        //不可修改的 List，它是没有 add、remove
        val data: List<String> by ::_data
        //MutableList，这是 Kotlin 当中的可变集合，它是有 add、remove 方法的
        private val _data: MutableList<String> = mutableListOf()
        private fun load() {
            // 网络请求
            _data.add("Hello")
        }
    }
    //案例 2：数据与 View 的绑定

    operator fun TextView.provideDelegate(value: Any?, property: KProperty<*>) = object : ReadWriteProperty<Any?, String?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? = text.toString()
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            text = value
        }
    }


//    val textView : TextView
//
//    // ① 将 message 委托给了 textView。这意味着，message 的 getter 和 setter 都将与 TextView 关联到一起
//    var message: String? by textView
//
//    // ②
//    textView.text = "Hello"
//    println(message)
//
//// ③
//    message = "World"
//    println(textView.text)


//    结果：
//    Hello
//    World
    //案例 3：ViewModel 委托

//    private val mainViewModel: MainViewModel by viewModels()

    //委托类，委托的是接口的方法
    //委托属性，委托的是属性的 getter、setter
    //自定义委托，我们需要遵循 Kotlin 提供的一套语法规范
    //在自定义委托的时候，如果我们有灵活的需求时，可以使用 provideDelegate 来动态调整委托逻辑。
}
