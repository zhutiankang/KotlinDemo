package base.processor

import java.io.File

/**
 * ProcessorV1
 *
 * @author tiankang
 * @description: 实现频率统计基本功能，使用“命令式风格”的代码
 * @date :2022/4/24 11:21
 */
class ProcessorV1 {

    fun processText(text: String): List<WordFreq> {

        // 1.文本清洗
        val cleaned = clean(text)
        // 2.文本分割
        val words = cleaned.split(" ")
        // 3.统计单词频率
        val map = getWordCount(words)
        // 4.词频排序
        val list = sortByFrequency(map)

        return list
    }

    fun clean(text: String): String {
        return text.replace("[^A-Za-z]".toRegex(), " ").trim()
    }

    fun getWordCount(list: List<String>): Map<String, Int> {
        val map = hashMapOf<String, Int>()
        for (word in list) {
            if (word == "") continue
            val trim = word.trim()
            val count = map.getOrDefault(trim, 0)
            map[trim] = count + 1
        }
        return map
    }

    fun sortByFrequency(map: Map<String, Int>): MutableList<WordFreq> {

        val list = mutableListOf<WordFreq>()
        for (entry in map) {
            if (entry.key == "") continue
            val freq = WordFreq(entry.key, entry.value)
            list.add(freq)
        }
        list.sortByDescending {
            it.frequency
        }
        return list
    }

    fun processFile(file: File): List<WordFreq> {
        val text = file.readText(Charsets.UTF_8)
        return processText(text)
    }
}


data class WordFreq(val word: String, val frequency: Int)