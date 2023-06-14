package com.github.kotlin.network.https

import android.annotation.SuppressLint
import com.github.kotlin.network.https.gson.GsonAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * ApiCall
 *
 * @author tiankang
 * @description:
 * @date :2023/6/2 17:31
 */
object ApiCall {
    /**
     * 请求超时时间
     */
    private const val DEFAULT_TIMEOUT = 30000
    private lateinit var SERVICE: HttpService

    //手动创建一个OkHttpClient并设置超时时间
    val retrofit: HttpService
        get() {
            if (!ApiCall::SERVICE.isInitialized) {
                SERVICE = Retrofit.Builder()
                    .client(okHttp)
                    .addConverterFactory(GsonConverterFactory.create(GsonAdapter.buildGson()))
                    .baseUrl(HttpService.url)
                    .build()
                    .create(HttpService::class.java)
            }
            return SERVICE
        }

    //手动创建一个OkHttpClient并设置超时时间
    val okHttp: OkHttpClient
        get() {
            return OkHttpClient.Builder().run {
                connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                writeTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                //不验证证书
                sslSocketFactory(createSSLSocketFactory(), arrayOf(TrustAllCerts())[0])
                hostnameVerifier(TrustAllNameVerifier())
                build()
            }
        }

    private fun createSSLSocketFactory(): SSLSocketFactory {
        lateinit var ssfFactory: SSLSocketFactory
        try {
            val sslFactory = SSLContext.getInstance("TLS")
            sslFactory.init(null,  arrayOf(TrustAllCerts()), SecureRandom());
            ssfFactory = sslFactory.socketFactory
        } catch (e: Exception) {
            print("SSL错误：${e.message}")
        }
        return ssfFactory
    }

}

class TrustAllNameVerifier: HostnameVerifier {
    @SuppressLint("BadHostnameVerifier")
    override fun verify(hostname: String?, session: SSLSession?): Boolean = true
}

@SuppressLint("CustomX509TrustManager")
class TrustAllCerts : X509TrustManager {

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}