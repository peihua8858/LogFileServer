package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.fileparser.IParser
import com.peihua8858.logfileserver.utils.md5FileName
import com.peihua8858.logfileserver.utils.sourceFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.imageio.ImageIO

abstract class AbstractParser<T> : IParser<T>() {
    override fun generateParentFile(
        bundleId: String?, isOnlyUploadFile: Boolean,
        contentType: String,
        fileExtension: String,
        file: File
    ): File {
        val uploadFolder = File(sourceFile?.parentFile, "upload")
        //如果上传目录为/files/upload/[android/ios/other/images]/，则可以如下获取：
        val platformFile = createPlatformFile(contentType, fileExtension, file, uploadFolder)
            ?: throw IllegalArgumentException("文件类型不匹配。")
        return if (isOnlyUploadFile || bundleId.isNullOrEmpty()) {
            platformFile
        } else {
            val bundlePath =bundleId.md5FileName()
            File(platformFile, bundlePath)
        }
    }

    abstract fun createPlatformFile(contentType: String, fileExtension: String, file: File, parentFile: File): File?
}