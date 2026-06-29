package com.peihua8858.logfileserver.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

object QrcodeUtil {

    private val logger: Logger = LoggerFactory.getLogger(QrcodeUtil::class.java)

    /**
     * @param content 内容
     * @param path
     * @param width
     * @param height
     * @throws WriterException
     * @throws IOException
     */
    fun getImage(content: String?, path: String, width: Int, height: Int) {
        // 二维码的图片格式
        val format = "jpg"
        val hints: MutableMap<EncodeHintType?, Any?> = EnumMap(EncodeHintType::class.java)
        //设置二维码四周白色区域的大小
        hints[EncodeHintType.MARGIN] = 1
        //设置二维码的容错性
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        // 内容所使用字符集编码
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        var bitMatrix: BitMatrix? = null
        try {
            bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        } catch (e: WriterException) {
            logger.error(e.message)
        }
        // 生成二维码
        val pathfile = Paths.get(path)
        try {
            MatrixToImageWriter.writeToPath(bitMatrix, format, pathfile)
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    fun getBase64QRCode(content: String?, width: Int, height: Int): String? {
        return getBase64QRCode(content, width, height, 3)
    }

    /**
     * 生成二维码并使用Base64编码
     *
     * @param content
     * @param width
     * @param height
     * @return
     * @throws WriterException
     */
    fun getBase64QRCode(content: String?, width: Int, height: Int, margin: Int): String? {
        val multiFormatWriter = MultiFormatWriter()
        val hints: MutableMap<EncodeHintType?, Any?> = EnumMap(EncodeHintType::class.java)
        //设置二维码四周白色区域的大小
        hints[EncodeHintType.MARGIN] = margin //设置0-4之间
        //设置二维码的容错性
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        //设置编码格式
        hints[EncodeHintType.CHARACTER_SET] = "utf-8"
        //画二维码
        var bitMatrix: BitMatrix? = null
        try {
            bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        } catch (e: WriterException) {
            logger.error(e.message)
        }
        val image = MatrixToImageWriter.toBufferedImage(bitMatrix)
        //注意此处拿到字节数据
        val bytes: ByteArray = imageToBytes(image, "jpg")
        //Base64编码
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun imageToBytes(image: BufferedImage, type: String): ByteArray {
        val out = ByteArrayOutputStream()
        try {
            ImageIO.write(image, type, out)
        } catch (e: IOException) {
            logger.error(e.localizedMessage)
        }
        return out.toByteArray()
    }


    fun toBufferedImageCustom(matrix: BitMatrix): BufferedImage {
        //二维码边框的宽度，默认二维码的宽度是100，经过多次尝试后自定义的宽度
        val left = 3
        val right = 4
        val top = 2
        val bottom = 2

        //1、首先要自定义生成边框
        val rec = matrix.enclosingRectangle //获取二维码图案的属性
        val resWidth = rec[2] + left + right
        val resHeight = rec[3] + top + bottom

        val matrix2 = BitMatrix(resWidth, resHeight) // 按照自定义边框生成新的BitMatrix
        matrix2.clear()
        for (i in left + 1..<resWidth - right) {   //循环，将二维码图案绘制到新的bitMatrix中
            for (j in top + 1..<resHeight - bottom) {
                if (matrix.get(i - left + rec[0], j - top + rec[1])) {
                    matrix2.set(i, j)
                }
            }
        }

        val width = matrix2.width
        val height = matrix2.height

        //2、为边框设置颜色
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (x in 0..<width) {
            for (y in 0..<height) {
                if (x < left || x > width - right || y < top || y > height - bottom) {
                    image.setRGB(x, y, MatrixToImageConfig.BLACK) //为了与Excel边框重合，设置二维码边框的颜色为黑色
                } else {
                    image.setRGB(
                        x,
                        y,
                        if (matrix2.get(x, y)) MatrixToImageConfig.BLACK else MatrixToImageConfig.WHITE
                    )
                }
            }
        }
        return image
    }
}
