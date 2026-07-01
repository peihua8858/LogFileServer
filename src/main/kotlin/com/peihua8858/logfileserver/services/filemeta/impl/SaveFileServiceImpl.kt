package com.peihua8858.logfileserver.services.filemeta.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.fz.common.file.copyToFile
import com.fz.common.file.createFile
import com.fz.common.file.deleteFile
import com.fz.common.file.writeToFile
import com.fz.common.text.isNonEmpty
import com.peihua8858.logfileserver.configs.AppProperties
import com.peihua8858.logfileserver.data.DataStore
import com.peihua8858.logfileserver.entity.FileModel
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.FileParserRouter
import com.peihua8858.logfileserver.fileparser.Parameter
import com.peihua8858.logfileserver.fileparser.ResultData
import com.peihua8858.logfileserver.fileparser.exception.UploadFileException
import com.peihua8858.logfileserver.mappers.appinfo.AppInfoMapper
import com.peihua8858.logfileserver.services.filemeta.SaveFileService
import com.peihua8858.logfileserver.utils.*
import jakarta.servlet.http.HttpServletRequest
import okio.Path.Companion.toPath
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.io.File
import java.util.Locale
import javax.imageio.ImageIO

@Service
class SaveFileServiceImpl(
    private val fileParserRouter: FileParserRouter,  // 注入路由器
    private val properties: AppProperties
) : ServiceImpl<AppInfoMapper, AppInfo>(), SaveFileService {

    companion object {
        private val LOG = LoggerFactory.getLogger(SaveFileServiceImpl::class.java)
    }

    override fun parserAndSaveFiles(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        files: Array<MultipartFile>
    ): Map<String, MutableList<ResultData>> {
        val result = mutableListOf<ResultData>()
        files.forEach {
            val resultData = try {
                parserAndSaveFile(request, desc, buildType, isOverWrite, isOnlyUploadFile, it)
            } catch (e: Exception) {
                ResultData(
                    name = it.name,
                    size = it.size,
                    type = it.contentType,
                    error = e.message ?: "Error while saving app info."
                )
            }
            result.add(resultData)
        }
        val res = mutableMapOf<String, MutableList<ResultData>>()
        res["files"] = result
        return res
    }

    override fun parserAndSaveFile(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        file: MultipartFile
    ): ResultData {
        if (file.isEmpty) {
            return ResultData(
                name = file.name,
                size = file.size,
                type = file.contentType,
                error = "Multiple request parameters are invalid."
            )
        }
        val contentType = file.contentType
            ?: return ResultData(error = "Content-Type is not support.")
        val tempDir = File(properties.dataDirFile, DataStore.TEMP_DIR)
        if (!tempDir.exists()) tempDir.mkdirs()
        val tempFile = File(tempDir, file.originalFilename ?: contentType.createFileName())
        file.transferTo(tempFile)
        val parameter = Parameter(tempFile, buildType, desc, isOnlyUploadFile, isOverWrite)
        val extensionName = file.originalFilename?.extension ?: "apk"
        val parser = fileParserRouter.resolve(extensionName, contentType)
        val pairResult = parser.onParser(parameter, properties.dataDirFile)
        val result = pairResult.first

        // 通用属性设置
        result.buildDescription = desc
        LOG.info(result.toJSONString())

        try {
            // 根据文件类型分支处理
            when (result) {
                is AppInfo -> {
                    // AppInfo 专属流程：版本号校验
                    val versionName = result.versionName
                    if (!isOnlyUploadFile) {
                        if (!versionName.isValidVersion()) {
                            return ResultData(
                                name = file.name,
                                size = file.size,
                                type = file.contentType,
                                error = "version number \"$versionName\" is invalid.Please see Semantic Versioning 2.0.0: https://semver.org/lang/zh-CN/"
                            )
                        }
                    }
                    // 生成输出目录
                    val outputParent = parser.generateParentFile(
                        result.bundleId,
                        isOnlyUploadFile,
                        contentType,
                        extensionName,
                        dirFile = properties.dataDirFile,
                        parameter.file
                    )
                    LOG.info("outputParent>>>$outputParent")
                    // 保存图标
                    if (!isOnlyUploadFile) {
                        val iconPath = saveLauncherIcon(pairResult.second, outputParent)
                        result.iconPath = iconPath
                    }
                    // 保存文件
                    val response =
                        saveRealFile(result, outputParent, extensionName, isOverWrite, isOnlyUploadFile, parameter.file)
                    LOG.info("outputParent>>>${response.toJSONString()}")
                    // 数据库持久化
                    if (!isOnlyUploadFile) {
                        save(result)
                    }
                    return buildResultData(file, result, response, request)
                }

                else -> {
                    // ImageFileModel / OtherFileModel 流程：只做文件保存
                    val outputParent = parser.generateParentFile(
                        result.bundleId,
                        isOnlyUploadFile,
                        contentType,
                        extensionName,
                        dirFile = properties.dataDirFile,
                        parameter.file
                    )
                    LOG.info("outputParent>>>$outputParent")
                    if (!outputParent.exists()) outputParent.mkdirs()
                    val savedFile = File(outputParent, parameter.file.name)
                    parameter.file.copyToFile(savedFile)
                    val filePath = "files" + File.separator + savedFile.readRelativePath(DataStore.FILES_DIR)
                    result.filePath = filePath
                    result.fileName = savedFile.name
                    result.downloadUrl = filePath
                    return buildResultData(file, result, filePath, request)
                }
            }
        } finally {
            // 确保临时文件被清理
            parameter.file.deleteFile()
        }
    }

    private fun buildResultData(
        file: MultipartFile,
        result: FileModel,
        url: String,
        request: HttpServletRequest
    ): ResultData {
        return ResultData(
            file.originalFilename,
            file.size,
            thumbnailUrl = result.iconPath ?: "",
            url = url,
            type = file.contentType
        ).apply {
            val serverUrl = request.serverUrl
            val fullUrl = "$serverUrl${File.separator}${this.url}"
            if (this.thumbnailUrl.isNonEmpty()) {
                val iconUrl = "$serverUrl${File.separator}${this.thumbnailUrl}"
                this.thumbnailUrl = iconUrl
            }
            this.url = fullUrl
        }
    }

    override fun saveAppFiles(
        request: HttpServletRequest,
        bundleId: String,
        appName: String,
        versionName: String,
        versionCode: String,
        buildType: String,
        platform: String,
        files: Array<MultipartFile>
    ): Map<String, MutableList<ResultData>> {
        val result = mutableListOf<ResultData>()
        files.forEach {
            val resultData = try {
                saveAppFile(request, bundleId, appName, versionName, versionCode, buildType, platform, it)
            } catch (e: Exception) {
                ResultData(
                    name = it.name,
                    size = it.size,
                    type = it.contentType,
                    error = e.message ?: "Error while saving app info."
                )
            }
            result.add(resultData)
        }
        val res = mutableMapOf<String, MutableList<ResultData>>()
        res["files"] = result
        return res
    }

    override fun saveAppFile(
        request: HttpServletRequest,
        bundleId: String,
        appName: String,
        versionName: String,
        versionCode: String,
        buildType: String,
        platform: String,
        file: MultipartFile
    ): ResultData {
        LOG.info("bundleId:$bundleId")
        val bundlePath = bundleId.md5FileName()
        LOG.info("bundlePath:$bundlePath")
        //如果上传目录为/files/upload/[android/ios]/，则可以如下获取：
        val uploadFolder =
            DataStore.FILES_DIR + File.separator + platform.lowercase(Locale.getDefault()) + File.separator + bundlePath + File.separator
        val uploadFolderFile = File(properties.dataDirFile, uploadFolder)
        if (!uploadFolderFile.exists()) uploadFolderFile.mkdirs()
        LOG.info("uploadFolderFile:" + uploadFolderFile.absolutePath)
        val contentType = file.contentType ?: throw UploadFileException("Content-Type is not support.")
        val originFileName = file.originalFilename
        if (originFileName.isNullOrEmpty()) {
            throw UploadFileException("获取文件名失败。")
        }
        //获取文件扩展名
        val fileExtension: String = originFileName.extension
        if ("apk".equals(fileExtension, ignoreCase = true) || "ipa".equals(fileExtension, ignoreCase = true)) {
            try {
                val fileName = appName + "_" + versionName + "_" + versionCode +
                        "_" + buildType + "_" + formatCurTimeSecond + "." + fileExtension
                LOG.info("file.getName():$fileName")
                val appFile = File(uploadFolderFile, fileName)
                if (appFile.exists()) {
                    appFile.delete()
                }
                LOG.info("appFile:" + appFile.absolutePath)
                LOG.info(
                    "appFile.getParentFile():" + appFile.getParentFile().getAbsoluteFile()
                )
                file.transferTo(appFile) //保存文件
                val backAppPath = "files" + File.separator + uploadFolder + appFile.getName()
                LOG.info("backAppPath:$backAppPath")
                return ResultData(appFile.name, size = file.size, type = contentType, url = backAppPath)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                throw UploadFileException(e.message ?: "save file error")
            }
        } else {
            val image: Image? = ImageIO.read(file.inputStream)
            if (contentType.startsWith("image/") || image != null) {
                try {
                    val imageFile = File(uploadFolderFile, "ic_launcher.png")
                    if (imageFile.exists()) {
                        imageFile.delete()
                    }
                    LOG.info("imageFile:" + imageFile.absolutePath)
                    LOG.info("imageFile.getParentFile():" + imageFile.getAbsoluteFile())
                    file.transferTo(imageFile) //保存文件
                    val backIconPath = "files" + File.separator + uploadFolder + imageFile.getName()
                    LOG.info("backImagePath:$backIconPath")
                    return ResultData(imageFile.name, size = file.size, type = contentType, url = backIconPath)
                } catch (e: Exception) {
                    LOG.info("saveImagePath:" + e.stackTraceToString())
                    e.printStackTrace()
                    throw UploadFileException(e.message ?: "save file error")
                }
            } else {
                throw UploadFileException("File is not support.")
            }
        }
    }

    private fun saveLauncherIcon(launcherData: ByteArray?, outputParent: File): String? {
        if (launcherData == null || launcherData.isEmpty()) {
            return null
        }
        try {
            if (!outputParent.exists()) outputParent.mkdirs()
            val iconFile = File(outputParent, "ic_launcher.png")
            if (iconFile.exists()) {
                iconFile.delete()
            }
            LOG.info("iconFile:" + iconFile.absolutePath)
            LOG.info("iconFile.getParentFile():" + iconFile.absoluteFile)
            iconFile.writeToFile(launcherData)
            val backIconPath = "files" + File.separator + iconFile.readRelativePath(DataStore.FILES_DIR)
            LOG.info("saveLauncherIcon>backIconPath:$backIconPath")
            return backIconPath
        } catch (e: java.lang.Exception) {
            LOG.info("saveLauncherIcon:${e.stackTraceToString()}")
            e.printStackTrace()
            return null
        }
    }

    private fun saveRealFile(
        model: FileModel,
        outputParent: File,
        fileExtension: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        file: File
    ): String {
        //如果上传目录为/files/upload/[android/ios]/，则可以如下获取：
        if (!outputParent.exists()) outputParent.mkdirs()
        var appFile = File(outputParent, file.name)
        appFile = if (isOnlyUploadFile) {
            if (isOverWrite) {
                if (appFile.exists()) {
                    appFile.delete()
                }
                appFile
            } else {
                File(outputParent, file.renameFile() + "." + fileExtension)
            }
        } else {
            // 对于非 AppInfo 类型，使用通用命名
            val fileName = if (model is AppInfo) {
                "${model.name}_${model.versionName}_${model.versionCode}_${model.buildType}_${formatCurTimeSecond}.$fileExtension"
            } else {
                "${model.name ?: file.nameWithoutExtension}_${formatCurTimeSecond}.$fileExtension"
            }
            File(outputParent, fileName)
        }
        file.copyToFile(appFile) //保存文件
        val filePath = "files" + File.separator + appFile.readRelativePath(DataStore.FILES_DIR)
        model.filePath = filePath
        model.fileName = filePath
        model.downloadUrl = filePath
        return filePath
    }
}