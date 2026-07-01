package com.peihua8858.logfileserver.fileparser.impl

import java.io.File

/**
 * 平台策略接口，封装每种平台的通用属性和目录创建逻辑。
 *
 * 每种文件解析器（Apk、Ipa、Images、Other）通过实现此接口，
 * 声明自己负责的平台名称、目录名、匹配扩展名，以及默认的目录创建逻辑。
 *
 * 对于仅需按扩展名匹配的平台（如 Apk、Ipa），可直接使用 [Default] 实现。
 * 对于有复杂判断逻辑的平台（如 Images、Other），解析器可重写
 * [AbstractFileParser.createPlatformFile] 提供自定义行为。
 */
interface PlatformStrategy {

    /** 平台显示名称，如 "Android"、"iOS"、"Images"、"Other" */
    val platformName: String

    /** 默认上传子目录名，如 "android"、"ios"、"images"、"other" */
    val platformDirectory: String

    /** 此策略负责的文件扩展名（不含点号），如 "apk"、"ipa" */
    val extension: String

    /**
     * 创建平台子目录。
     *
     * @param contentType HTTP Content-Type
     * @param fileExtension 文件扩展名（不含点号）
     * @param file 上传的原始文件
     * @param parentFile 上级目录（upload/）
     * @return 平台子目录，若文件类型不匹配则返回 null
     */
    fun createPlatformFile(
        contentType: String,
        fileExtension: String,
        file: File,
        parentFile: File,
    ): File?

    /**
     * 默认实现：按扩展名忽略大小写匹配，匹配成功则返回以 [platformDirectory] 命名的子目录。
     */
    class Default(
        override val platformName: String,
        override val platformDirectory: String,
        override val extension: String,
    ) : PlatformStrategy {
        override fun createPlatformFile(
            contentType: String,
            fileExtension: String,
            file: File,
            parentFile: File,
        ): File? {
            return if (extension.equals(fileExtension, ignoreCase = true)) {
                File(parentFile, platformDirectory)
            } else {
                null
            }
        }
    }
}
