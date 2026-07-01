package com.peihua8858.logfileserver.fileparser


import com.peihua8858.logfileserver.entity.FileModel
import java.io.File

/**
 * 文件解析器策略接口
 * 每种文件类型实现此接口并注册为 Spring Bean
 */
interface FileParser<T : FileModel> {

    /**
     * 判断当前解析器是否支持该文件类型
     * @param extensionName 文件扩展名（如 apk, ipa, png）
     * @param contentType   MIME 类型（如 application/octet-stream, image/png）
     */
    fun supports(extensionName: String, contentType: String): Boolean

    /**
     * 解析文件，返回解析结果实体和图标数据
     */
    fun onParser(parameter: Parameter, dirFile: File): Pair<T, ByteArray?>

    /**
     * 生成文件存放的父目录
     */
    fun generateParentFile(
        bundleId: String?,
        isOnlyUploadFile: Boolean,
        contentType: String,
        fileExtension: String,
        dirFile: File,
        file: File
    ): File

    /**
     * 解析器优先级，数值越小优先级越高
     * 兜底解析器（OtherParser）应返回最大值
     */
    fun order(): Int = 100
}