package base.leetcode

/**
 * One
 *
 * @author tiankang
 * @description: 怎么爽就怎么来
 * @date :2022/8/8 17:38
 */

// 程序的输入是一个字符串 s。题目要求我们移除当中的所有元音字母 a、e、i、o、u，然后返回。
fun removeVowels(s: String): String = s.filter {
    it !in setOf('a', 'e', 'i', 'o')
}

//程序的输入是一段英语文本（paragraph），一个禁用单词列表（banned）返回出现次数最多、同时不在禁用列表中的单词
fun mostCommonWord1(paragraph: String, banned: Array<String>) =
    paragraph.toLowerCase()//非 清理符号
        .replace("[^a-zA-Z ]".toRegex(), " ")
        .split("\\s+".toRegex())
        .filter {
            it !in banned.toSet()
        }
        .groupBy { it }
        .mapValues { it.value.size }
        .maxByOrNull {
            it.value
        }
        ?.key ?: throw IllegalArgumentException()

// 冒泡排序 因为 Kotlin 当中的 Range 要求必须是右边不能小于左边，比如“1…3”是可以的，而“3…1”是不行的
fun sort(array: IntArray): IntArray {
    for (end in (array.size - 1) downTo 1) {
        for (begin in 1..end) {
            if (array[begin - 1] > array[begin]) {
                val temp = array[begin - 1]
                array[begin - 1] = array[begin]
                array[begin] = temp
            }
        }
    }
    return array
}