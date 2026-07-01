package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.entity.filemeta.OtherFileModel
import com.peihua8858.logfileserver.fileparser.Parameter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OtherParserTest {

    private lateinit var tempFile: File

    @BeforeEach
    fun setUp() {
        tempFile = createTempFile(suffix = ".txt").toFile()
        tempFile.writeBytes("test content".toByteArray())
    }

    @AfterEach
    fun tearDown() {
        if (tempFile.exists()) tempFile.delete()
    }

    @Nested
    @DisplayName("onParser 其他文件解析")
    inner class OnParserTests {

        @Test
        fun `应返回OtherFileModel类型`() {
            // Arrange
            val parser = OtherParser()
            val parameter = Parameter(
                file = tempFile, buildType = "", desc = "",
                isOnlyUploadFile = false, isOverwriteFile = false
            )

            // Act
            val (model, iconData) = parser.onParser(parameter, tempFile.parentFile)

            // Assert
            assertTrue(model is OtherFileModel)
            assertEquals(null, iconData)
        }

        @Test
        fun `platform应设为Other`() {
            // Arrange
            val parser = OtherParser()
            val parameter = Parameter(
                file = tempFile, buildType = "", desc = "",
                isOnlyUploadFile = false, isOverwriteFile = false
            )

            // Act
            val (model, _) = parser.onParser(parameter, tempFile.parentFile)

            // Assert
            assertEquals("Other", model.platform)
        }

        @Test
        fun `fileName和filePath应被正确设置`() {
            // Arrange
            val parser = OtherParser()
            val parameter = Parameter(
                file = tempFile, buildType = "", desc = "",
                isOnlyUploadFile = false, isOverwriteFile = false
            )

            // Act
            val (model, _) = parser.onParser(parameter, tempFile.parentFile)

            // Assert
            assertEquals(tempFile.name, model.fileName)
            assertEquals(tempFile.absolutePath, model.filePath)
        }
    }

    @Nested
    @DisplayName("createPlatformFile 平台目录创建")
    inner class CreatePlatformFileTests {

        private lateinit var parentDir: File

        @BeforeEach
        fun setUp() {
            parentDir = createTempFile().toFile().parentFile
        }

        @ParameterizedTest(name = "扩展名: {0} → 子目录: {1}")
        @CsvSource(
            "txt, txt",
            "pdf, pdf",
            "html, html",
            "htm, html",
            "docx, docx",
            "doc, docx",
            "docm, docx",
            "xlsx, xlsx",
            "xls, xlsx",
            "xlsm, xlsx",
            "zip, zip",
            "7z, zip",
            "rar, zip",
        )
        fun `应对已知扩展名返回正确子目录`(extension: String, expectedDir: String) {
            // Arrange
            val parser = OtherParser()

            // Act
            val result = parser.createPlatformFile("application/octet-stream", extension, tempFile, parentDir)

            // Assert
            assertNotNull(result)
            assertEquals(expectedDir, result.name)
        }

        @Test
        fun `对image contentType应返回images子目录`() {
            // Arrange
            val parser = OtherParser()

            // Act
            val result = parser.createPlatformFile("image/png", "unknown_ext", tempFile, parentDir)

            // Assert
            assertNotNull(result)
            assertEquals("images", result.name)
        }

        @Test
        fun `对不识别的扩展名应使用扩展名本身作为子目录名`() {
            // Arrange
            val parser = OtherParser()

            // Act
            val result = parser.createPlatformFile("application/octet-stream", "custom", tempFile, parentDir)

            // Assert
            assertNotNull(result)
            assertEquals("custom", result.name)
        }

        @Test
        fun `对空扩展名应返回other子目录`() {
            // Arrange
            val parser = OtherParser()

            // Act
            val result = parser.createPlatformFile("application/octet-stream", "", tempFile, parentDir)

            // Assert
            assertNotNull(result)
            assertEquals("other", result.name)
        }

        @Test
        fun `对apk扩展名应抛出异常`() {
            // Arrange
            val parser = OtherParser()

            // Act & Assert
            assertFailsWith<IllegalArgumentException> {
                parser.createPlatformFile("application/octet-stream", "apk", tempFile, parentDir)
            }
        }

        @Test
        fun `对ipa扩展名应抛出异常`() {
            // Arrange
            val parser = OtherParser()

            // Act & Assert
            assertFailsWith<IllegalArgumentException> {
                parser.createPlatformFile("application/octet-stream", "ipa", tempFile, parentDir)
            }
        }
    }
}
