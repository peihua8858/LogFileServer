package com.peihua8858.logfileserver.services.appinfo.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.fz.common.file.copyToFile
import com.fz.common.file.deleteFile
import com.fz.common.file.writeToFile
import com.fz.common.text.isNonEmpty
import com.fz.common.utils.toBoolean
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.IParser
import com.peihua8858.logfileserver.fileparser.Parameter
import com.peihua8858.logfileserver.fileparser.ResultData
import com.peihua8858.logfileserver.mappers.appinfo.AppInfoMapper
import com.peihua8858.logfileserver.services.SaveService
import com.peihua8858.logfileserver.services.SaveService.Companion.KEY_UPLOAD
import com.peihua8858.logfileserver.services.appinfo.AppInfosService
import com.peihua8858.logfileserver.services.appinfo.SaveAppFileService
import com.peihua8858.logfileserver.utils.extension
import com.peihua8858.logfileserver.utils.isValidVersion
import com.peihua8858.logfileserver.utils.sourceFile
import com.peihua8858.logfileserver.utils.toJSONString
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Service
class SaveAppFileServiceImpl : ServiceImpl<AppInfoMapper, AppInfo>(), SaveAppFileService, SaveService {
    companion object {
        private val LOG = LoggerFactory.getLogger(SaveAppFileServiceImpl::class.java)
    }

    override fun saveAppFiles(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        files: Array<MultipartFile>
    ): Map<String, MutableList<ResultData>> {
        val result = mutableListOf<ResultData>()
        files.forEach {
            val metadata = mutableMapOf<String, String>()
            metadata["Content-Type"] = it.contentType ?: "image/*"
            metadata["Content-Length"] = it.size.toString()
            val resultData = saveAppFile(request, desc, buildType, isOverWrite, isOnlyUploadFile, it)
            val url = "http://$serverHost${File.separator}${resultData.url}"
            if (resultData.thumbnailUrl.isNonEmpty()) {
                val iconUrl = "http://$serverHost${File.separator}${resultData.thumbnailUrl}"
                resultData.thumbnailUrl = iconUrl
            }
            resultData.url = url
            result.add(resultData)
        }
        val res = mutableMapOf<String, MutableList<ResultData>>()
        res["files"] = result
    }

    override fun saveAppFile(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        file: MultipartFile
    ): ResultData {
        if (file.isEmpty) {
            return ResultData(error = "Multiple request parameters are invalid.")
        }
        val contentType = file.contentType
            ?: return ResultData(error = "Content-Type is not support.")
        val parameter = Parameter(transFileToTemp(file, contentType), buildType, desc, isOnlyUploadFile, isOverWrite)
        val extensionName = file.originalFilename?.extension ?: "apk"
        val parser = IParser.createParser(extensionName, contentType)
        val result = parser.onParser(parameter) ?: return ResultData(error = "upload file failure.")
        result.buildDescription = desc
        val versionName = result.versionName
        LOG.info(result.toJSONString())
        if (!isOnlyUploadFile) {
            if (!versionName.isValidVersion()) {
                return ResultData(error = "version number \"$versionName\" is invalid.Please see Semantic Versioning 2.0.0: https://semver.org/lang/zh-CN/")
            }
        }
        //生成输出文件父级目录
        val outputParent =
            parser.generateParentFile(result.bundleId, isOnlyUploadFile, contentType, extensionName, parameter.file)
        LOG.info("outputParent>>>$outputParent")
        //保存app 桌面图标
        if (!isOnlyUploadFile) {
            saveLauncherIcon(result, outputParent)
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
        )

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
            val backIconPath = "files" + File.separator + iconFile.readRelativePath()
            LOG.info("backIconPath:$backIconPath")
            return backIconPath
        } catch (e: java.lang.Exception) {
            LOG.info("backIconPath:" + XUtils.getStackTraceMessage(e))
            e.printStackTrace()
            return null
        }
    }

    private fun saveRealFile(
        model: AppInfoExample,
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
                File(outputParent, XUtils.renameFile(file.name) + "." + fileExtension)
            }
        } else {
            val fileName = createFileName(
                model.name.trim { it <= ' ' },
                model.buildType.trim { it <= ' ' },
                model.versionName.trim { it <= ' ' },
                model.versionCode,
                fileExtension,
                file.name.trim { it <= ' ' })
            File(outputParent, fileName)
        }
        file.copyToFile(appFile) //保存文件
        val filePath = "files" + File.separator + appFile.readRelativePath()
        model.filePath = filePath
        model.fileName = filePath
        model.downloadUrl = filePath
        return filePath
    }

    private fun transFileToTemp(file: MultipartFile, contentType: String): File {
        val parentFile = readFileFolder(".temps")
        val outputFile = File(parentFile, file.originalFilename ?: createFileName(contentType))
        file.transferTo(outputFile)
        return outputFile
    }

    fun File.readRelativePath(): String {
        val path = absolutePath
        return path.substring(path.indexOf(KEY_UPLOAD))
    }
}