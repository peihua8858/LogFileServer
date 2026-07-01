package com.peihua8858.logfileserver.services.filemeta

import com.peihua8858.logfileserver.fileparser.ResultData
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.multipart.MultipartFile

interface SaveFileService {
    fun parserAndSaveFiles(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        files: Array<MultipartFile>
    ): Map<String, MutableList<ResultData>>

    fun parserAndSaveFile(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        file: MultipartFile
    ): ResultData

    fun saveAppFiles(
        request: HttpServletRequest,
        bundleId: String,
        appName: String,
        versionName: String,
        versionCode: String,
        buildType: String,
        platform: String,
        files: Array<MultipartFile>
    ): Map<String, MutableList<ResultData>>

    fun saveAppFile(
        request: HttpServletRequest,
        bundleId: String,
        appName: String,
        versionName: String,
        versionCode: String,
        buildType: String,
        platform: String,
        file: MultipartFile
    ): ResultData
}