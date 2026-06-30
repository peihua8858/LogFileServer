package com.peihua8858.logfileserver.utils

import org.springframework.web.multipart.MultipartFile
import java.io.File

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

fun MultipartFile.transFileToTemp(contentType: String): File {
    val sourceFile = FileUtils::class.java.sourceFile?.parentFile
    val parentFile = File(sourceFile, ".temps")
    val outputFile = File(parentFile, originalFilename ?: contentType.createFileName())
    transferTo(outputFile)
    return outputFile
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