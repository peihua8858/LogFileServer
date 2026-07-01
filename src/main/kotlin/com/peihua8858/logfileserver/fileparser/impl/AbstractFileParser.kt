package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.data.DataStore
import com.peihua8858.logfileserver.entity.FileModel
import com.peihua8858.logfileserver.fileparser.FileParser
import com.peihua8858.logfileserver.utils.md5FileName
import java.io.File

/**
 *
 * 文件解析基础实现
 * @author dingpeihua
 * @date 2026/7/1 11:30
 **/
abstract class AbstractFileParser<T : FileModel> : FileParser<T> {

    /**
     * 返回当前解析器的平台策略，用于声明平台名称、目录及默认的文件目录创建逻辑。
     */
    abstract fun platformStrategy(): PlatformStrategy

    override fun generateParentFile(
        bundleId: String?, isOnlyUploadFile: Boolean,
        contentType: String,
        fileExtension: String,
        dirFile: File,
        file: File
    ): File {
        val uploadFolder = File(dirFile, DataStore.FILES_DIR)
        //如果上传目录为/files/upload/[android/ios/other/images]/，则可以如下获取：
        val platformFile = createPlatformFile(contentType, fileExtension, file, uploadFolder)
            ?: throw IllegalArgumentException("文件类型不匹配。")
        return if (isOnlyUploadFile || bundleId.isNullOrEmpty()) {
            platformFile
        } else {
            val bundlePath = bundleId.md5FileName()
            File(platformFile, bundlePath)
        }
    }

    /**
     * 设置文件元信息（fileSize、filePath）
     */
    protected fun setFileInfo(file: File, model: T) {
        model.fileSize = file.length()
        model.filePath = file.absolutePath
    }

    /**
     * 创建平台子目录。默认委托给 [platformStrategy]。
     * 子类可重写此方法以提供自定义的目录创建逻辑（如 ImagesParser、OtherParser）。
     */
     open fun createPlatformFile(
        contentType: String,
        fileExtension: String,
        file: File,
        parentFile: File
    ): File? {
        return platformStrategy().createPlatformFile(contentType, fileExtension, file, parentFile)
    }
}
