package com.peihua8858.logfileserver.services.appinfo

import com.peihua8858.logfileserver.fileparser.ResultData
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.multipart.MultipartFile

interface SaveAppFileService {
    fun saveAppFiles(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean,
        files: Array<MultipartFile>
    ): Map<String, MutableList<ResultData>>

    fun saveAppFile(
        request: HttpServletRequest,
        desc: String,
        buildType: String,
        isOverWrite: Boolean,
        isOnlyUploadFile: Boolean, file: MultipartFile
    ): ResultData
}
