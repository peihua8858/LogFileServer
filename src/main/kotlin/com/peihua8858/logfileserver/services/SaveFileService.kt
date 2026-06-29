package com.peihua8858.logfileserver.services

import org.springframework.web.multipart.MultipartFile

interface SaveService {
         val KEY_CONFIGS = "configs"
         val KEY_UPLOAD = "upload"
         val KEY_KEYSTORE = "keystore"
//    fun saveOtherFile(data: Map<String, String>, file: MultipartFile)
//    fun saveOtherFiles(data: Map<String, String>, files: Array<MultipartFile>)
}