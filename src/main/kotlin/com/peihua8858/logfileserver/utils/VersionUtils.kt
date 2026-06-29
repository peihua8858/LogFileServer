package com.peihua8858.logfileserver.utils

import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min

class VersionUtils {
}

/**
 * 版本号1.0.0-beta规则
 */
private val VERSION_PATTERN = Pattern.compile(
    "^[vV]?(([0-9]|([1-9]([0-9]*))).){1,5}([0-9]|" +
            "([1-9]([0-9]*)))([-](([0-9A-Za-z]|([1-9A-Za-z]([0-9A-Za-z]*)))[.])*([0-9A-Za-z]|([1-9A-Za-z]" + "([0-9A" + "-Za-z]*))))?([+](([0-9A-Za-z]+)[.])*([0-9A-Za-z]+))?$"
)

/**
 * 版本号1.0.0规则
 */
private val VERSION_PATTERN2 = Pattern.compile(
    "^[vV]?(([0-9]|([1-9]([0-9]*))).){1,5}([0-9]|" +
            "([1-9]([0-9]*)))$"
)

/**
 * 判断版本号是否正确,后面可跟Alpha 或beta
 *
 * @param [this] 版本号
 * @author dingpeihua
 * @date 2020/1/21 17:45
 * @version 1.0
 */
fun String?.isValidVersionAlphaOrBeta(): Boolean {
    if (this == null) {return false}
    val matcher: Matcher = VERSION_PATTERN.matcher(this)
    return matcher.find()
}

/**
 * 判断版本号是否正确，只能数字加点
 *
 * @param [this] 版本号
 * @author dingpeihua
 * @date 2020/1/21 17:45
 * @version 1.0
 */
fun String?.isValidVersion(): Boolean {
    if (this == null) {return false}
    val matcher: Matcher = VERSION_PATTERN2.matcher(this)
    return matcher.find()
}

/**
 * 版本号比较，版本号逐位比较
 *
 * @param version1 第一个版本号
 * @param version2 第二个版本号
 * @author dingpeihua
 * @date 2020/3/23 14:49
 * @version 1.0
 */
fun compareVersion(version1: String, version2: String): Int {
    require(!(!version1.isValidVersion() || !version2.isValidVersion())) { "Verion number is invalid." }
    val versionArray1: Array<String> = version1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val versionArray2: Array<String> = version2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val len1 = versionArray1.size
    val len2 = versionArray2.size
    val len = min(len1, len2)
    //共有版本号部分，从前向后比较对应位置数字
    var x1: Int
    var x2: Int
    for (i in 0..<len) {
        x1 = versionArray1[i].toInt()
        x2 = versionArray2[i].toInt()
        if (x1 > x2) {
            return 1
        } else if (x1 < x2) {
            return -1
        }
    }
    //共有版本号相等的情况下，谁的版本号段数更多且多余部分不全为0，谁的版本更新
    if (len1 > len2) {
        for (i in len..<len1) {
            if (versionArray1[i].toInt() > 0) {
                return 1
            }
        }
    } else if (len1 < len2) {
        for (i in len..<len2) {
            if (versionArray2[i].toInt() > 0) {
                return -1
            }
        }
    }
    return 0
}