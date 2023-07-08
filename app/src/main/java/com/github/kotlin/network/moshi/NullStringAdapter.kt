package com.github.kotlin.network.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson


/**
 * NullStringAdapter
 * JSON解析框架moshi额外配置规则
 * @author tiankang
 * @description: 防止服务端返回的JSON字段是null（{“data:”:null,"code":200,"message":"ok"}) 导致程序出错崩溃
 * 正常情况下如果是不返回某个字段，会自动填充默认值
 * @date :2022/12/5 18:19
 */
object NullStringAdapter {

    //服务器返回null，做一层兼容，替换成默认值""空字符串
    @FromJson
    fun fromJson(reader: JsonReader): String {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextString()
        }
        reader.nextNull<Unit>()
        return ""
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: String?) {
        writer.value(value)
    }
}