package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.fileparser.IParser
import com.peihua8858.logfileserver.utils.sourceFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.imageio.ImageIO

abstract class AbstractParser : IParser() {
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
            val bundlePath = createFilePath(bundleId)
            File(platformFile, bundlePath)
        }
    }

    abstract fun createPlatformFile(contentType: String, fileExtension: String, file: File, parentFile: File): File?

    /**
     * 创建文件夹名
     *
     * @author dingpeihua
     * @date 2019/4/20 09:44
     * @version 1.0
     */
    fun createFilePath(path: String?): String {
        if (path.isNullOrEmpty()) {
            return ""
        }
        try {
            val m = MessageDigest.getInstance("MD5")
            m.update(path.toByteArray(StandardCharsets.UTF_8))
            val data = m.digest()
            val result = StringBuilder()
            for (i in data.indices) {
                result.append(Integer.toHexString((0x000000ff and data[i].toInt()) or -0x100).substring(6))
            }
            return result.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return path.replace(".", "_")
    }
}