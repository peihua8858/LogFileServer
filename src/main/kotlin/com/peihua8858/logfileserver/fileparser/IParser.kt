package com.peihua8858.logfileserver.fileparser

import com.fz.common.file.writeToFile
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.exception.PluginException
import com.peihua8858.logfileserver.fileparser.impl.ApkParser
import com.peihua8858.logfileserver.fileparser.impl.ImagesParser
import com.peihua8858.logfileserver.fileparser.impl.IpaParser
import com.peihua8858.logfileserver.fileparser.impl.OtherParser
import com.peihua8858.logfileserver.utils.sourceFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * App 包解析器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/16 15:21
 */
abstract class IParser<T> {
    @get:Throws(FileNotFoundException::class)
    protected val classPathFile: File
        get() {
            val path = sourceFile?.absolutePath
            println("classpath:$path")
            var file = File(path)
            println("path:" + file.absolutePath)
            if (!file.exists()) file = File("")
            return file
        }

    /**
     * 创建临时文件
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun createTempFile(data: ByteArray?, fileName: String): File {
        if (data == null || data.isEmpty()) {
            throw NullPointerException("No data available.")
        }
        if (fileName.isEmpty()) {
            throw NullPointerException("Don't know the file name.")
        }
        val path = classPathFile
        println("path:" + path.absolutePath)
        val tempFolder = "temp" + File.separator
        val parentFile = File(path.absolutePath, tempFolder)
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        println("parentFile:" + parentFile.absolutePath)
        val tempFile = File(parentFile, fileName)
        if (tempFile.exists()) {
            tempFile.delete()
        }
        tempFile.writeToFile(data)
        return tempFile
    }
//
//    /**
//     * 上传launcher 图标
//     */
//    @Throws(IOException::class)
//    protected fun saveLauncherIcon(iconData: ByteArray?, model: AppInfoExample) {
//        val file = createTempFile(iconData, "ic_launcher.png")
//        model.iconPath = file.absolutePath
//    }

    /**
     * 上传app文件
     */
    @Throws(Exception::class)
    protected fun uploadFile(file: File, model: AppInfo) {
        model.fileSize = file.length()
        model.filePath = file.absolutePath
//        httpClient.sendPostJson(model)
    }

    /**
     * 只做文件上传
     */
    @Throws(Exception::class)
    protected fun uploadFile(
        file: File, model: AppInfo,
        overwrite: Boolean, serverIp: String
    ) {
        model.fileSize = file.length()
        model.filePath = file.absolutePath
//        httpClient.onlyUploadFile(model, overwrite, serverIp)
    }

    /**
     * App 包解析
     *
     * @param parameter
     * @author dingpeihua
     * @date 2020/1/16 16:00
     * @version 1.0
     */
    @Throws(PluginException::class, IOException::class)
    abstract fun onParser(parameter: Parameter): Pair<T, ByteArray?>

    abstract fun generateParentFile(
        bundleId: String?, isOnlyUploadFile: Boolean,
        contentType: String,
        fileExtension: String,
        file: File
    ): File

    companion object {
        /**
         * 根据文件后缀创建解析器
         *
         * @param name
         * @return
         * @throws IllegalAccessException
         */
        @JvmStatic
        @Throws(IllegalAccessException::class)
        fun<T> createParser(extensionName: String?, contentType: String): IParser<T> {
            return when {
                "apk".equals(extensionName, ignoreCase = true) -> {
                    ApkParser()
                }

                "ipa".equals(extensionName, ignoreCase = true) -> {
                    IpaParser()
                }

                contentType.uppercase().startsWith("image/") -> ImagesParser()
                else -> OtherParser()
            }
        }
    }
}