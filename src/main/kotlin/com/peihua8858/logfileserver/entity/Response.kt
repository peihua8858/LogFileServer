package com.peihua8858.logfileserver.entity

import com.baomidou.mybatisplus.core.metadata.IPage
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

/**
 * REST API 返回结果
 *
 * @param <T> 结果集
 */
data class Response<T>(
    /**
     * 业务错误码
     * @mock 200
     */
    @JsonProperty("code")
    private val code: Int = 0,

    /**
     * 响应数据结果
     *
     */
    @JsonProperty("data")
    private val data: T? = null,

    /**
     * 提示信息
     *  @mock Success
     */
    @JsonProperty("msg")
    private val msg: String = ""

) {
    constructor(data: T?, httpStatus: HttpStatus) : this(
        code = httpStatus.value(),
        data = data,
        msg = httpStatus.reasonPhrase
    )

    constructor(httpStatus: HttpStatus) : this(code = httpStatus.value(), msg = httpStatus.reasonPhrase)

    companion object {
        fun <T> ok(data: IPage<T>): Response<ResponsePage<T>> {
            return ok(ResponsePage(data))
        }
        fun <T> ok(data: T?): Response<T> {
            var aec = HttpStatus.OK
            if (data is Boolean && false == data) {
                aec = HttpStatus.NOT_FOUND
            }
            return restResult(data = data, errorCode = aec)
        }

        fun msg(msg: String): Response<Any?> {
            return Response(msg = msg)
        }

        fun msg(code: Int, msg: String): Response<Any?> {
            return Response(code = code, msg = msg)
        }

        fun failed(msg: String): Response<Any?> {
            return restResult(code = HttpStatus.BAD_REQUEST.value(), msg = msg)
        }

        fun failed(httpStatus: HttpStatus, msg: String): Response<Any?> {
            return restResult(code = httpStatus.value(), msg = msg)
        }

        fun failed(httpStatus: HttpStatus): Response<Any?> {
            return restResult(null, httpStatus)
        }

        fun <T> restResult(data: T?, errorCode: HttpStatus): Response<T> {
            return restResult(data, errorCode.value(), errorCode.reasonPhrase)
        }

        private fun <T> restResult(data: T? = null, code: Int, msg: String): Response<T> {
            return Response(code = code, data = data, msg = if (code == HttpStatus.OK.value()) "Success" else msg)
        }

    }

    fun isOK(): kotlin.Boolean {
        return HttpStatus.OK.value() == code
    }

    fun <T> ok(): Response<T> {
        return ok(null)
    }

    /**
     * 服务间调用非业务正常，异常直接释放
     */
    fun serviceData(): T? {
        if (!isOK()) {
            throw RuntimeException(this.msg)
        }
        return data
    }
}
