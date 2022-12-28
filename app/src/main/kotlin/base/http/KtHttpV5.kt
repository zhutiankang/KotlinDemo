package base.http

/**
 * KtHttpV5
 *
 * @author tiankang
 * @description:
 * 其实在实际的工作中，我们往往没有权限修改第三方提供的 SDK，那么这时候，如果想要让 SDK 获得 Flow 的能力，我们就只能借助 Kotlin 的扩展函数，为它扩展出 Flow 的能力。
 * 而对于工程内部的代码，我们希望某个功能模块获得 Flow 的能力，就可以直接修改它的源代码，让它直接支持 Flow。
 * @date :2022/12/28 17:01
 */
import base.RepoList
import base.http.annotations.Field
import base.http.annotations.GET
import com.google.gson.Gson
import com.google.gson.internal.`$Gson$Types`.getRawType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import kotlin.concurrent.thread

// https://trendings.herokuapp.com/repo?lang=java&since=weekly

interface ApiServiceV5 {
    @GET("/repo")
    fun repos(
        @Field("lang") lang: String,
        @Field("since") since: String
    ): KtCall<RepoList>

    @GET("/repo")
    fun reposSync(
        @Field("lang") lang: String,
        @Field("since") since: String
    ): RepoList


    @GET("/repo")
    fun reposFlow(
        @Field("lang") lang: String,
        @Field("since") since: String
    ): Flow<RepoList>
}

object KtHttpV5 {

    private var okHttpClient: OkHttpClient = OkHttpClient()
    private var gson: Gson = Gson()
    var baseUrl = "https://trendings.herokuapp.com"

    fun <T : Any> create(service: Class<T>): T {
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf<Class<*>>(service)
        ) { proxy, method, args ->
            val annotations = method.annotations
            for (annotation in annotations) {
                if (annotation is GET) {
                    val url = baseUrl + annotation.value
                    return@newProxyInstance invoke<T>(url, method, args!!)
                }
            }
            return@newProxyInstance null

        } as T
    }

    private fun <T : Any> invoke(path: String, method: Method, args: Array<Any>): Any? {
        if (method.parameterAnnotations.size != args.size) return null

        var url = path
        val parameterAnnotations = method.parameterAnnotations
        for (i in parameterAnnotations.indices) {
            for (parameterAnnotation in parameterAnnotations[i]) {
                if (parameterAnnotation is Field) {
                    val key = parameterAnnotation.value
                    val value = args[i].toString()
                    if (!url.contains("?")) {
                        url += "?$key=$value"
                    } else {
                        url += "&$key=$value"
                    }

                }
            }
        }

        val request = Request.Builder()
            .url(url)
            .build()

        val call = okHttpClient.newCall(request)

        return if (isKtCallReturn(method)) {
            val genericReturnType = getTypeArgument(method)
            KtCall<T>(call, gson, genericReturnType)
        } else {
            val response = okHttpClient.newCall(request).execute()

            val genericReturnType = method.genericReturnType
            val json = response.body?.string()
            gson.fromJson<Any?>(json, genericReturnType)
        }
    }

    private fun getTypeArgument(method: Method) =
        (method.genericReturnType as ParameterizedType).actualTypeArguments[0]

    private fun isKtCallReturn(method: Method) =
        getRawType(method.genericReturnType) == KtCall::class.java
}

fun <T : Any> KtCall<T>.asFlow1(): Flow<T> = callbackFlow {

    val job = launch {
        println("Coroutine start")
        delay(3000L)
        println("Coroutine end")
    }

    job.invokeOnCompletion {
        println("Coroutine completed $it")
    }

    val call = call(object : Callback<T> {
        override fun onSuccess(data: T) {
            trySendBlocking(data)
                .onSuccess { close() }
                .onFailure {
                    cancel(CancellationException("Send channel fail!", it))
                }
        }

        override fun onFail(throwable: Throwable) {
            cancel(CancellationException("Request fail!", throwable))
        }
    })

    awaitClose {
        call.cancel()
    }
}

fun <T : Any> KtCall<T>.asFlow(): Flow<T> = callbackFlow {
    val call = call(object : Callback<T> {
        override fun onSuccess(data: T) {
            trySendBlocking(data)
                .onSuccess { close() }
                .onFailure {
                    cancel(CancellationException("Send channel fail!", it))
                }
        }

        override fun onFail(throwable: Throwable) {
            cancel(CancellationException("Request fail!", throwable))
        }

    })

    awaitClose {
        call.cancel()
    }
}

fun <T : Any> KtCall<T>.asFlowTest(value: T): Flow<T> = callbackFlow {

    fun test(callback: Callback<T>) {
        thread(isDaemon = true) {
            Thread.sleep(2000L)
            callback.onSuccess(value)
        }
    }

    println("Start")
    test(object : Callback<T> {
        override fun onSuccess(data: T) {
            trySendBlocking(data)
                .onSuccess {
                    println("Send success")
//                    close()
                }
                .onFailure {
                    close()
                }
        }

        override fun onFail(throwable: Throwable) {
            close(throwable)
        }

    })

    awaitClose { }
}

fun main() = runBlocking {
    testAsFlow()
}

private suspend fun testAsFlow() =
    KtHttpV5.create(ApiServiceV5::class.java)
        .repos(lang = "Kotlin", since = "weekly")
        .asFlow1()
        .catch { println("Catch: $it") }
        .collect {
            println(it)
        }

/**
 * 控制台输出带协程信息的log
 */
fun logX(any: Any?) {
    println(
        """
================================
$any
Thread:${Thread.currentThread().name}
================================""".trimIndent()
    )
}