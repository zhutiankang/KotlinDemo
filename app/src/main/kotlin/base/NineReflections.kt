package base

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

/**
 * base.NineReflections
 *
 * @author tiankang
 * @description: 注解与反射 灵活性
 * @date :2022/7/5 17:25
 */
class NineReflections {

    //注解 对已有数据进行补充的一种数据 Kotlin 当中的注解，其实就是“程序代码的一种补充”

    //注解，其实就是“程序代码的一种补充”，而反射，其实就是“程序代码自我反省的一种方式”

    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY,
    )
    @MustBeDocumented
    public annotation class Deprecated(
        val message: String,
        val replaceWith: ReplaceWith = ReplaceWith(""),
        val level: DeprecationLevel = DeprecationLevel.WARNING
    )


    @Deprecated(
        message = "Use CalculatorV3 instead.",
        replaceWith = ReplaceWith("CalculatorV3"),
        level = DeprecationLevel.ERROR
    )
    class Calculator {
        // 错误逻辑
        fun add(a: Int, b: Int): Int = a - b
    }

    class CalculatorV3 {
        // 正确逻辑
        fun add(a: Int, b: Int): Int = a + b
    }

    //注解的精确使用目标 明确标记出注解的精确使用目标
    object Singleton {
        //    ①
//    ↓
//        @set:Inject
//        lateinit var person: Person
//     ↑
//     ②
    }
//    实际上，注解的精确使用目标，一般是和注解一起使用的，在上面的例子当中，set 就是和 @Inject 一起使用的。而除了 set 以外，Kotlin 当中还有其他的使用目标：
//    file，作用于文件；
//    property，作用于属性；
//    field，作用于字段；
//    get，作用于属性 getter；
//    set，作用于属性 setter；
//    receiver，作用于扩展的接受者参数；
//    param，作用于构造函数参数；
//    setparam，作用于函数参数；
//    delegate，作用于委托字段。

    //反射

    fun main() {
        val student = Student("Tom", 99.5, 170)
        val school = School("PKU", "Beijing...")
        // 修改其中的address属性
        modifyAddressMember(school)
        readMembers(student)
        readMembers(school)
    }

    fun readMembers(obj: Any) {
        // 读取obj的所有成员属性的名称和值
        obj::class.memberProperties.forEach {
            println("${obj::class.simpleName}.${it.name}=${it.getter.call(obj)}")
        }
    }

    data class Student(
        val name: String,
        val score: Double,
        val height: Int
    )

    data class School(
        val name: String,
        var address: String
    )

// 要求readMembers函数能够输出以下内容：

//    Student.height=170
//    Student.name=Tom
//    Student.score=99.5
//    School.address=Beijing...
//    School.name=PKU

//  如果传入的参数当中，存在 String 类型的 address 变量，我们就将其改为 China
    fun modifyAddressMember(obj: Any) {
        obj::class.memberProperties.forEach {
            if (it.name == "address" && // ① 名称是否为 address
                it is KMutableProperty1 && // ② 属性是否可变
                it.setter.parameters.size == 2 && // ③ setter 的参数是否符合预期 参数个数应该是 2
                it.getter.returnType.classifier == String::class // ④ 返回值类型
            ) {
                // ⑤
                it.setter.call(obj, "China")
                println("====Address changed.====")
            }else {
            // 差别在这里
             println("====Wrong type.====")
             }
            }
        }
//    第一种情况，程序在运行的时候，可以通过反射来查看自身的状态。可以修改自身的状态,可以根据自身的状态调整自身的行为


/*// 运行结果：
    Student.height=170
    Student.name=Tom
    Student.score=99.5
// 注意这里
    School.address=Beijing...
    School.name=PKU
    ====Address changed.====
    ====Wrong type.==== // 差别在这里
 */
// 注意这里
//    School.address=China
//    School.name=PKU
//    Student.height=170
//    Student.name=Tom
//    Student.score=99.5*/
}