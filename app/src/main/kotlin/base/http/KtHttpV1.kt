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
 * @description: Java思维
 * @date :2022/7/20 14:50
 */
// 这种写法是有问题的，但这节课我们先不管。
data class RepoList(
    var count: Int?,
    var items: List<Repo>?,
    var msg: String?
)

data class Repo(
    var added_stars: String?,
    var avatars: List<String>?,
    var desc: String?,
    var forks: String?,
    var lang: String?,
    var repo: String?,
    var repo_link: String?,
    var stars: String?
)

interface ApiService {
    @GET("/repo")
    fun repos(
        @Field("lang") lang: String,
        @Field("since") since: String
    ): RepoList
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET(val value: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(val value: String)


fun main() {
    // ①
    val api: ApiService = KtHttpV1.create(ApiService::class.java)

    // ②
    val data: RepoList = api.repos(lang = "Kotlin", since = "weekly")

    println(data)

    KtHttpV1.baseUrl = "https://api.github.com"

    val api1 = KtHttpV1.create(GitHubService::class.java)
    val data1 = api1.search(id = "JetBrains")

    println(data1)
}

object KtHttpV1 {
    //通过 Proxy，就可以动态地创建 ApiService 接口的实例化对象

    private var okHttpClient: OkHttpClient = OkHttpClient()
    private var gson: Gson = Gson()
    var baseUrl = "https://baseurl.com"
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


    private fun invoke(path: String, method: Method, args: Array<Any>): Any? {
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
interface GitHubService {
    @GET("/search")
    fun search(@Field("id") id:String) : FourObject.User
}