package com.peihua8858.logfileserver.fileparser

data class ResultData(
    val name: String?="",
    val size: Long=0,
    var thumbnailUrl: String="",
    val type: String?="",
    var url: String="",
    val error:String=""
)