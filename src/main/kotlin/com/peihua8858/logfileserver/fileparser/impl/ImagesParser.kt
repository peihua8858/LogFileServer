package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.Parameter
import java.io.File
import javax.imageio.ImageIO

class ImagesParser : AbstractParser() {
    override fun onParser(parameter: Parameter): Pair<AppInfo, ByteArray?> {
        val appPath = parameter.file
        val app = AppInfo()
        app.platform = "images"
        app.fileName = appPath.name
        app.filePath = appPath.absolutePath
        uploadFile(appPath, app, parameter.isOverwriteFile, "")
        return app to null
    }

    override fun createPlatformFile(
        contentType: String,
        fileExtension: String,
        file: File,
        parentFile: File,
    ): File? {
        val image = ImageIO.read(file)
       return if (contentType.startsWith("image/") || image != null) {
            File(file, "images")
        } else {
            File(file, fileExtension)
        }
    }
}