package com.github.kotlin.network.https.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

/**
 * LongDefault0Adapter
 *
 * @author tiankang
 * @description: 定义为long类型,如果后台返回""或者null,则返回0
 * @date :2023/6/12 16:52
 */
class LongDefault0Adapter : JsonSerializer<Long?>,
    JsonDeserializer<Long?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Long {
        try {
            if (json.asString.equals("") || json.asString.equals("null")) {
                return 0L
            }
        } catch (ignore: Exception) {
        }
        return try {
            json.asLong
        } catch (e: NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    override fun serialize(
        src: Long?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src)
    }
}