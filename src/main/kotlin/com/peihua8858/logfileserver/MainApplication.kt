package com.peihua8858.logfileserver

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


@SpringBootApplication
class MainApplication

fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}

@Component
class DbDirectoryInitializer {
    @PostConstruct
    @Throws(IOException::class)
    fun init() {
        val dir: Path = Paths.get("./data")
        if (Files.notExists(dir)) {
            Files.createDirectories(dir)
        }
    }
}