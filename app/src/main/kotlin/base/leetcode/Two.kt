package base.leetcode

/**
 * Two
 *
 * @author tiankang
 * @description: 版本号对比
 * @date :2022/8/9 16:12
 */
// 思路一，代码逻辑比较清晰，代码量小，时间复杂度、空间复杂度较差。
// 思路二，代码逻辑比较复杂，代码量稍大，时间复杂度、空间复杂度非常好。
// 思路三，代码主逻辑非常清晰，代码量大，时间复杂度、空间复杂度较差

fun compareVersion(version1: String, version2: String): Int {
    // ① 使用“.”，分割 version1 和 version2，得到list1、list2
    val list1 = version1.split(".")
    val list2 = version2.split(".")
    // ② 同时遍历list1、list2，取出的元素v1、v2，并将其转换成整数，这里注意补零操作
    var i = 0
    while (i < list1.size || i < list2.size) {
        // getOrNull(i)，这是 Kotlin 独有的库函数。使用这个方法，我们不必担心越界问题
        val v1 = list1.getOrNull(i)?.toInt() ?: 0
        val v2 = list2.getOrNull(i)?.toInt() ?: 0
        // ③ 对比v1、v2的大小，如果它们两者不一样，我们就可以终止流程，直接返回结果。
        if (v1 != v2) {
            return v1.compareTo(v2)
        }
        i++
    }
    // ④ 当遍历完list1、list2后仍然没有判断出大小话，说明两个版本号其实是相等的，这时候应该返回0
    return 0
}

// 双指针思想
fun compareVersion2(version1: String, version2: String): Int {
    val length1 = version1.length
    val length2 = version2.length

    // ①
    var i = 0
    var j = 0
    // ②
    while (i < length1 || j < length2) {
        // ③
        var x = 0
        while (i < length1 && version1[i] != '.') {
            x = x * 10 + version1[i].toInt() - '0'.toInt()
            i++
        }
        i++

        // ④
        var y = 0
        while (j < length2 && version2[j] != '.') {
            y = y * 10 + version2[j].toInt() - '0'.toInt()
            j++
        }
        j++

        // ⑤
        if (x != y) {
            return x.compareTo(y)
        }
    }
    // ⑥
    return 0
}

// 函数式思路
fun compareVersion3(version1: String, version2: String): Int =
    version1.split(".")
        .zipLongest(version2.split("."), "0")
        .onEach {
            with(it) {
                if (first != second) {
                    return first.compareTo(second)
                }
            }
        }.run {
            return 0
        }



private fun Iterable<String>.zipLongest(
    other: Iterable<String>,
    default: String
): List<Pair<Int, Int>> {
    val first = iterator()
    val second = other.iterator()
    val list = ArrayList<Pair<Int, Int>>(minOf(collectionSizeOrDefault(10), other.collectionSizeOrDefault(10)))
    while (first.hasNext() || second.hasNext()) {
        val v1 = (first.nextOrNull() ?: default).toInt()
        val v2 = (second.nextOrNull() ?: default).toInt()
        list.add(Pair(v1, v2))
    }
    return list
}

private fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int =
    if (this is Collection<*>) this.size else default

private fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

// Pair 是Kotlin标准库提供的一个数据类
// 专门用于存储两个成员的数据
// 提交代码的时候，Pair不需要拷贝进去
//public data class Pair<out A, out B>(
//    public val first: A,
//    public val second: B
//) : Serializable {
//    public override fun toString(): String = "($first, $second)"
//}



//                                                        注意这里
//                                                           ↓
inline fun <T, C : Iterable<T>> C.onEachWithReceiver(action: T.() -> Unit): C {
    return apply { for (element in this) action(element) }
}

//                                                   注意这里
// Kotlin库函数当中的onEach                                ↓
public inline fun <T, C : Iterable<T>> C.onEach(action: (T) -> Unit): C {
    return apply { for (element in this) action(element) }
}


fun compareVersion4(version1: String, version2: String): Int =
    version1.split(".")
        .zipLongest(version2.split("."), "0")
        .onEachWithReceiver {
            // 减少了一层嵌套
            if (first != second) {
                return first.compareTo(second)
            }
        }.run { return 0 }