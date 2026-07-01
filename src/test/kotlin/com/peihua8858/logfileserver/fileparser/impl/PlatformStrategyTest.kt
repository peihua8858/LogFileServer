package com.peihua8858.logfileserver.fileparser.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PlatformStrategyTest {

    @Nested
    @DisplayName("PlatformStrategy.Default 默认实现")
    inner class DefaultStrategyTests {

        @Test
        fun `匹配扩展名时应返回platformDirectory子目录`() {
            // Arrange
            val strategy = PlatformStrategy.Default(
                platformName = "Android",
                platformDirectory = "android",
                extension = "apk"
            )
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".apk").toFile()

            try {
                // Act
                val result = strategy.createPlatformFile("application/octet-stream", "apk", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("android", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `大写扩展名也应匹配（忽略大小写）`() {
            // Arrange
            val strategy = PlatformStrategy.Default(
                platformName = "Android",
                platformDirectory = "android",
                extension = "apk"
            )
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".APK").toFile()

            try {
                // Act
                val result = strategy.createPlatformFile("application/octet-stream", "APK", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("android", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `不匹配扩展名时应返回null`() {
            // Arrange
            val strategy = PlatformStrategy.Default(
                platformName = "Android",
                platformDirectory = "android",
                extension = "apk"
            )
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".txt").toFile()

            try {
                // Act
                val result = strategy.createPlatformFile("text/plain", "txt", tempFile, parentDir)

                // Assert
                assertNull(result)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `platformName应返回构造时传入的值`() {
            // Arrange
            val strategy = PlatformStrategy.Default(
                platformName = "iOS",
                platformDirectory = "iOS",
                extension = "ipa"
            )

            // Assert
            assertEquals("iOS", strategy.platformName)
        }

        @Test
        fun `platformDirectory应返回构造时传入的值`() {
            // Arrange
            val strategy = PlatformStrategy.Default(
                platformName = "iOS",
                platformDirectory = "iOS",
                extension = "ipa"
            )

            // Assert
            assertEquals("iOS", strategy.platformDirectory)
        }

        @Test
        fun `extension应返回构造时传入的值`() {
            // Arrange
            val strategy = PlatformStrategy.Default(
                platformName = "iOS",
                platformDirectory = "iOS",
                extension = "ipa"
            )

            // Assert
            assertEquals("ipa", strategy.extension)
        }
    }

    @Nested
    @DisplayName("各解析器的 platformStrategy 配置")
    inner class ParserStrategyTests {

        @Test
        fun `ApkParser 策略应配置正确`() {
            // Arrange & Act
            val strategy = ApkParser().platformStrategy()

            // Assert
            assertEquals(ApkParser.PLATFORM, strategy.platformName)
            assertEquals(ApkParser.PARENT_FILE, strategy.platformDirectory)
            assertEquals("apk", strategy.extension)
        }

        @Test
        fun `IpaParser 策略应配置正确`() {
            // Arrange & Act
            val strategy = IpaParser().platformStrategy()

            // Assert
            assertEquals(IpaParser.PLATFORM, strategy.platformName)
            assertEquals(IpaParser.PARENT_FILE, strategy.platformDirectory)
            assertEquals("ipa", strategy.extension)
        }

        @Test
        fun `ImagesParser 策略 platformName 应为 Images`() {
            // Arrange & Act
            val strategy = ImagesParser().platformStrategy()

            // Assert
            assertEquals(ImagesParser.PLATFORM, strategy.platformName)
            assertEquals(ImagesParser.PLATFORM_DIRECTORY, strategy.platformDirectory)
        }

        @Test
        fun `OtherParser 策略 platformName 应为 Other`() {
            // Arrange & Act
            val strategy = OtherParser().platformStrategy()

            // Assert
            assertEquals(OtherParser.PLATFORM, strategy.platformName)
            assertEquals(OtherParser.PLATFORM_DIRECTORY, strategy.platformDirectory)
        }
    }
}
