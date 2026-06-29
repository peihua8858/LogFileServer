package com.peihua8858.logfileserver.fileparser.impl

import com.example.ServiceApplication
import com.example.ServiceApplication.Companion.readUploadFolder
import com.example.entity.primary.AppInfoExample
import com.peihua8858.logfileserver.fileparser.IParser
import com.peihua8858.logfileserver.fileparser.Parameter
import com.example.utils.Utils
import java.io.File
import javax.imageio.ImageIO

class OtherParser : IParser() {
    override fun onParser(parameter: Parameter): AppInfoExample {
        val appPath = parameter.file
        val app = AppInfoExample()
        app.platform = "other"
        app.fileName = appPath.name
        app.filePath = appPath.absolutePath
        uploadFile(appPath, app, parameter.isOverwriteFile, "")
        return app
    }

    override fun generateParentFile(
        bundleId: String?,
        isOnlyUploadFile: Boolean,
        contentType: String,
        fileExtension: String,
        file: File
    ): File {
        val otherFolder = ServiceApplication.readUploadFolder("other")
        //如果上传目录为/files/upload/[android/ios/other/images]/，则可以如下获取：
        val platformFile =  if ("apk".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "android")
        } else  if ("ipa".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "ios")
        } else if ("txt".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "txt")
        } else if ("pdf".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "pdf")
        } else if ("html".equals(fileExtension, ignoreCase = true) || "htm".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "html")
        } else if ("docx".equals(fileExtension, ignoreCase = true) || "doc".equals(fileExtension, ignoreCase = true)
            || "docm".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "docx")
        }else if ("xls".equals(fileExtension, ignoreCase = true) || "xlsx".equals(fileExtension, ignoreCase = true)
            || "xlsm".equals(fileExtension, ignoreCase = true)) {
            File(otherFolder, "xlsx")
        } else if ("zip".equals(fileExtension, ignoreCase = true) || "7z".equals(
                fileExtension,
                ignoreCase = true
            ) || "rar".equals(fileExtension, ignoreCase = true)
        ) {
            File(otherFolder, "zip")
        } else {
            val image = ImageIO.read(file)
            if (contentType.startsWith("image/") || image != null) {
                File(otherFolder, "images")
            } else {
                File(otherFolder, fileExtension)
            }
        }
        return if (isOnlyUploadFile || bundleId.isNullOrEmpty()) {
            platformFile
        } else {
            val bundlePath = Utils.createFilePath(bundleId)
            File(platformFile, bundlePath)
        }
    }
}