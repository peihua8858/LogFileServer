package com.peihua8858.logfileserver.utils

import org.springframework.boot.system.ApplicationHome
import java.io.File
import java.net.URISyntaxException
import java.nio.file.Path


class FileUtils

val String.extension: String
    get() = substringAfterLast('.', "")


fun File.readRelativePath(fileName: String): String {
    val path = absolutePath
    return path.substring(path.indexOf(fileName))
}

fun File.renameFile(): String {
    val name: String = nameWithoutExtension
    return name + "_" + formatCurTimeSecond
}

fun Any.createFileName(
    appName: String, buildType: String, versionName: String, versionCode: Int, extensionName: String,
    fileName: String
): String {
    return appName + "_" + versionName + "_" + versionCode + "_" + buildType + "_" + formatCurTimeSecond + "." + extensionName
}

/**
 *
 * [this] is file name
 * @author dingpeihua
 * @date 2026/6/30 10:30
 **/
fun String.createImageFileName(): String {
    val extensionName = this.extension
    return "IMG_$formatCurrentTimeMillis.$extensionName"
}


/**
 *
 * [this] is content-type or media type,e. ‘image/png’
 * @author dingpeihua
 * @date 2026/6/30 10:30
 **/
fun String.createFileName(): String {
    val extensionName = if (startsWith("image/")) ".jpg" else ".temp"
    return "File_$formatCurrentTimeMillis$extensionName"
}

/**
 *
 * 获取jar所在的文件目录
 * @author dingpeihua
 * @date 2026/7/1 11:49
 **/
val jarDir: Path
    get() {
        try {
            val jarFile = File(
                FileUtils::class.java.getProtectionDomain()
                    .codeSource
                    .location
                    .toURI()
            )
            return jarFile.getParentFile().toPath().toAbsolutePath().normalize()
        } catch (e: URISyntaxException) {
            throw RuntimeException("无法获取 jar 所在目录", e)
        }
    }