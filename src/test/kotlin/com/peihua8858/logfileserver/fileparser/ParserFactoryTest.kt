package com.peihua8858.logfileserver.fileparser

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ParserFactoryTest {

    @Nested
    @DisplayName("FileParserRouter.resolve 路由匹配")
    inner class RouterResolveTests {

        private val apkParser = mockk<FileParser<*>> {
            every { supports(any(), any()) } returns false
            every { supports("apk", any()) } returns true
            every { supports("APK", any()) } returns true
            every { supports("Apk", any()) } returns true
            every { order() } returns 1
        }

        private val ipaParser = mockk<FileParser<*>> {
            every { supports(any(), any()) } returns false
            every { supports("ipa", any()) } returns true
            every { supports("IPA", any()) } returns true
            every { supports("Ipa", any()) } returns true
            every { order() } returns 1
        }

        private val imagesParser = mockk<FileParser<*>> {
            every { supports(any(), any()) } returns false
            every { supports("png", "image/png") } returns true
            every { supports("jpg", "image/jpeg") } returns true
            every { supports("PNG", "IMAGE/PNG") } returns true
            every { supports("gif", "IMAGE/gif") } returns true
            every { order() } returns 2
        }

        private val otherParser = mockk<FileParser<*>> {
            every { supports(any(), any()) } returns true
            every { order() } returns Int.MAX_VALUE
        }

        private fun buildRouter(vararg parsers: FileParser<*>): FileParserRouter {
            return FileParserRouter(parsers.toList())
        }

        @ParameterizedTest(name = "扩展名: {0}, contentType: {1} → apkParser")
        @CsvSource(
            "apk, application/vnd.android.package-archive",
            "APK, application/vnd.android.package-archive",
            "Apk, application/octet-stream",
        )
        fun `应根据apk扩展名匹配到apkParser`(extension: String, contentType: String) {
            val router = buildRouter(apkParser, ipaParser, imagesParser, otherParser)
            val result = router.resolve(extension, contentType)
            assertEquals(apkParser, result)
        }

        @ParameterizedTest(name = "扩展名: {0}, contentType: {1} → ipaParser")
        @CsvSource(
            "ipa, application/octet-stream",
            "IPA, application/octet-stream",
            "Ipa, application/zip",
        )
        fun `应根据ipa扩展名匹配到ipaParser`(extension: String, contentType: String) {
            val router = buildRouter(apkParser, ipaParser, imagesParser, otherParser)
            val result = router.resolve(extension, contentType)
            assertEquals(ipaParser, result)
        }

        @ParameterizedTest(name = "扩展名: {0}, contentType: {1} → imagesParser")
        @CsvSource(
            "png, image/png",
            "jpg, image/jpeg",
            "PNG, IMAGE/PNG",
            "gif, IMAGE/gif",
        )
        fun `应根据image contentType匹配到imagesParser`(extension: String, contentType: String) {
            val router = buildRouter(apkParser, ipaParser, imagesParser, otherParser)
            val result = router.resolve(extension, contentType)
            assertEquals(imagesParser, result)
        }

        @ParameterizedTest(name = "扩展名: {0}, contentType: {1} → otherParser(兜底)")
        @CsvSource(
            "txt, text/plain",
            "pdf, application/pdf",
            "zip, application/zip",
            "docx, application/vnd.openxmlformats",
            "html, text/html",
            "log, text/plain",
        )
        fun `其他扩展名应匹配到兜底的otherParser`(extension: String, contentType: String) {
            val router = buildRouter(apkParser, ipaParser, imagesParser, otherParser)
            val result = router.resolve(extension, contentType)
            assertEquals(otherParser, result)
        }

        @Test
        fun `无匹配解析器时应抛出异常`() {
            // 只有一个不匹配任何类型的 parser
            val strictParser = mockk<FileParser<*>> {
                every { supports(any(), any()) } returns false
                every { order() } returns 1
            }
            val router = buildRouter(strictParser)
            assertFailsWith<IllegalArgumentException> {
                router.resolve("unknown", "application/octet-stream")
            }
        }

        @Test
        fun `应按order优先级排序_低order优先匹配`() {
            val lowPriority = mockk<FileParser<*>> {
                every { supports(any(), any()) } returns true
                every { order() } returns 100
            }
            val highPriority = mockk<FileParser<*>> {
                every { supports(any(), any()) } returns true
                every { order() } returns 1
            }
            // 即使 lowPriority 先加入列表，highPriority 应优先匹配
            val router = buildRouter(lowPriority, highPriority)
            val result = router.resolve("any", "any")
            assertEquals(highPriority, result)
        }
    }
}
