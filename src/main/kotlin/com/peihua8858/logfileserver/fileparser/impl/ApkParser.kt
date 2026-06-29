package com.peihua8858.logfileserver.fileparser.impl

import com.fz.common.utils.toInteger
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.Parameter
import com.peihua8858.logfileserver.fileparser.exception.PackageParseException
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.AdaptiveIcon
import net.dongliu.apk.parser.bean.Icon
import net.dongliu.apk.parser.bean.IconFace
import java.io.File

/**
 * android 解析Apk
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/16 15:20
 */
internal class ApkParser : AbstractParser() {
    @Throws(PackageParseException::class)
    override fun onParser(parameter: Parameter): Pair<AppInfo, ByteArray?> {
        val androidParentPath = PARENT_FILE + File.separator
        val androidParentFile = File(classPathFile, androidParentPath)
        if (!androidParentFile.exists()) {
            androidParentFile.mkdirs()
        }
        val appPath = parameter.file
        val app = AppInfo()
        if (parameter.buildType.isEmpty()) {
            app.buildType = "Develop"
        } else {
            app.buildType = parameter.buildType
        }
        val path = appPath.path
        if (path.isNullOrEmpty()) {
            throw NullPointerException("参数为空。")
        }
        app.platform = "Android"
        println("PluginRemote--- >$path")
        return ApkFile(File(path)).use { apkParser ->
            val apkMeta = apkParser.apkMeta
            val iconFaces = apkParser.allIcons
            val parseIcon = ParseIcon(iconFaces).invoke()
            val iconPath = parseIcon.iconPath
            var iconData = parseIcon.iconData
            if (iconPath != null) {
                println("PluginIcon--- >$iconPath")
            }
            println("PluginVersionCode--- >" + apkMeta.versionCode)
            println("PluginVersionName--- >" + apkMeta.versionName)
            app.name = apkMeta.name
            app.projectName = apkMeta.name
            app.bundleId = apkMeta.packageName
            app.versionCode = apkMeta.versionCode.toInteger()
            app.versionName = apkMeta.versionName
            if (iconPath.isNullOrEmpty()) {
                iconData = null
//                saveLauncherIcon(iconData, app)
            }
            uploadFile(appPath, app)
            if (app.buildType.isNullOrEmpty()) {
                app.buildType = "Develop"
            }
            return@use app to iconData
        }
//        return try {
//
//
//
//        } catch (e: Exception) {
//            throw PackageParseException("Unfortunately, an error has occurred while processing parse android app " +
//                    "file", e)
//        } finally {
//            try(apkParser?.close()){
//
//            }
//        }
    }

    override fun createPlatformFile(
        contentType: String,
        fileExtension: String,
        file: File,
        parentFile: File
    ): File? {
       return if ("apk".equals(fileExtension, ignoreCase = true)) {
            File(parentFile, "android")
        } else null
    }

    private inner class ParseIcon(private val iconFaces: List<IconFace>?) {
        var iconPath: String? = null
            private set
        var iconData: ByteArray? = null
            private set

        operator fun invoke(): ParseIcon {
            if (!iconFaces.isNullOrEmpty()) {
                var density = 0
                for (iconFace in iconFaces) {
                    val iconPathTm = iconFace.path
                    if (iconPathTm.endsWith(".png") || iconPathTm.endsWith(".PNG")
                        || iconPathTm.endsWith(".jpg") || iconPathTm.endsWith(".jpeg")
                    ) {
                        println("PluginIcon--- >" + iconFace.path)
                        if (iconFace is Icon) {
                            if (density < iconFace.density) {
                                density = iconFace.density
                                iconPath = iconFace.path
                                iconData = iconFace.data
                            }
                        } else if (iconFace is AdaptiveIcon) {
                            val foregroundIcon = iconFace.foreground
                            if (density < foregroundIcon.density) {
                                density = foregroundIcon.density
                                iconPath = foregroundIcon.path
                                iconData = foregroundIcon.data
                            }
                        }
                    }
                }
            }
            return this
        }

    }

    companion object {
        const val PARENT_FILE = "android"
    }
}