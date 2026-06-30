package com.peihua8858.logfileserver.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class MD5Utils {
}

/**
 * 创建文件夹名
 *
 * @author dingpeihua
 * @date 2019/4/20 09:44
 * @version 1.0
 */
fun String?.md5FileName(): String {
    if (isNullOrEmpty()) {
        return ""
    }
    try {
        val m = MessageDigest.getInstance("MD5")
        m.update(toByteArray(StandardCharsets.UTF_8))
        val data = m.digest()
        val result = StringBuilder()
        for (i in data.indices) {
            result.append(Integer.toHexString((0x000000ff and data[i].toInt()) or -0x100).substring(6))
        }
        return result.toString()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return replace(".", "_")
}