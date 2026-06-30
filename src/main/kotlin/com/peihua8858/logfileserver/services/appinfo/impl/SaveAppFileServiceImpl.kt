package com.peihua8858.logfileserver.services.appinfo.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.fz.common.file.copyToFile
import com.fz.common.file.deleteFile
import com.fz.common.file.writeToFile
import com.fz.common.text.isNonEmpty
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.IParser
import com.peihua8858.logfileserver.fileparser.Parameter
import com.peihua8858.logfileserver.fileparser.ResultData
import com.peihua8858.logfileserver.fileparser.exception.UploadFileException
import com.peihua8858.logfileserver.mappers.appinfo.AppInfoMapper
import com.peihua8858.logfileserver.services.SaveService
import com.peihua8858.logfileserver.services.appinfo.SaveAppFileService
import com.peihua8858.logfileserver.utils.*
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Image
import java.io.File
import java.util.Locale
import javax.imageio.ImageIO

@Service
class SaveAppFileServiceImpl : ServiceImpl<AppInfoMapper, AppInfo>(), SaveAppFileService, SaveService {

    companion object {
        private val LOG = LoggerFactory.getLogger(SaveAppFileServiceImpl::class.java)
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
                error = "Multiple request parameters are invalid.")
        }
        val contentType = file.contentType
            ?: return ResultData(error = "Content-Type is not support.")
        val parameter = Parameter(file.transFileToTemp(contentType), buildType, desc, isOnlyUploadFile, isOverWrite)
        val extensionName = file.originalFilename?.extension ?: "apk"
        val parser = IParser.createParser(extensionName, contentType)
        val pairResult = parser.onParser(parameter)
        val result = pairResult.first
        result.buildDescription = desc
        val versionName = result.versionName
        LOG.info(result.toJSONString())
        if (!isOnlyUploadFile) {
            if (!versionName.isValidVersion()) {
                return ResultData(
                    name = file.name,
                    size = file.size,
                    type = file.contentType,
                    error = "version number \"$versionName\" is invalid.Please see Semantic Versioning 2.0.0: https://semver.org/lang/zh-CN/")
            }
        }
        //生成输出文件父级目录
        val outputParent =
            parser.generateParentFile(result.bundleId, isOnlyUploadFile, contentType, extensionName, parameter.file)
        LOG.info("outputParent>>>$outputParent")
        //保存app 桌面图标
        if (!isOnlyUploadFile) {
            saveLauncherIcon(pairResult.second, outputParent)
        }
        //保存上传的文件
        val response = saveRealFile(result, outputParent, extensionName, isOverWrite, isOnlyUploadFile, parameter.file)
        LOG.info("outputParent>>>${response.toJSONString()}")
        parameter.file.deleteFile()
        if (!isOnlyUploadFile) {
            save(result)
        }
        return ResultData(
            file.originalFilename,
            file.size,
            thumbnailUrl = result.iconPath ?: "",
            url = response,
            type = file.contentType
        ).apply {
            val serverUrl = request.serverUrl
            val url = "$serverUrl${File.separator}${this.url}"
            if (this.thumbnailUrl.isNonEmpty()) {
                val iconUrl = "$serverUrl${File.separator}${this.thumbnailUrl}"
                this.thumbnailUrl = iconUrl
            }
            this.url = url
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
        val parentFile: File? = sourceFile?.parentFile
        LOG.info("path:" + parentFile?.absolutePath)
        //如果上传目录为/files/upload/[android/ios]/，则可以如下获取：
        val uploadFolder =
            "upload" + File.separator + platform.lowercase(Locale.getDefault()) + File.separator + bundlePath + File.separator
        val uploadFolderFile = File(parentFile, uploadFolder)
        if (!uploadFolderFile.exists()) uploadFolderFile.mkdirs()
        LOG.info("uploadFolderFile:" + uploadFolderFile.absolutePath)
        val contentType = file.contentType ?: throw UploadFileException("Content-Type is not support.")
        val originFileName = file.originalFilename
        if (originFileName.isNullOrEmpty()) {
            throw UploadFileException("获取文件名失败。")
        }
        if (originFileName.isEmpty()) {
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
            val iconFile = File(outputParent, "ic_launcher.png")
            if (iconFile.exists()) {
                iconFile.delete()
            }
            LOG.info("iconFile:" + iconFile.absolutePath)
            LOG.info("iconFile.getParentFile():" + iconFile.absoluteFile)
            iconFile.writeToFile(launcherData)
            val backIconPath = "files" + File.separator + iconFile.readRelativePath(KEY_UPLOAD)
            LOG.info("saveLauncherIcon>backIconPath:$backIconPath")
            return backIconPath
        } catch (e: java.lang.Exception) {
            LOG.info("saveLauncherIcon:${e.stackTraceToString()}")
            e.printStackTrace()
            return null
        }
    }

    private fun saveRealFile(
        model: AppInfo,
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
            val fileName = model.name + "_" + model.versionName + "_" + model.versionCode +
                    "_" + model.buildType + "_" + formatCurTimeSecond + "." + fileExtension
            File(outputParent, fileName)
        }
        file.copyToFile(appFile) //保存文件
        val filePath = "files" + File.separator + appFile.readRelativePath(KEY_UPLOAD)
        model.filePath = filePath
        model.fileName = filePath
        model.downloadUrl = filePath
        return filePath
    }
}