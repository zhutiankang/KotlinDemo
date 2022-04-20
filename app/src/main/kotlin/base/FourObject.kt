package base

/**
 * author : tiankang
 * date : 2022/4/20 8:53 下午
 * desc : object单例模式
 */
class FourObject {


    interface A {
        fun findA()
    }

    interface B {
        fun findB()
    }

    abstract class Man {
        abstract fun findMan()
    }

    fun main() {
        //这个匿名内部类 在继承了Man类的同时，还实现了A，B接口
        val item = object : Man(), A, B {
            override fun findA() {
                // do something
                TODO("Not yet implemented")
            }

            override fun findB() {
                TODO("Not yet implemented")
            }

            override fun findMan() {
                TODO("Not yet implemented")
            }

        }
    }

    //普通单例 不支持懒加载 不支持传参构造单例
    //单例占用内存很小，并且对内存不敏感，不需要传参
    object UserManager {
        fun login() {}
    }

    //嵌套对象 演化伴生对象
    class Person {
        object InnerSingleton {
            @JvmStatic
            fun foo() {
            }
        }
    }

    fun main2() {
        Person.InnerSingleton.foo()
        Person2.foo()

        User.create("Tom")
        UserManager3.getInstance("Tom")
        PersonManager.getInstance("Tom")
    }

    //伴生对象 静态方法变量
    class Person2 {
        companion object InnerSingleton {
            @JvmStatic
            fun foo() {
            }
        }
    }

    //工厂模式
    //私有的构造函数，外部无法调用
    class User private constructor(name: String) {
        companion object {
            @JvmStatic
            fun create(name: String): User? {
                // 统一检查，比如敏感词过滤
                return User(name)
            }
        }
    }

    //懒加载委托
    //单例占用内存很小，不需要传参，但它内部的属性会触发消耗资源的网络请求和数据库查询
    object UserManager2 {
        //对外暴露的user
        val user by lazy { loadUser() }
        private fun loadUser(): User? {
            //从网络或者数据库加载数据
            return User.create("Tom")
        }

        fun login() {}
    }

    //伴生对象 Double Check 方式
    //工程很简单，只有一两个单例场景，同时我们有懒加载需求，并且 getInstance() 需要传参
    class UserManager3 private constructor(name: String) {
        companion object {
            @Volatile
            private var INSTANCE: UserManager3? = null

            fun getInstance(name: String): UserManager3 =
                //第一次判空
                INSTANCE ?: synchronized(this) {
                    //第二次判空
                    INSTANCE ?: UserManager3(name).also { INSTANCE = it }
                }
        }
    }

    //抽象模板方式
    //工程规模大，对内存敏感，单例场景比较多，那我们就很有必要使用抽象类模板 BaseSingleton 了
    abstract class BaseSingleton<in P, out T> {

        @Volatile
        private var instance: T? = null

        protected abstract fun creator(param: P): T

        fun getInstance(param: P): T =
            instance ?: synchronized(this) {
                instance ?: creator(param).also { instance = it }
            }
    }

    class PersonManager private constructor(name: String) {
        companion object : BaseSingleton<String, PersonManager>() {
            override fun creator(param: String): PersonManager = PersonManager(param)
        }
    }

    //接口模块 不推荐
    interface ISingleton<P, T> {
        //instance 无法使用 private 修饰
        //instance 无法使用 @Volatile 修饰
        var instance: T
        fun creator(param: P): T
        fun getInstance(param: P): T =
            instance ?: synchronized(this) {
                instance ?: creator(param).also { instance = it }
            }
    }
}