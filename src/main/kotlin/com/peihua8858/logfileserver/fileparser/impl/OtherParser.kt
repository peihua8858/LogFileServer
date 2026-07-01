package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.entity.filemeta.OtherFileModel
import com.peihua8858.logfileserver.fileparser.Parameter
import org.springframework.stereotype.Component
import java.io.File

@Component
class OtherParser : AbstractFileParser<OtherFileModel>() {

    override fun platformStrategy(): PlatformStrategy = PlatformStrategy.Default(
        platformName = PLATFORM,
        platformDirectory = PLATFORM_DIRECTORY,
        extension = ""
    )

    override fun supports(extensionName: String, contentType: String): Boolean {
        return true
    }

    override fun order(): Int = Int.MAX_VALUE  // 最低优先级
    override fun onParser(parameter: Parameter ,dirFile: File): Pair<OtherFileModel, ByteArray?> {
        val appPath = parameter.file
        val app = OtherFileModel()
        app.platform = PLATFORM
        app.fileName = appPath.name
        app.filePath = appPath.absolutePath
        setFileInfo(appPath, app)
        return app to null
    }

    override fun createPlatformFile(
        contentType: String,
        fileExtension: String,
        file: File,
        parentFile: File
    ): File {
        return when {
            "apk".equals(fileExtension, ignoreCase = true) || "ipa".equals(fileExtension, ignoreCase = true)
                -> throw IllegalArgumentException("OtherParser 不处理 apk/ipa 文件类型，请使用对应的专用解析器。")

            "txt".equals(fileExtension, ignoreCase = true) -> File(parentFile, "txt")
            "pdf".equals(fileExtension, ignoreCase = true) -> File(parentFile, "pdf")
            "html".equals(fileExtension, ignoreCase = true) || "htm".equals(fileExtension, ignoreCase = true) -> File(
                parentFile,
                "html"
            )

            "docx".equals(fileExtension, ignoreCase = true) || "doc".equals(fileExtension, ignoreCase = true)
                    || "docm".equals(fileExtension, ignoreCase = true) -> File(parentFile, "docx")

            "xls".equals(fileExtension, ignoreCase = true) || "xlsx".equals(fileExtension, ignoreCase = true)
                    || "xlsm".equals(fileExtension, ignoreCase = true) -> File(parentFile, "xlsx")

            "zip".equals(fileExtension, ignoreCase = true) || "7z".equals(fileExtension, ignoreCase = true)
                    || "rar".equals(fileExtension, ignoreCase = true) -> File(parentFile, "zip")

            contentType.startsWith("image/", ignoreCase = true) -> File(parentFile, "images")
            else -> File(parentFile, fileExtension.ifEmpty { "other" })
        }
    }

    companion object {
        const val PLATFORM = "Other"
        const val PLATFORM_DIRECTORY = "other"
    }
}
