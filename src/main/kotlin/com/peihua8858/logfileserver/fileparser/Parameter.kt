package com.peihua8858.logfileserver.fileparser

import java.io.File

data class Parameter(
    val file: File, var buildType: String, var desc: String?, var isOnlyUploadFile: Boolean = false,
    var isOverwriteFile: Boolean = false
) {

}