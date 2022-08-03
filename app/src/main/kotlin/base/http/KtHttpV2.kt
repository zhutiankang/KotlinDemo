package base

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * KtHttp
 *
 * @author tiankang
 * @description: 函数式思维
 * @date :2022/7/20 14:50
 */
// 这种写法是有问题的，但这节课我们先不管。
data class RepoList2(
    var count: Int?,
    var items: List<Repo>?,
    var msg: String?
)

data class Repo2(
    var added_stars: String?,
    var avatars: List<String>?,
    var desc: String?,
    var forks: String?,
    var lang: String?,
    var repo: String?,
    var repo_link: String?,
    var stars: String?
)

interface ApiService2 {
    @GET("/repo")
    fun repos(
        @Field("lang") lang: String,
        @Field("since") since: String
    ): RepoList2
}


fun main() {
    // ①
    val api: ApiService2 = KtHttpV2.create(ApiService2::class.java)

    // ②
    val data: RepoList2 = api.repos(lang = "Kotlin", since = "weekly")

    println(data)

    KtHttpV2.baseUrl = "https://api.github.com"

    val api1 = KtHttpV2.create<GitHubService2>()
    val data1 = api1.search(id = "JetBrains")

    println(data1)
}

object KtHttpV2 {
    //通过 Proxy，就可以动态地创建 ApiService 接口的实例化对象

    private val okHttpClient by lazy { OkHttpClient() }
    private val gson: Gson by lazy { Gson() }

    var baseUrl = "https://baseurl.com"

    //类型实化（Reified Type）
    inline fun <reified T> create(): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java)
        ) { proxy, method, args ->
            return@newProxyInstance method.annotations
                .filterIsInstance<GET>()
                .takeIf { it.size == 1 }
                ?.let {
                    invoke1("$baseUrl${it[0].value}", method, args)
                }
        } as T
    }
    fun invoke1(url: String, method: Method, args: Array<Any>): Any? =
        method.parameterAnnotations
            .takeIf { method.parameterAnnotations.size == args.size }
            ?.mapIndexed{index, it -> Pair(it, args[index])}
            ?.fold(url, ::parseUrl)
            ?.let { Request.Builder().url(it).build() }
            ?.let { okHttpClient.newCall(it).execute().body?.string() }
            ?.let { gson.fromJson(it, method.genericReturnType) }

    //fold 这个操作符，其实就是高阶函数版的 for 循环
    private fun parseUrl(acc: String, pair: Pair<Array<Annotation>, Any>) =
        pair.first.filterIsInstance<Field>()
            .first()
            .let { field ->
                if (acc.contains("?")) {
                    "$acc&${field.value}=${pair.second}"
                } else {
                    "$acc?${field.value}=${pair.second}"
                }
            }
    fun <T> create(service: Class<T>): T {

        // 调用 Proxy.newProxyInstance 就可以创建接口的实例化对象
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf<Class<*>>(service)
        ) { proxy, method, args ->
            val annotations = method.annotations
            for (annotation in annotations) {
                if (annotation is GET) {
                    val url = baseUrl + annotation.value
                    return@newProxyInstance invoke(url, method, args!!)

                }
            }
        } as T
    }


    fun invoke(path: String, method: Method, args: Array<Any>): Any? {
        // 条件判断
        if (method.parameterAnnotations.size != args.size) return null

        // ① 根据url拼接参数，也就是：url + ?lang=Kotlin&since=weekly
        // args 参数的值
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
        // 最终的url会是这样： // https://baseurl.com/repo?lang=Kotlin&since=weekly

        // ② 使用okHttpClient进行网络请求
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()

        val genericReturnType = method.genericReturnType
        // ③ 使用gson进行JSON解析
        val body = response.body
        val json = body?.string()
        // ④ 返回结果
        return gson.fromJson(json, genericReturnType)
    }
}

//动态代理 + 注解 + 反射
//通过这样的方式，我们就不必在代码当中去实现每一个接口，而是只要是符合这样的代码模式，任意的接口和方法，我们都可以直接传进去
//动态代理实现网络请求的优势，它的灵活性是非常好的
interface GitHubService2 {
    @GET("/search")
    fun search(@Field("id") id: String): FourObject.User
}