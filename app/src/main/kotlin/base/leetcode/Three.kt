package base.leetcode

/**
 * Three
 *
 * @author tiankang
 * @description: 方程求解
 * @date :2022/8/9 16:45
 */


fun solveEquation(equation: String): String {
    // ① 分割等号
    val list = equation.split("=")
    // ② 遍历左边的等式，移项，合并同类项
    // ③ 遍历右边的等式，移项，合并同类项

    var leftSum = 0
    var rightSum = 0

    val leftList = splitByOperator(list[0])
    val rightList = splitByOperator(list[1])

// ② 遍历左边的等式，移项，合并同类项
    leftList.forEach {
        if (it.contains("x")) {
            leftSum += xToInt(it)
        } else {
            rightSum -= it.toInt()
        }
    }

// ③ 遍历右边的等式，移项，合并同类项
    rightList.forEach {
        if (it.contains("x")) {
            leftSum -= xToInt(it)
        } else {
            rightSum += it.toInt()
        }
    }
    // ④ 系数化为一，返回结果
    return when {
        leftSum == 0 && rightSum == 0 -> "Infinite solutions"
        leftSum == 0 && rightSum != 0 -> "No solution"
        else -> "x=${rightSum / leftSum}"
    }
}


private fun splitByOperator(list: String): List<String> {
    val result = mutableListOf<String>()
    var temp = ""
    list.forEach {
        if (it == '+' || it == '-') {
            if (temp.isNotEmpty()) {
                result.add(temp)
            }
            temp = it.toString()
        } else {
            temp += it
        }
    }

    result.add(temp)
    return result
}

private fun xToInt(x: String) =
    when (x) {
        "x",
        "+x" -> 1
        "-x" -> -1
        else -> x.replace("x", "").toInt()
    }