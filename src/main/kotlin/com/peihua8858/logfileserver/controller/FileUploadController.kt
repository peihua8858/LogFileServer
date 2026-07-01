package com.peihua8858.logfileserver.controller

import com.fz.common.utils.toBoolean
import com.peihua8858.logfileserver.entity.Response
import com.peihua8858.logfileserver.services.filemeta.SaveFileService
import com.peihua8858.logfileserver.utils.isEmptyMessage
import com.peihua8858.logfileserver.utils.isValidVersion
import com.peihua8858.logfileserver.utils.toJSONString
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

/**
 * 文件上传下载处理
 */
@Controller
@RequestMapping("/")
class FileUploadController @Autowired constructor(private val saveFileService: SaveFileService) {
    private val LOG = LoggerFactory.getLogger(FileUploadController::class.java)
    @GetMapping("/upload/index")
    fun index(): String {
        return "uploadFile/index"
    }
    /**
     * Jenkins上传APP安装包文件或图标文件
     *
     * @param bundleId    安装包包名，不能为空
     * @param appName     项目名
     * @param versionName 版本名称
     * @param versionCode 版本号
     * @param buildType   编译类型，如：release
     * @param platform    系统平台，Android或iOS
     * @param file        上传的文件
     * @return
     * @throws IOException
     */
    @PostMapping("upload/jenkins_file")
    @ResponseBody
    fun jenkinsUploadFile(
        request: HttpServletRequest,
        @RequestParam("bundleId") bundleId: String?,
        @RequestParam("appName") appName: String?,
        @RequestParam("versionName") versionName: String?,
        @RequestParam("versionCode") versionCode: String?,
        @RequestParam("buildType") buildType: String?,
        @RequestParam("platform") platform: String?,
        @RequestParam("file") file: MultipartFile
    ): Response<String> {
        if (file.isEmpty) {
            return Response.failed("Multiple file are invalid.")
        }
        if (bundleId.isNullOrEmpty() || appName.isNullOrEmpty()
            || buildType.isNullOrEmpty() || versionName.isNullOrEmpty()
            || versionCode.isNullOrEmpty() || platform.isNullOrEmpty()
        ) {
            return Response.failed(
                isEmptyMessage(
                    bundleId, appName,
                    buildType, versionName, versionCode, platform
                )
            )
        }
        if (!versionName.isValidVersion()) {
            return Response.failed(
                ("version number \"" + versionName + "\" is invalid.Please see Semantic Versioning "
                        + "2.0.0: https://semver.org/lang/zh-CN/")
            )
        }
        try {
            val result = saveFileService.saveAppFile(
                request, bundleId, appName,
                versionName, versionCode, buildType, platform, file
            )
            return Response.ok(result.url)
        } catch (e: Exception) {
            return Response.failed(e.message ?: "upload file failed")
        }
    }

    /**
     *
     * 前端页面直接上传文件
     * @author dingpeihua
     * @date 2026/6/30 13:52
     **/
    @RequestMapping("upload/files")
    @ResponseBody
    fun uploadFile(
        request: HttpServletRequest,
        @RequestParam data: Map<String, String>,
        @RequestParam("files") files: Array<MultipartFile>
    ): String {
        val desc: String = data["desc"] ?: ""
        val buildType: String = data["buildType"] ?: ""
        val isOverWrite: Boolean = data["overWrite"].toBoolean()
        val isOnlyUploadFile: Boolean = data["isOnlyUploadFile"].toBoolean()
        LOG.info("file upload start,desc:$desc,buildType:$buildType,isOverWrite:$isOverWrite,isOnlyUploadFile:$isOnlyUploadFile,files:$files")
        LOG.info("file upload start")
        val result = saveFileService.parserAndSaveFiles(request, desc, buildType, isOverWrite, isOnlyUploadFile, files)
        LOG.info("file upload end,result:${result.toJSONString()}")
        return result.toJSONString()
    }

}