package com.github.kotlin.network.https.exception

/**
 * ApiException
 *
 * @author tiankang
 * @description: * 自定义异常，当接口返回的code不为200时，需要跑出此异常
 * eg：登陆时验证码错误；参数为传递等
 * @date :2023/6/12 17:58
 */
class ApiException(private var code: Int, private var displayMessage: String?) : Exception() {

    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getDisplayMessage(): String? {
        return displayMessage
    }

    fun setDisplayMessage(displayMessage: String) {
        this.displayMessage = displayMessage
    }

    override fun toString(): String {
        return "ApiException{" +
                "code=" + code +
                ", displayMessage='" + displayMessage + '\'' +
                '}'
    }
}