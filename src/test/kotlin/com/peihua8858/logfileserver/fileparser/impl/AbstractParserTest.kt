package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.entity.FileModel
import com.peihua8858.logfileserver.fileparser.Parameter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AbstractParserTest {

    /**
     * 用于测试的简单 FileModel 实现
     */
    private class StubFileModel : FileModel {
        override var platform: String? = null
        override var fileName: String? = null
        override var filePath: String? = null
        override var fileSize: Long? = null
        override var iconPath: String? = null
        override var buildDescription: String? = null
        override var bundleId: String? = null
        override var versionName: String? = null
        override var versionCode: Int? = null
        override var name: String? = null
        override var buildType: String? = null
        override var downloadUrl: String? = null
    }

    /**
     * 用于测试的具体解析器实现，使用 Default 策略
     */
    private class TestParser : AbstractFileParser<StubFileModel>() {
        override fun platformStrategy(): PlatformStrategy = PlatformStrategy.Default(
            platformName = "Test",
            platformDirectory = "test",
            extension = "tst"
        )

        override fun supports(extensionName: String, contentType: String): Boolean {
            return "tst".equals(extensionName, ignoreCase = true)
        }

        override fun onParser(parameter: Parameter, dirFile: File): Pair<StubFileModel, ByteArray?> {
            return StubFileModel() to null
        }
    }

    /**
     * 用于测试自定义 createPlatformFile 重写的解析器
     */
    private class CustomParser : AbstractFileParser<StubFileModel>() {
        override fun platformStrategy(): PlatformStrategy = PlatformStrategy.Default(
            platformName = "Custom",
            platformDirectory = "custom",
            extension = "cst"
        )

        override fun supports(extensionName: String, contentType: String): Boolean {
            return "cst".equals(extensionName, ignoreCase = true) || "special".equals(extensionName, ignoreCase = true)
        }

        override fun onParser(parameter: Parameter, dirFile: File): Pair<StubFileModel, ByteArray?> {
            return StubFileModel() to null
        }

        override fun createPlatformFile(
            contentType: String,
            fileExtension: String,
            file: File,
            parentFile: File
        ): File? {
            return if (fileExtension == "special") {
                File(parentFile, "special-dir")
            } else {
                super.createPlatformFile(contentType, fileExtension, file, parentFile)
            }
        }
    }

    @Nested
    @DisplayName("createPlatformFile 默认委托给策略")
    inner class CreatePlatformFileTests {

        @Test
        fun `匹配扩展名时应返回策略定义的平台目录`() {
            // Arrange
            val parser = TestParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".tst").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "tst", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("test", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `大写扩展名也应匹配`() {
            // Arrange
            val parser = TestParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".TST").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "TST", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("test", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `不匹配扩展名时应返回null`() {
            // Arrange
            val parser = TestParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".xyz").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "xyz", tempFile, parentDir)

                // Assert
                assertNull(result)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Nested
    @DisplayName("子类重写 createPlatformFile")
    inner class OverrideCreatePlatformFileTests {

        @Test
        fun `自定义逻辑应优先于默认策略`() {
            // Arrange
            val parser = CustomParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".special").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "special", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("special-dir", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `未匹配自定义逻辑时应回退到默认策略`() {
            // Arrange
            val parser = CustomParser()
            val parentDir = createTempFile().toFile().parentFile
            val tempFile = createTempFile(suffix = ".cst").toFile()

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "cst", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("custom", result.name)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Nested
    @DisplayName("generateParentFile 目录生成")
    inner class GenerateParentFileTests {

        @Test
        fun `不匹配的扩展名应抛出 IllegalArgumentException`() {
            // Arrange
            val parser = TestParser()
            val tempFile = createTempFile(suffix = ".xyz").toFile()

            try {
                // Act & Assert
                assertFailsWith<IllegalArgumentException> {
                    parser.generateParentFile(
                        bundleId = null,
                        isOnlyUploadFile = true,
                        contentType = "application/octet-stream",
                        fileExtension = "xyz",
                        dirFile = tempFile.parentFile,
                        file = tempFile
                    )
                }
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `isOnlyUploadFile为true时应返回平台目录`() {
            // Arrange
            val parser = TestParser()
            val tempFile = createTempFile(suffix = ".tst").toFile()

            try {
                // Act
                val result = parser.generateParentFile(
                    bundleId = "com.example.app",
                    isOnlyUploadFile = true,
                    contentType = "application/octet-stream",
                    fileExtension = "tst",
                    dirFile = tempFile.parentFile,
                    file = tempFile
                )

                // Assert
                assertNotNull(result)
                assertEquals("test", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `bundleId为空时应返回平台目录`() {
            // Arrange
            val parser = TestParser()
            val tempFile = createTempFile(suffix = ".tst").toFile()

            try {
                // Act
                val result = parser.generateParentFile(
                    bundleId = "",
                    isOnlyUploadFile = false,
                    contentType = "application/octet-stream",
                    fileExtension = "tst",
                    dirFile = tempFile.parentFile,
                    file = tempFile
                )

                // Assert
                assertNotNull(result)
                assertEquals("test", result.name)
            } finally {
                tempFile.delete()
            }
        }

        @Test
        fun `有bundleId时应在平台目录下创建bundle子目录`() {
            // Arrange
            val parser = TestParser()
            val tempFile = createTempFile(suffix = ".tst").toFile()

            try {
                // Act
                val result = parser.generateParentFile(
                    bundleId = "com.example.app",
                    isOnlyUploadFile = false,
                    contentType = "application/octet-stream",
                    fileExtension = "tst",
                    dirFile = tempFile.parentFile,
                    file = tempFile
                )

                // Assert
                assertNotNull(result)
                // 父目录应为平台目录
                assertEquals("test", result.parentFile.name)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Nested
    @DisplayName("platformStrategy 接口验证")
    inner class PlatformStrategyInterfaceTests {

        @Test
        fun `TestParser 的 platformStrategy 应返回非空策略`() {
            // Arrange & Act
            val parser = TestParser()
            val strategy = parser.platformStrategy()

            // Assert
            assertEquals("Test", strategy.platformName)
            assertEquals("test", strategy.platformDirectory)
            assertEquals("tst", strategy.extension)
        }
    }
}
