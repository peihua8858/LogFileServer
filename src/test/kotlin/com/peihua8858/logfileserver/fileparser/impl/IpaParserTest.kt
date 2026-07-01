package com.peihua8858.logfileserver.fileparser.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IpaParserTest {

    @Nested
    @DisplayName("createPlatformFile 平台目录创建")
    inner class CreatePlatformFileTests {

        @Test
        fun `对ipa扩展名应返回ios目录`() {
            // Arrange
            val parser = IpaParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".ipa").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "ipa", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("ios", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `对IPA大写扩展名应返回iOS目录`() {
            // Arrange
            val parser = IpaParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".IPA").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "IPA", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("ios", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `对非ipa扩展名应返回null`() {
            // Arrange
            val parser = IpaParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".apk").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "apk", tempFile, parentDir)

                // Assert
                assertNull(result)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Nested
    @DisplayName("IpaParser 常量")
    inner class Constants {

        @Test
        fun `PARENT_FILE应为ios`() {
            // Assert
            assertEquals("ios", IpaParser.PARENT_FILE)
        }

        @Test
        fun `PLATFORM应为ios`() {
            // Assert
            assertEquals("ios", IpaParser.PLATFORM)
        }
    }
}
