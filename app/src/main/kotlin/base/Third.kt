package base

/**
 * Third
 *
 * @author tiankang
 * @description:
 * @date :2022/1/19 14:12
 */
class Third {
    //研究kotlin代码

    fun main() {
        println("Hello World")
    }

    class Person(val name: String, var age: Int) {
        val isAdult
            get() = age >= 18
//        val isAdult = age >= 18
    }
}