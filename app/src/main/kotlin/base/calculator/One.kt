package base.calculator

/**
 * One
 *
 * @author tiankang
 * @description:
 * @date :2022/1/22 10:58
 */
class One {

    val help = """
        --------------------------------------
        使用说明：
        1. 输入 1 + 1，按回车，即可使用计算器；
        2. 注意：数字与符号之间要有空格；
        3. 想要退出程序，请输入：exit
        --------------------------------------
    """.trimIndent()


    fun main() {

        while (true) {
            // 初始化，打印提示信息
            println(help)

            // 第一步，读取输入命令；
            val input = readLine() ?: continue
            // 第二步，判断命令是不是exit，如果是则直接退出程序；
            if (input == "exit") continue

            // 第三步，解析算式，分解出“数字”与“操作符”：“1”“+”“2”；
            val inputList = input.split(" ")
            // 第四步，根据操作符类型，算出结果：3；
            val result = calculator(inputList)

            // 第五步，输出结果：1 + 2 = 3；
            if (result == null) {
                println("输入格式不对")
                return
            } else {
                println("$input = $result")
            }

            // 第六步，进入下一个while循环。
        }
    }

    private fun calculator(inputList: List<String>): Int? {

        if (inputList.size != 3) return null
        // 第七步，取出数字和操作符
        val left = inputList[0].toInt()
        val operation = Operation.valueOf(inputList[1])
        val right = inputList[2].toInt()

        // 第八步，根据操作符的类型，执行计算
        // 使用 when 表达式的时候，应该尽量结合“枚举”或者“密封类”来使用
        return when (operation) {
            Operation.ADD -> left + right
            Operation.MINUS -> left - right
            Operation.MULTI -> left * right
            Operation.DIVI -> left / right
        }
    }

    enum class Operation(val value:String) {
        ADD("+"),
        MINUS("-"),
        MULTI("*"),
        DIVI("/")
    }

}