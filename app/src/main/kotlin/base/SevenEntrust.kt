package base

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
    class StringDelegate(private var s: String = "Hello") {
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

}
