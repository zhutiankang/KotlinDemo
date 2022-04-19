package base.calculator

import java.lang.StringBuilder
import kotlin.system.exitProcess

/**
 * Two
 *
 * @author tiankang
 * @description:
 * @date :2022/1/24 15:45
 */
class Three {

    val exit = "exit"
    val help = """
        --------------------------------------
        使用说明：
        1. 输入 1 + 1，按回车，即可使用计算器；
        2. 注意：数字与符号之间要有空格；
        3. 想要退出程序，请输入：exit
        --------------------------------------
    """.trimIndent()

    fun start() {
        while (true) {
            println(help)
            val input = readLine() ?: continue
            val result = calculate(input)
            if (result == null) {
                println("输入格式不对")
                continue
            } else {
                println("$input = $result")
            }
        }
    }

    fun calculate(input: String): String? {
        if (shouldExit(input)) exitProcess(0)
        val exp = parseException(input) ?: return null

//        val left = exp.left
//        val operator = exp.operator
//        val right = exp.right
        val(left, operator, right) = exp

        return when (operator) {
            Operation.ADD -> addString(left,right)
            Operation.MINUS -> minusString(left,right)
            Operation.MULTI -> multiString(left,right)
            Operation.DIVI -> diviString(left,right)
        }
    }

    private fun addString(leftNum: String, rightNum: String): String {
//        val result = left.toInt() + right.toInt()
//        return result.toString()
        //大数加法
        val result = StringBuilder()
        var leftIndex = leftNum.length - 1
        var rightIndex = rightNum.length - 1
        var carry = 0

        while (leftIndex >= 0 || rightIndex >= 0) {
            val leftVal = if (leftIndex >= 0) leftNum[leftIndex].digitToInt() else 0
            val rightVal = if (rightIndex >= 0) rightNum[rightIndex].digitToInt() else 0
            val sum = leftVal + rightVal + carry
            carry = sum / 10
            result.append(sum % 10)
            leftIndex--
            rightIndex--
        }
        // 99+1的情况
        if (carry != 0) {
            result.append(carry)
        }

        return result.reverse().toString()
    }
    private fun minusString(left: String, right: String): String {
        val result = left.toInt() - right.toInt()
        return result.toString()
    }
    private fun multiString(left: String, right: String): String {
        val result = left.toInt() * right.toInt()
        return result.toString()
    }
    private fun diviString(left: String, right: String): String {
        val result = left.toInt() / right.toInt()
        return result.toString()
    }

    private fun shouldExit(input: String): Boolean {
        return input == exit
    }

    private fun parseException(input: String): Expression? {
        val operation = parseOperator(input) ?: return null
        val list = input.split(operation.value)
        if (list.size != 2) return null
        return Expression(
            left = list[0].trim(),
            operator = operation,
            right = list[1].trim()
        )
    }

    private fun parseOperator(input: String): Operation? {
        Operation.values().forEach {
            if (input.contains(it.value)) {
                return it
            }
        }
        return null
    }


    private fun parseOperator1(input: String): Operation? {
        return when {
            input.contains(Operation.ADD.value) -> Operation.ADD
            input.contains(Operation.MINUS.value) -> Operation.MINUS
            input.contains(Operation.MULTI.value) -> Operation.MULTI
            input.contains(Operation.DIVI.value) -> Operation.DIVI
            else -> null
        }
    }
}



fun main() {
    val calculator = Two()
    calculator.start()
}