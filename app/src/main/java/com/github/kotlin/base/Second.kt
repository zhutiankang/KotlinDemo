package com.github.kotlin.base

import com.github.kotlin.base.Second.Human.*

/**
 * Second
 *
 * @author tiankang
 * @description:
 * @date :2022/1/18 11:14
 */
class Second {


    class Person(val name: String, var age: Int) {
        val isAdult
            get() = age >= 18

        val isAdult2: Boolean
            get() {
                // do something else
                return age >= 18
            }
        var age2: Int = 0
            private set(value) {
                //log()
                val b = "a"
                field = value + b.toInt()
            }
    }

    //抽象类 && 标记为open的类或者方法 才可以被继承或者重写
    abstract class Cat(val color: String) {
        abstract fun eat()
    }

    class WhiteCat(color: String) : Cat(color) {
        override fun eat() {
            TODO("Not yet implemented")
        }
    }

    open class Dog() {
        open fun walk() {}
        val canEat: Boolean = false
        fun eat() {}
    }

    class WhiteDog : Dog() {
        override fun walk() {
            super.walk()
        }
    }

    // 接口可以有默认实现，也可以有属性
    interface Behavior {
        val canWalk: Boolean

        fun walk() {
            if (canWalk) {
                // do
            }
        }
    }

    class girl(val name: String) : Behavior {

        override val canWalk: Boolean
            get() = true

        override fun walk() {
            TODO("Not yet implemented")
        }
    }

    //默认静态内部类,避免内存泄露，不可访问主类成员，加上inner 关键字可以访问
    class A {
        val name: String = ""
        fun foo() = 1

        inner class B {
            val a = name
            val b = foo()
        }
    }

    // 数据类
    data class Man(val name: String, val age: Int)

    fun main() {
        val tom = Man("Tom", 18)
        val jack = Man("Jack", 19)
        println(tom.equals(jack))
        println(tom.hashCode())
        println(tom.toString())

        //变量赋值
        val (name, age) = tom
        println("name is $name, age is $age")

        val mike = tom.copy(name = "Mike")
        println(mike)

        println(Human.MAN == Human.MAN)
        println(Human.MAN === Human.MAN)
    }

    //密封类 更强大的枚举类
    //每一个枚举的值，它在内存当中始终都是同一个对象引用
    enum class Human {
        MAN, WOMAN
    }

    fun isMan(data: Human) = when (data) {
        MAN -> true
        WOMAN -> false
        // 这里不需要else分支，编译器自动推导出逻辑已完备
    }

    //密封类 不一样的对象引用
    sealed class Result<out R> {
        data class Success<out T>(val data: T, val message: String = "") : Result<T>()
        data class Error<out T>(val exception: Exception) : Result<Nothing>()
        data class Loading<out T>(val time: Long = System.currentTimeMillis()) : Result<Nothing>()
    }

    fun display(data:Result<Man>) = when(data) {
        is Result.Success -> displaySuccessUI(data)
        is Result.Error<*> -> showErrorMsg(data)
        is Result.Loading<*> -> showLoading()
    }

    private fun showLoading() {
        TODO("Not yet implemented")
    }

    private fun showErrorMsg(data: Result<Man>) {

    }

    private fun displaySuccessUI(data: Result<Man>) {

    }
}