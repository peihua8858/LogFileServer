package com.peihua8858.logfileserver.services.appinfo.impl

import com.peihua8858.logfileserver.configs.AppProperties
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.fileparser.FileParserRouter
import com.peihua8858.logfileserver.fileparser.ResultData
import com.peihua8858.logfileserver.services.filemeta.impl.SaveFileServiceImpl
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SaveAppFileServiceImplTest {

    private lateinit var service: SaveFileServiceImpl
    private val request: HttpServletRequest = mockk(relaxed = true)
    private val fileParserRouter: FileParserRouter = mockk(relaxed = true)
    private val appProperties: AppProperties = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        service = spyk(SaveFileServiceImpl(fileParserRouter, appProperties))
        every { request.scheme } returns "http"
        every { request.serverName } returns "localhost"
        every { request.serverPort } returns 8080

        // Mock ServiceImpl.save to avoid needing MyBatis baseMapper
        every { service.save(any<AppInfo>()) } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Nested
    @DisplayName("parserAndSaveFile 单文件处理")
    inner class SingleFileProcessing {

        @Test
        fun `当文件为空时应返回错误ResultData`() {
            // Arrange
            val emptyFile = MockMultipartFile(
                "file", "app.apk",
                "application/vnd.android.package-archive", ByteArray(0)
            )

            // Act
            val result = service.parserAndSaveFile(
                request, "", "release",
                isOverWrite = false, isOnlyUploadFile = false, file = emptyFile
            )

            // Assert
            assertTrue(result.error.isNotBlank(), "错误信息不应为空")
        }

        @Test
        fun `当contentType为null时应返回错误`() {
            // Arrange
            val file = MockMultipartFile(
                "file", "app.apk",
                null, "fake content".toByteArray()
            )

            // Act
            val result = service.parserAndSaveFile(
                request, "", "release",
                isOverWrite = false, isOnlyUploadFile = false, file = file
            )

            // Assert
            assertTrue(result.error.isNotBlank(), "错误信息不应为空")
            assertTrue(result.error.contains("Content-Type"), "错误信息应包含 Content-Type 提示")
        }

        @Test
        fun `当文件为空时ResultData应包含文件名和大小信息`() {
            // Arrange
            val emptyFile = MockMultipartFile(
                "file", "test.apk",
                "application/vnd.android.package-archive", ByteArray(0)
            )

            // Act
            val result = service.parserAndSaveFile(
                request, "", "release",
                isOverWrite = false, isOnlyUploadFile = false, file = emptyFile
            )

            // Assert
            assertEquals("file", result.name)
            assertEquals(0L, result.size)
        }
    }

    @Nested
    @DisplayName("parserAndSaveFiles 批量上传")
    inner class BatchUpload {

        @Test
        fun `应返回与文件数量相同的ResultData列表`() {
            // Arrange
            val files: Array<MultipartFile> = arrayOf(
                MockMultipartFile("files", "a.apk", "application/octet-stream", "fake".toByteArray()),
                MockMultipartFile("files", "b.ipa", "application/octet-stream", "fake".toByteArray()),
            )
            val mockResult = ResultData(name = "a.apk", url = "files/upload/android/a.apk")
            every {
                service.parserAndSaveFile(request, any(), any(), any(), any(), any())
            } returns mockResult

            // Act
            val result = service.parserAndSaveFiles(
                request, "desc", "release", false, false, files
            )

            // Assert
            assertNotNull(result["files"])
            assertEquals(2, result["files"]!!.size)
        }

        @Test
        fun `当单个文件处理异常时应捕获异常并返回错误信息`() {
            // Arrange
            val files: Array<MultipartFile> = arrayOf(
                MockMultipartFile("files", "a.apk", "application/octet-stream", "fake".toByteArray()),
            )
            every {
                service.parserAndSaveFile(request, any(), any(), any(), any(), any())
            } throws RuntimeException("模拟异常")

            // Act
            val result = service.parserAndSaveFiles(
                request, "desc", "release", false, false, files
            )

            // Assert
            assertNotNull(result["files"])
            assertEquals(1, result["files"]!!.size)
            assertTrue(result["files"]!![0].error.isNotBlank())
            assertEquals("模拟异常", result["files"]!![0].error)
        }

        @Test
        fun `空文件数组应返回空列表`() {
            // Arrange
            val files: Array<MultipartFile> = emptyArray()

            // Act
            val result = service.parserAndSaveFiles(
                request, "desc", "release", false, false, files
            )

            // Assert
            assertNotNull(result["files"])
            assertEquals(0, result["files"]!!.size)
        }

        @Test
        fun `多文件上传时每个文件都应被单独处理`() {
            // Arrange
            val files: Array<MultipartFile> = arrayOf(
                MockMultipartFile("files", "a.apk", "application/octet-stream", "fake1".toByteArray()),
                MockMultipartFile("files", "b.ipa", "application/octet-stream", "fake2".toByteArray()),
                MockMultipartFile("files", "c.png", "image/png", "fake3".toByteArray()),
            )
            val mockResult = ResultData(name = "mock", url = "mock/url")
            every {
                service.parserAndSaveFile(request, any(), any(), any(), any(), any())
            } returns mockResult

            // Act
            val result = service.parserAndSaveFiles(
                request, "desc", "release", false, false, files
            )

            // Assert
            assertEquals(3, result["files"]!!.size)
            verify(exactly = 3) {
                service.parserAndSaveFile(request, any(), any(), any(), any(), any())
            }
        }

        @Test
        fun `当部分文件失败时其他文件仍应继续处理`() {
            // Arrange
            val files: Array<MultipartFile> = arrayOf(
                MockMultipartFile("files", "a.apk", "application/octet-stream", "fake1".toByteArray()),
                MockMultipartFile("files", "b.ipa", "application/octet-stream", "fake2".toByteArray()),
            )
            val successResult = ResultData(name = "a.apk", url = "files/upload/android/a.apk")
            val callCount = intArrayOf(0)
            every {
                service.parserAndSaveFile(request, any(), any(), any(), any(), any())
            } answers {
                callCount[0]++
                if (callCount[0] == 1) throw RuntimeException("第一个文件失败")
                successResult
            }

            // Act
            val result = service.parserAndSaveFiles(
                request, "desc", "release", false, false, files
            )

            // Assert
            assertEquals(2, result["files"]!!.size)
            assertTrue(result["files"]!![0].error.isNotBlank(), "第一个文件应有错误")
            assertEquals("", result["files"]!![1].error, "第二个文件不应有错误")
        }
    }
}
