package base

import android.view.View
/**
 * author : tiankang
 * date : 2022/4/21 8:51 下午
 * desc : 一个是针对定义方，代码中减少了两个接口类的定义；另一个是对于调用方来说，代码也会更加简洁。这样一来，
 *        就大大减少了代码量，提高了代码可读性，并通过减少类的数量，提高了代码的性能
 *        而它明确的定义其实是这样的：高阶函数是将函数用作参数或返回值的函数。
 *        一个函数的参数或是返回值，它们当中有一个是函数的情况下，这个函数就是高阶函数
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
        imageView.setOnClickListener() {object:View.OnClickListener{
            override fun onClick(v: View?) {
                gotoPreview(v)
            }
        }}
        //2
        imageView.setOnClickListener() { View.OnClickListener { v: View? -> gotoPreview(v) } }
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
    }

    fun gotoPreview(view:View?){
    }
    //带接收者的函数类型
}

