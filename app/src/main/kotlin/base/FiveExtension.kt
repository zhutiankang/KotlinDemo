package base

/**
 * author : tiankang
 * date : 2022/4/21 8:09 下午
 * desc : 最主要的用途，就是用来取代 Java 当中的各种工具类，比如 StringUtils、DateUtils 等等
 */
class FiveExtension {

    private val msg: String = ""

    // 普通函数变成扩展函数 本质上还是静态方法
    fun String.lastElement(): Char? {
        if (this.isEmpty()) {
            println(msg)
            return null
        }
        return this[length - 1]
    }

    //扩展在语义上更适合作为函数还是属性就够了
    private val String.lastElement: Char?
        get() = if (isEmpty()) {
            null
        } else {
            get(length - 1)
        }

    fun main() {
        val msg = "Hello World"
        val last = msg.lastElement
    }

    //扩展限制 1.不是真正的类成员2.扩展属性无法存储状态3.访问作用域定义处成员 公开成员
    //扩展无法被重写；扩展属性无法存储状态；扩展的作用域有限，无法访问私有成员
    open class Person {
        var name: String = ""
        var age: Int = 0
    }

    class Helper {
        private fun walkOnFoot() {
            println("walk")
        }

        val Person.isAdult: Boolean
            get() = age >= 18

        fun Person.walk() {
            walkOnFoot()
        }

        fun test() {
            val person = Person()
            person.walk()
        }
    }
    //第一个典型使用场景：关注点分离。所谓关注点分离，就是将我们程序的逻辑划分成不同的部分，每一个部分，都只关注自己那部分的职责
    //上面的 String 类为例，String.kt这个类，只关注 String 的核心逻辑；而Strings.kt则只关注 String 的操作符逻辑
    //主动使用扩展，通过它来优化软件架构
    //被动使用扩展，提升可读性与开发效率
    //关注点分离，优化代码架构；消灭模板代码，提高可读性和开发效率
    //1、调用 String接受者
    //fun String.lastElement(): Char? {}：只能是不可null的String才能调用
    //fun String?.lastElement(): Char? {}：可null String和不可null String都能调用
    //2、实现
    //fun String.lastElement(): Char? {}：返回值为null，只有length == 0
    //fun String?.lastElement(): Char? {}：返回值为null，this == null 或者 length == 0
    /**
     *
     */



}