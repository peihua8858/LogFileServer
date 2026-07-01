package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.entity.filemeta.ImageFileModel
import com.peihua8858.logfileserver.fileparser.Parameter
import org.springframework.stereotype.Component
import java.io.File
import javax.imageio.ImageIO

@Component
class ImagesParser : AbstractFileParser<ImageFileModel>() {

    override fun platformStrategy(): PlatformStrategy = PlatformStrategy.Default(
        platformName = PLATFORM,
        platformDirectory = PLATFORM_DIRECTORY,
        extension = ""
    )

    override fun supports(extensionName: String, contentType: String): Boolean {
        return contentType.startsWith("image/", ignoreCase = true)
                || extensionName.equals("jpg", ignoreCase = true)
                || extensionName.equals("png", ignoreCase = true)
                || extensionName.equals("jpeg", ignoreCase = true)
                || extensionName.equals("gif", ignoreCase = true)
                || extensionName.equals("bmp", ignoreCase = true)
    }
    override fun order(): Int = 2
    override fun onParser(parameter: Parameter,dirFile: File): Pair<ImageFileModel, ByteArray?> {
        val appPath = parameter.file
        val app = ImageFileModel()
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
        parentFile: File,
    ): File? {
        val image = ImageIO.read(file)
        return if (contentType.startsWith("image/") || image != null) {
            File(parentFile, "images")
        } else {
            File(parentFile, fileExtension)
        }
    }

    companion object {
        const val PLATFORM = "Images"
        const val PLATFORM_DIRECTORY = "images"
    }
}