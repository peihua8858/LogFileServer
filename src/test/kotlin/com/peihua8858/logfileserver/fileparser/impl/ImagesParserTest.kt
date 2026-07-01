package com.peihua8858.logfileserver.fileparser.impl

import com.peihua8858.logfileserver.entity.filemeta.ImageFileModel
import com.peihua8858.logfileserver.fileparser.Parameter
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImagesParserTest {

    private lateinit var tempFile: File

    @BeforeEach
    fun setUp() {
        tempFile = createTempFile(suffix = ".png").toFile()
        tempFile.writeBytes(ByteArray(10)) // 写入一些字节
    }

    @AfterEach
    fun tearDown() {
        if (tempFile.exists()) tempFile.delete()
    }

    @Nested
    @DisplayName("onParser 图片文件解析")
    inner class OnParserTests {

        @Test
        fun `应返回ImageFileModel类型`() {
            // Arrange
            val parser = ImagesParser()
            val parameter = Parameter(
                file = tempFile, buildType = "", desc = "",
                isOnlyUploadFile = false, isOverwriteFile = false
            )

            // Act
            val (model, iconData) = parser.onParser(parameter, tempFile.parentFile)

            // Assert
            assertTrue(model is ImageFileModel)
            assertEquals(null, iconData)
        }

        @Test
        fun `platform应设为Images`() {
            // Arrange
            val parser = ImagesParser()
            val parameter = Parameter(
                file = tempFile, buildType = "", desc = "",
                isOnlyUploadFile = false, isOverwriteFile = false
            )

            // Act
            val (model, _) = parser.onParser(parameter, tempFile.parentFile)

            // Assert
            assertEquals("Images", model.platform)
        }

        @Test
        fun `fileName和filePath应被正确设置`() {
            // Arrange
            val parser = ImagesParser()
            val parameter = Parameter(
                file = tempFile, buildType = "", desc = "",
                isOnlyUploadFile = false, isOverwriteFile = false
            )

            // Act
            val (model, _) = parser.onParser(parameter, tempFile.parentFile)

            // Assert
            assertNotNull(model.fileName)
            assertEquals(tempFile.name, model.fileName)
            assertNotNull(model.filePath)
            assertEquals(tempFile.absolutePath, model.filePath)
        }
    }

    @Nested
    @DisplayName("createPlatformFile 平台目录创建")
    inner class CreatePlatformFileTests {

        @Test
        fun `对image contentType应返回images目录`() {
            // Arrange
            val parser = ImagesParser()
            val parentDir = createTempFile().toFile().parentFile

            // Mock ImageIO.read 避免真实文件读取
            mockkStatic(ImageIO::class)
            every { ImageIO.read(any<File>()) } returns null

            try {
                // Act
                val result = parser.createPlatformFile("image/png", "png", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("images", result.name)
            } finally {
                unmockkStatic(ImageIO::class)
            }
        }

        @Test
        fun `当ImageIO能读取文件时应返回images目录`() {
            // Arrange
            val parser = ImagesParser()
            val parentDir = createTempFile().toFile().parentFile
            val fakeImage = java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB)

            mockkStatic(ImageIO::class)
            every { ImageIO.read(any<File>()) } returns fakeImage

            try {
                // Act
                val result = parser.createPlatformFile("application/octet-stream", "dat", tempFile, parentDir)

                // Assert
                assertNotNull(result)
                assertEquals("images", result.name)
            } finally {
                unmockkStatic(ImageIO::class)
            }
        }
    }
}
