package com.peihua8858.logfileserver.configs

import com.peihua8858.logfileserver.data.DataStore
import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File

@ConfigurationProperties(prefix = "app")
class AppProperties(
    /**
     * 应用数据根目录
     */
    val dataDir: String ="./${DataStore.DATA_DIR}"
){
    val dataDirFile: File
        get() = File(dataDir)
}
