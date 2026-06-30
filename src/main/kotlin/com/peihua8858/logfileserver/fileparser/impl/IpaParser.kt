package com.peihua8858.logfileserver.fileparser.impl

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import com.fz.common.file.deleteFileOrDir
import com.fz.common.file.read
import com.fz.common.text.isNonEmpty
import com.fz.common.utils.toInteger
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.Parameter
import com.peihua8858.logfileserver.fileparser.exception.PackageParseException
import com.peihua8858.logfileserver.fileparser.png.IPngConverter
import net.lingala.zip4j.ZipFile
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO


internal class IpaParser : AbstractParser<AppInfo>() {
    private fun inputStream2String(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val buffer = StringBuffer()
        var line = ""
        try {
            while (reader.readLine().also { line = it } != null) {
                buffer.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buffer.toString()
    }

    @Throws(PackageParseException::class, IOException::class)
    override fun onParser(parameter: Parameter): Pair<AppInfo, ByteArray?> {
        val iOSParentPath = PARENT_FILE + File.separator
        val iOSParentFile = File(classPathFile, iOSParentPath)
        if (!iOSParentFile.exists()) {
            iOSParentFile.mkdirs()
        }
        val appPath = parameter.file
        val app = AppInfo()
        val result = parse(app, appPath)
        if (parameter.buildType.isNonEmpty()) {
            app.buildType = parameter.buildType
        } else if (app.buildType.isNullOrEmpty()) {
            app.buildType = "Adhoc"
        }
        app.platform = PLATFORM
        uploadFile(appPath, app)
        return result
    }


    @Throws(PackageParseException::class)
    private fun parse(model: AppInfo, ipaFile: File): Pair<AppInfo, ByteArray?> {
        try {
            val sysTemp = System.getProperty("java.io.tmpdir")
            val dir = File(sysTemp + File.separator + "ipaparser")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val path = dir.path
            val zipFile = ZipFile(ipaFile.absoluteFile)
            println(path)
            zipFile.extractAll(path)
            val payloadFolder = File(path + File.separator + "Payload")
            val files: Array<File>? = payloadFolder.listFiles()
            val appFolder = files?.get(0)
            println("App Folder: $appFolder")
            val infoPlist = File(appFolder.toString() + File.separator + "Info.plist")
            val rootDict = PropertyListParser.parse(infoPlist) as NSDictionary
            val file = File(appFolder.toString() + File.separator + "embedded.mobileprovision")
            if (file.exists()) {
                val str = read(FileInputStream(file)) ?: ""//inputStream2String(FileInputStream(file))
                val isAdHoc = str.contains("<key>ProvisionedDevices</key>")
                if (isAdHoc) {
                    model.buildType = "Adhoc"
                } else {
                    val isInhouse = str.contains("<key>ProvisionsAllDevices</key>")
                    if (isInhouse) {
                        model.buildType = "Inhouse"
                    }
                }
            }
            model.versionCode = rootDict.objectForKey("CFBundleVersion").toInteger()
            if (rootDict.objectForKey("CFBundleDisplayName") != null) {
                model.projectName = rootDict.objectForKey("CFBundleDisplayName").toString()
                model.name = rootDict.objectForKey("CFBundleDisplayName").toString()
            } else if (rootDict.objectForKey("AppIDName") != null) {
                model.projectName = rootDict.objectForKey("AppIDName").toString()
                model.name = rootDict.objectForKey("AppIDName").toString()
            } else {
                model.name = "Unkown Name"
                model.projectName = "Unkown Name"
            }
            model.bundleId = rootDict.objectForKey("CFBundleIdentifier").toString()
            model.versionName = rootDict.objectForKey("CFBundleShortVersionString").toString()
            var iconFile: File? = null
            if (rootDict.containsKey("CFBundleIconFiles")) {
                val iconFiles = (rootDict.objectForKey("CFBundleIconFiles") as NSArray).array
                if (iconFiles.isNotEmpty()) {
                    println("icons -------<" + appFolder + File.separator + iconFiles[0].toString())
                    iconFile = File(appFolder.toString() + File.separator + iconFiles[iconFiles.size - 1].toString())
                }
            }
            if (rootDict.containsKey("CFBundleIcons") && iconFile == null) {
                val cfIcons = rootDict.objectForKey("CFBundleIcons") as NSDictionary
                if (cfIcons.objectForKey("CFBundlePrimaryIcon") != null) {
                    val nd = cfIcons.objectForKey("CFBundlePrimaryIcon") as NSDictionary
                    if (nd.objectForKey("CFBundleIconFiles") != null) {
                        val nsArray = (nd.objectForKey("CFBundleIconFiles") as NSArray).array
                        if (nsArray.isNotEmpty()) {
                            iconFile = File(
                                appFolder.toString() + File.separator + nsArray[nsArray.size - 1].toString() +
                                        "@2x.png"
                            )
                        }
                    }
                }
            } else if (iconFile == null) {
                iconFile = File(appFolder.toString() + File.separator + "iTunesArtwork")
            }
            var iconData: ByteArray? = null
            try {
                if (iconFile?.exists() == true) {
                    val covertFile = File(appFolder.toString() + File.separator + "convertIcon.png")
                    IPngConverter(iconFile, covertFile).convert()
                    val iconImage = ImageIO.read(covertFile)
                    if (iconImage != null) {
                        val bytes = imageToBytes(iconImage, "png")
                        iconData = bytes
//                        saveLauncherIcon(bytes, model)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dir.deleteFileOrDir()
            return model to iconData
        } catch (e: Exception) {
            throw PackageParseException("Unfortunately, an error has occurred while processing parse ios app file", e)
        }
    }

    override fun createPlatformFile(
        contentType: String,
        fileExtension: String,
        file: File,
        parentFile: File
    ): File? {
        return if ("ipa".equals(fileExtension, ignoreCase = true)) {
            File(parentFile, "ios")
        } else null
    }

    companion object {
        const val PARENT_FILE = "iOS"
        const val PLATFORM = "iOS"

        @Throws(PackageParseException::class)
        fun imageToBytes(image: Image, format: String?): ByteArray {
            val bImage = BufferedImage(image.getWidth(null), image.getHeight(null), 2)
            val bg = bImage.graphics
            bg.drawImage(image, 0, 0, null)
            bg.dispose()
            val out = ByteArrayOutputStream()
            try {
                ImageIO.write(bImage, format, out)
            } catch (e: IOException) {
                throw PackageParseException(
                    "Unfortunately, an error has occurred while processing write ios icon " +
                            "file", e
                )
            }
            return out.toByteArray()
        }
    }
}