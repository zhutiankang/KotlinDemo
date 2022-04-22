package base

import android.view.View
/**
 * author : tiankang
 * date : 2022/4/21 8:51 下午
 * desc : 一个是针对定义方，代码中减少了两个接口类的定义；另一个是对于调用方来说，代码也会更加简洁。这样一来，
 *        就大大减少了代码量，提高了代码可读性，并通过减少类的数量，提高了代码的性能
 *        而它明确的定义其实是这样的：高阶函数是将函数用作参数或返回值的函数。
 *        一个函数的参数或是返回值，它们当中有一个是函数的情况下，这个函数就是高阶函数
 *
 *        为什么引入高阶函数？答：为了简化。
 *        高阶函数是什么？答：函数作为参数 or 返回值   JVM匿名内部类
 *        函数类型是什么？答：函数的类型。
 *        函数引用是什么？答：类比变量的引用 ::
 *        Lambda 是什么？答：可以简单理解为“函数的简写”
 *        带接收者的函数类型是什么？答：可以简单理解为“成员函数的类型
 */
class SixHighFunction {

    val imageView: View
        get() {
            TODO()
        }

    //(View) -> Unit 就代表了参数类型是 View，返回值类型为 Unit 的函数类型。
    //Lambda 就是函数的一种简写
    fun main() {

        //1
        imageView.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                gotoPreview(v)
            }
        })
        //2
        imageView.setOnClickListener( View.OnClickListener { v: View? -> gotoPreview(v) })
        //3由于 Kotlin 的 Lambda 表达式是不需要 SAM Constructor 的，所以它也可以被删掉：
        imageView.setOnClickListener({v: View? -> gotoPreview(v)})
        //4由于 Kotlin 支持类型推导，所以 View 可以被删掉：
        imageView.setOnClickListener( {v -> gotoPreview(v)} )
        //5当 Kotlin Lambda 表达式只有一个参数的时候，它可以被写成 it：
        imageView.setOnClickListener( {it -> gotoPreview(it)} )
        //6Kotlin Lambda 的 it 是可以被省略的：
        imageView.setOnClickListener( {gotoPreview(it)} )
        //7当 Kotlin Lambda 作为函数的最后一个参数时，Lambda 可以被挪到外面：
        imageView.setOnClickListener(){
            gotoPreview(it)
        }
        //8当 Kotlin 只有一个 Lambda 作为函数参数时，() 可以被省略：
        imageView.setOnClickListener { gotoPreview(it) }

        val user = User("Tom","www.blog.com")

        user.apply {
            val name1 = name
            val text = blog
            imageView.setOnClickListener {
                gotoImage(this)
            }
        }
    }

    fun gotoPreview(view:View?){
    }
    fun gotoImage(user:User){

    }
    //带接收者的函数类型 User.() -> Unit带接收者的函数类型，就等价于成员方法
    data class User(var name:String,var blog:String)

    fun User.apply(block:User.() -> Unit):User{
        block()
        return this;
    }

//    User.() -> Unit带接收者的函数类型，就等价于成员方法 代表扩展函数
//    class User{
//        fun apply(){}
//    }

    //将一个方法改成属性 从函数的语法变成了属性的语法，语法从复杂变得简洁，其中的关键元素并未丢失
//    abstract class BaseSingleton<in P, out T> {
//
//        @Volatile
//        private var instance: T? = null
//
//        protected abstract fun creator(param: P): T
//
//        fun getInstance(param: P): T =
//            instance ?: synchronized(this) {
//                instance ?: creator(param).also { instance = it }
//            }
//    }
//
//    class PersonManager private constructor(name: String) {
//        companion object : BaseSingleton<String, PersonManager>() {
//            override fun creator(param: String): PersonManager = PersonManager(param)
//        }
//    }

    abstract class BaseSingleton<in P, out T> {

        @Volatile
        private var instance: T? = null

        //将 creator 改成一个类型为：(P)->T的属性 函数类型的属性
        protected abstract val creator:(P)->T

        fun getInstance(param: P): T =
            instance ?: synchronized(this) {
                instance ?: creator(param).also { instance = it }
            }

    }

    class PersonManager private constructor(name: String) {
        companion object : BaseSingleton<String, PersonManager>() {
            override val creator: (String) -> PersonManager
                get() = ::PersonManager
            //函数引用::
//            override val creator = ::PersonManager
        }
    }


}

