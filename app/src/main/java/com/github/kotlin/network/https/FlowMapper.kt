package com.github.kotlin.network.https

import android.content.ComponentName
import android.content.Intent
import com.github.kotlin.network.https.exception.ApiException
import com.github.kotlin.network.https.exception.NetErrorCode
import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * FlowMapper
 *
 * @author tiankang
 * @description:
 * @date :2023/6/12 18:27
 */
object FlowMapper {
    private const val RESULT_OK = 200
    private const val TO_LOGIN_ACTION = "com.login.passiveLogout"
    private const val ACCOUNT_SERVICE_PACKAGE = "com.smartlink.tkmainservice.account"
    private const val ACCOUNT_SERVICE_LOGINRECEIVER =
        "com.smartlink.tkmainservice.account.receiver.LoginReceiver"


    fun <T> transformResultNoData(result: HttpBean<T>): Any? {
        val code: Int = result.resultCode
        val message: String = result.message
        return when (code) {
            RESULT_OK -> {
                result.data
            }

            NetErrorCode.RESULT_CODE_EXPIRED -> {
                toLogin()
                ApiException(code, message)
            }

            else -> {
                ApiException(code, message)
            }
        }
    }

    fun <T> transformResultMsg(result: HttpBean<T>): Any {
        val code: Int = result.resultCode
        val message: String = result.message
        return if (code == RESULT_OK) {
            message
        } else {
            ApiException(code, message)
        }
    }

    fun <T> transformResult(result: HttpBean<T>): Any? {
        val code: Int = result.resultCode
        val message: String = result.message
        return if (code == RESULT_OK) {
            if (result.data == null) {
                ApiException(NetErrorCode.RESULT_EMPTY, message)
            } else {
                result.data
            }
        } else {
            ApiException(code, message)
        }
    }


    fun transformException(e: Throwable): ApiException {
        val ex: ApiException = when (e) {
            is JsonParseException, is JSONException, is ParseException -> {
                ApiException(NetErrorCode.PARSE_ERROR, e.message)
            }

            is UnknownHostException, is SocketTimeoutException, is ConnectException -> {
                ApiException(NetErrorCode.NETWORK_ERROR, e.message)
            }

            is HttpException -> {
                ApiException(e.code(), e.message())
            }

            else -> {
                ApiException(NetErrorCode.UNKNOWN, e.message)
            }
        }
        return ex
    }

    private fun toLogin() {
        val intent = Intent(TO_LOGIN_ACTION).apply {
            component = ComponentName(ACCOUNT_SERVICE_PACKAGE, ACCOUNT_SERVICE_LOGINRECEIVER)
        }
//        if (null != LinkBaseApplication.getContext()) {
//            LinkBaseApplication.getContext().sendBroadcast(intent)
//        }
    }
}