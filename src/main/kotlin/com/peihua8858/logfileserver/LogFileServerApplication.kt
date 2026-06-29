package com.peihua8858.logfileserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LogFileServerApplication

fun main(args: Array<String>) {
    runApplication<LogFileServerApplication>(*args)
}
