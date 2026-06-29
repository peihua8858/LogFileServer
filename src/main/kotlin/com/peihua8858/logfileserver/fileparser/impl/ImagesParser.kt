package com.peihua8858.logfileserver.fileparser.impl

import com.example.ServiceApplication
import com.example.entity.primary.AppInfoExample
import com.peihua8858.logfileserver.fileparser.IParser
import com.peihua8858.logfileserver.fileparser.Parameter
import com.example.utils.Utils
import java.io.File
import javax.imageio.ImageIO

class ImagesParser : AbstractParser() {
    override fun onParser(parameter: Parameter): AppInfoExample {
        val appPath = parameter.file
        val app = AppInfoExample()
        app.platform = "images"
        app.fileName = appPath.name
        app.filePath = appPath.absolutePath
        uploadFile(appPath, app, parameter.isOverwriteFile, "")
        return app
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