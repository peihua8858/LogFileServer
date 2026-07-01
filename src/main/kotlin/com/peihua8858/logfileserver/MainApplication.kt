package com.peihua8858.logfileserver

import com.peihua8858.logfileserver.configs.AppProperties
import com.peihua8858.logfileserver.data.DataStore
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


@SpringBootApplication
@EnableConfigurationProperties(
    AppProperties::class
)
class MainApplication

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}

@Component
class DataDirectoryInitializer(
    private final val properties: AppProperties
) {
    @PostConstruct
    @Throws(IOException::class)
    fun init() {
        val root = Path.of(properties.dataDir).toAbsolutePath().normalize()
        Files.createDirectories(root)
        Files.createDirectories(root.resolve(DataStore.FILES_DIR))
        Files.createDirectories(root.resolve(DataStore.DATABASE_DIR))
        Files.createDirectories(root.resolve(DataStore.STORAGE_DIR))
        Files.createDirectories(root.resolve("${DataStore.STORAGE_DIR}/${DataStore.PUBLIC_DIR}"))
        Files.createDirectories(root.resolve("${DataStore.STORAGE_DIR}/${DataStore.PRIVATE_DIR}"))
        Files.createDirectories(root.resolve(DataStore.TEMP_DIR))
        Files.createDirectories(root.resolve(DataStore.LOGS_DIR))
        Files.createDirectories(root.resolve(DataStore.QRCODE_DIR))
    }
}