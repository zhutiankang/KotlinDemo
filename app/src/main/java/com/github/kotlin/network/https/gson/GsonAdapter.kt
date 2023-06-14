package com.github.kotlin.network.https.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * GsonAdapter
 *
 * @author tiankang
 * @description:
 * @date :2023/6/12 17:42
 */
object GsonAdapter {

    fun buildGson1(): Gson? {
        return GsonBuilder()
            //Integer.class
            .registerTypeAdapter(Int::class.java, IntegerDefault0Adapter())
            .registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDefault0Adapter())
            .registerTypeAdapter(Double::class.java, DoubleDefault0Adapter())
            .registerTypeAdapter(Double::class.javaPrimitiveType, DoubleDefault0Adapter())
            .registerTypeAdapter(Long::class.java, LongDefault0Adapter())
            .registerTypeAdapter(Long::class.javaPrimitiveType, LongDefault0Adapter())
            .create()
    }

    fun buildGson(): Gson = GsonBuilder().run {
        registerTypeAdapter(Int::class.java, IntegerDefault0Adapter())
        registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDefault0Adapter())
        registerTypeAdapter(Double::class.java, DoubleDefault0Adapter())
        registerTypeAdapter(Double::class.javaPrimitiveType, DoubleDefault0Adapter())
        registerTypeAdapter(Long::class.java, LongDefault0Adapter())
        registerTypeAdapter(Long::class.javaPrimitiveType, LongDefault0Adapter())
        create()
    }

}