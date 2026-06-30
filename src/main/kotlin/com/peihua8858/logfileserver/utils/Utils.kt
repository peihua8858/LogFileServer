package com.peihua8858.logfileserver.utils

import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.system.ApplicationHome
import org.springframework.util.StringUtils
import java.io.File
import java.util.regex.Pattern
import kotlin.contracts.ExperimentalContracts

private val IP_ADDRESS_AND_PORT = ("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." +
        "(25[0-5]|2[0-4]" + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]" + "[0-9]{2}|[1"
        + "-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" + "|[1-9][0-9]|[0-9])\\:([0-9]|[1-9]\\d{1," +
        "3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5]))")
private val IP_ADDRESS_STRING = ("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." + "(25"
        + "[0-5]|2[0-4]" + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]" + "[0-9]{2}|[1" + "-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" + "|[1-9][0-9]|[0-9]))")

val IP_ADDRESS = Pattern.compile(IP_ADDRESS_STRING)

val IP_ADDRESS_PORT = Pattern.compile(IP_ADDRESS_AND_PORT)
val AppInfo.isIOSPlatform: Boolean
    get() = "iOS".equals(platform, true)

val AppInfo.isAndroidPlatform: Boolean
    get() = "android".equals(platform, true)

/**
 * 验证文本[this]是否是数字
 *
 * @return 是返回true, 否则返回false
 */
val String?.isNumber: Boolean
    get() = this?.matches("[0-9]+".toRegex()) == true


/**
 * 验证是否是ip
 *
 * @param target 要验证的文本
 * @return 是返回true, 否则返回false
 */
val String?.isIpAddress: Boolean
    get() = this?.matches(IP_ADDRESS_STRING.toRegex()) == true

val Any.sourceFile: File?
    get() {
        val h = ApplicationHome(javaClass)
        return h.source
    }

val HttpServletRequest.serverUrl: String
    get() {
        val port = serverPort
        var serverUrl = "$scheme://$serverName"
        if (port > 0) {
            serverUrl += ":$port"
        }
        return serverUrl
    }


fun  isEmptyException(vararg data: CharSequence?): Boolean {
    val sb = StringBuilder()
    for (charSequence in data) {
        if (charSequence.isNullOrEmpty()) {
            sb.append(charSequence).append("and")
        }
    }
    println(">>>>>:$sb")
    if (sb.isNotEmpty()) {
        sb.subSequence(0, sb.lastIndexOf("and"))
        throw NullPointerException("Required String parameter $sb is invalid")
    }
    return false
}

fun  isEmptyMessage(vararg data: CharSequence?): String {
    val sb = StringBuilder()
    for (charSequence in data) {
        if (charSequence.isNullOrEmpty()) {
            sb.append(charSequence).append("and")
        }
    }
    println(">>>>>:$sb")
    if (sb.isNotEmpty()) {
        sb.subSequence(0, sb.lastIndexOf("and"))
        return "Required String parameter $sb is invalid"
    }
    return ""
}