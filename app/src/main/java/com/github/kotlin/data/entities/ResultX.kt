package com.github.kotlin.data.entities

/**
 * ResultX
 *
 * @author tiankang
 * @description: 利用密封类和泛型，将数据的成功、失败、加载中都统一了起来 密封相比枚举优势是巨大，首先就是可以不同类型的入参
 * @date :2022/12/5 16:07
 * Sealed 类（密封类）用于对类可能创建的子类进行限制，用 Sealed 修饰的类的直接子类只允许被定义在 Sealed 类所在的文件中（密封类的间接继承者可以定义在其他文件中），这有助于帮助开发者掌握父类与子类之间的变动关系，避免由于代码更迭导致的潜在 bug，
 * 且密封类的构造函数只能是 private 的
 * 因为 Sealed 类的子类对于编译器来说是可控的，所以如果在 when 表达式中处理了所有 Sealed 类的子类，那就不需要再提供 else 默认分支。即使以后由于业务变动又新增了 View 子类，编译器也会检测到 check 方法缺少分支检查后报错，所以说 check 方法是类型安全的
 */
sealed class ResultX<out R : Any> {

    data class Success<out T : Any>(val data: T) : ResultX<T>()
    data class Error(val exception: Exception) : ResultX<Nothing>()
    object Loading : ResultX<Nothing>()


    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}
