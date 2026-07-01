package com.peihua8858.logfileserver.configs

import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File

@ConfigurationProperties(prefix = "app")
class AppProperties(
    /**
     * 应用数据根目录
     */
    dataDir: String = "./data"
){
    val dataDir: String = File(dataDir).absolutePath
    val dataDirFile: File
        get() = File(dataDir)
}
