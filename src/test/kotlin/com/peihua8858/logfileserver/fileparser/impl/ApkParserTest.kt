package com.peihua8858.logfileserver.fileparser.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ApkParserTest {

    @Nested
    @DisplayName("createPlatformFile 平台目录创建")
    inner class CreatePlatformFileTests {

        @Test
        fun `对apk扩展名应返回android目录`() {
            // Arrange
            val parser = ApkParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".apk").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/vnd.android.package-archive", "apk", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("android", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `对APK大写扩展名应返回android目录`() {
            // Arrange
            val parser = ApkParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".APK").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "APK", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("android", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `对非apk扩展名应返回null`() {
            // Arrange
            val parser = ApkParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".txt").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("text/plain", "txt", tempFile, parentDir)

                // Assert
                assertNull(result)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Nested
    @DisplayName("ApkParser 常量")
    inner class Constants {

        @Test
        fun `PARENT_FILE应为android`() {
            // Assert
            assertEquals("android", ApkParser.PARENT_FILE)
        }

        @Test
        fun `PLATFORM应为Android`() {
            // Assert
            assertEquals("Android", ApkParser.PLATFORM)
        }
    }
}
