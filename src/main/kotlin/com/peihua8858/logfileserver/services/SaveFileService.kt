package com.peihua8858.logfileserver.services

import org.springframework.web.multipart.MultipartFile

interface SaveService {
    companion object {
        const val KEY_CONFIGS = "configs"
        const val KEY_UPLOAD = "upload"
        const val KEY_KEYSTORE = "keystore"
    }
//    fun saveOtherFile(data: Map<String, String>, file: MultipartFile)
//    fun saveOtherFiles(data: Map<String, String>, files: Array<MultipartFile>)
}