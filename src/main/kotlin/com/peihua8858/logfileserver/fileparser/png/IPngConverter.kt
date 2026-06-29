package com.peihua8858.logfileserver.fileparser.png

import com.jcraft.jzlib.Deflater
import com.jcraft.jzlib.GZIPException
import com.jcraft.jzlib.Inflater
import com.jcraft.jzlib.JZlib
import java.io.*
import java.util.zip.CRC32

class IPngConverter(source: File?, target: File?) {
    private val source: File
    private val target: File
    private var trunks: ArrayList<PNGTrunk>? = null
    @Throws(IOException::class)
    private fun getTargetFile(convertedFile: File): File {
        return if (source.isFile) {
            if (target.isDirectory) {
                File(target, source.name)
            } else {
                target
            }
        } else { // source is a directory
            if (target.isFile) { // single existing target
                target
            } else { // otherwise reconstruct a similar directory structure
                if (!target.isDirectory && !target.mkdirs()) {
                    throw IOException("failed to create folder " + target.absolutePath)
                }
                val relativeConvertedPath = source.toPath().relativize(convertedFile.toPath())
                val targetFile = File(target, relativeConvertedPath.toString())
                val targetFileDir = targetFile.parentFile
                if (targetFileDir != null && !targetFileDir.exists() && !targetFileDir.mkdirs()) {
                    throw IOException("unable to create folder " + targetFileDir.absolutePath)
                }
                targetFile
            }
        }
    }

    @Throws(IOException::class)
    fun convert() {
        convert(source)
    }

    private fun isPngFileName(file: File): Boolean {
        return file.name.toLowerCase().endsWith(".png")
    }

    private fun getTrunk(szName: String): PNGTrunk? {
        if (trunks == null) {
            return null
        }
        var trunk: PNGTrunk
        for (n in trunks!!.indices) {
            trunk = trunks!![n]
            if (trunk.name.equals(szName, ignoreCase = true)) {
                return trunk
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun convertPngFile(pngFile: File, targetFile: File) {
        readTrunks(pngFile)
        if (getTrunk("CgBI") != null) {
            // Convert data
            val ihdrTrunk = getTrunk("IHDR") as PNGIHDRTrunk?
            println("Width:" + ihdrTrunk!!.m_nWidth + "  Height:" + ihdrTrunk.m_nHeight)
            val nMaxInflateBuffer = 4 * (ihdrTrunk.m_nWidth + 1) * ihdrTrunk.m_nHeight
            val outputBuffer = ByteArray(nMaxInflateBuffer)
            convertDataTrunk(ihdrTrunk, outputBuffer, nMaxInflateBuffer)
            writePng(targetFile)
        } else {
            // Likely a standard PNG: just copy
            val buffer = ByteArray(1024)
            var bytesRead: Int
            val inputStream: InputStream = FileInputStream(pngFile)
            try {
                val outputStream: OutputStream = FileOutputStream(targetFile)
                try {
                    while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                } finally {
                    outputStream.close()
                }
            } finally {
                inputStream.close()
            }
        }
    }

    @Throws(GZIPException::class)
    private fun inflate(conversionBuffer: ByteArray, nMaxInflateBuffer: Int): Long {
        val inflater = Inflater(-15)
        for (dataTrunk in trunks!!) {
            if (!"IDAT".equals(dataTrunk.name, ignoreCase = true)) continue
            inflater.setInput(dataTrunk.data, true)
        }
        inflater.setOutput(conversionBuffer)
        val nResult: Int
        try {
            nResult = inflater.inflate(JZlib.Z_NO_FLUSH)
            checkResultStatus(nResult)
        } finally {
            inflater.inflateEnd()
        }
        if (inflater.totalOut > nMaxInflateBuffer) {
            println("PNGCONV_ERR_INFLATED_OVER")
        }
        return inflater.totalOut
    }

    @Throws(GZIPException::class)
    private fun deflate(buffer: ByteArray, length: Int, nMaxInflateBuffer: Int): Deflater {
        val deflater = Deflater()
        deflater.setInput(buffer, 0, length, false)
        val nMaxDeflateBuffer = nMaxInflateBuffer + 1024
        val deBuffer = ByteArray(nMaxDeflateBuffer)
        deflater.setOutput(deBuffer)
        deflater.deflateInit(JZlib.Z_BEST_COMPRESSION)
        val nResult = deflater.deflate(JZlib.Z_FINISH)
        checkResultStatus(nResult)
        if (deflater.totalOut > nMaxDeflateBuffer) {
            throw GZIPException("deflater output buffer was too small")
        }
        return deflater
    }

    @Throws(GZIPException::class)
    private fun checkResultStatus(nResult: Int) {
        when (nResult) {
            JZlib.Z_OK, JZlib.Z_STREAM_END -> {
            }
            JZlib.Z_NEED_DICT -> throw GZIPException("Z_NEED_DICT - $nResult")
            JZlib.Z_DATA_ERROR -> throw GZIPException("Z_DATA_ERROR - $nResult")
            JZlib.Z_MEM_ERROR -> throw GZIPException("Z_MEM_ERROR - $nResult")
            JZlib.Z_STREAM_ERROR -> throw GZIPException("Z_STREAM_ERROR - $nResult")
            JZlib.Z_BUF_ERROR -> throw GZIPException("Z_BUF_ERROR - $nResult")
            else -> throw GZIPException("inflater error: $nResult")
        }
    }

    @Throws(IOException::class)
    private fun convertDataTrunk(
        ihdrTrunk: PNGIHDRTrunk?, conversionBuffer: ByteArray, nMaxInflateBuffer: Int
    ): Boolean {
        println("converting colors")
        val inflatedSize = inflate(conversionBuffer, nMaxInflateBuffer)

        // Switch the color
        var nIndex = 0
        var nTemp: Byte
        for (y in 0 until ihdrTrunk!!.m_nHeight) {
            nIndex++
            for (x in 0 until ihdrTrunk.m_nWidth) {
                nTemp = conversionBuffer[nIndex]
                conversionBuffer[nIndex] = conversionBuffer[nIndex + 2]
                conversionBuffer[nIndex + 2] = nTemp
                nIndex += 4
            }
        }
        val deflater = deflate(conversionBuffer, inflatedSize.toInt(), nMaxInflateBuffer)

        // Put the result in the first IDAT chunk (the only one to be written out)
        val firstDataTrunk = getTrunk("IDAT") ?: return false
        val crc32 = CRC32()
        crc32.update(firstDataTrunk.name.toByteArray())
        crc32.update(deflater.nextOut, 0, deflater.totalOut.toInt())
        val lCRCValue = crc32.value
        firstDataTrunk.data = deflater.nextOut
        firstDataTrunk.cRC?.set(0, (lCRCValue and -0x1000000 shr 24).toByte())
        firstDataTrunk.cRC?.set(1, (lCRCValue and 0xFF0000 shr 16).toByte())
        firstDataTrunk.cRC?.set(2, (lCRCValue and 0xFF00 shr 8).toByte())
        firstDataTrunk.cRC?.set(3, (lCRCValue and 0xFF).toByte())
        firstDataTrunk.size = deflater.totalOut.toInt()
        return false
    }

    @Throws(IOException::class)
    private fun writePng(newFileName: File) {
        val outStream = FileOutputStream(newFileName)
        try {
            val pngHeader = byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)
            outStream.write(pngHeader)
            var dataWritten = false
            for (trunk in trunks!!) {
                // Skip Apple specific and misplaced CgBI chunk
                if (trunk.name.equals("CgBI", ignoreCase = true)) {
                    continue
                }

                // Only write the first IDAT chunk as they have all been put together now
                if ("IDAT".equals(trunk.name, ignoreCase = true)) {
                    dataWritten = if (dataWritten) {
                        continue
                    } else {
                        true
                    }
                }
                trunk.writeToStream(outStream)
            }
            outStream.flush()
        } finally {
            outStream.close()
        }
    }

    @Throws(IOException::class)
    private fun readTrunks(pngFile: File) {
        val input = DataInputStream(FileInputStream(pngFile))
        try {
            val nPNGHeader = ByteArray(8)
            input.readFully(nPNGHeader)
            var bWithCgBI = false
            trunks = ArrayList()
            if (nPNGHeader[0] == (-119).toByte() && nPNGHeader[1] == 0x50.toByte() && nPNGHeader[2] == 0x4e.toByte() && nPNGHeader[3] == 0x47.toByte()
                && nPNGHeader[4] == 0x0d.toByte() && nPNGHeader[5] == 0x0a.toByte() && nPNGHeader[6] == 0x1a.toByte() && nPNGHeader[7] == 0x0a.toByte()
            ) {
                var trunk: PNGTrunk
                do {
                    trunk = PNGTrunk.generateTrunk(input)
                    trunks!!.add(trunk)
                    if (trunk.name.equals("CgBI", ignoreCase = true)) {
                        bWithCgBI = true
                    }
                } while (!trunk.name.equals("IEND", ignoreCase = true))
            }
        } finally {
            input.close()
        }
    }

    @Throws(IOException::class)
    private fun convertDirectory(dir: File) {
        for (file in dir.listFiles()) {
            convert(file)
        }
    }

    @Throws(IOException::class)
    private fun convert(sourceFile: File) {
        if (sourceFile.isDirectory) {
            convertDirectory(sourceFile)
        } else if (isPngFileName(sourceFile)) {
            val targetFile = getTargetFile(sourceFile)
            println("converting " + sourceFile.path + " --> " + targetFile.path)
            convertPngFile(sourceFile, targetFile)
        }
    }

    init {
        if (source == null) throw NullPointerException("'source' cannot be null")
        if (target == null) throw NullPointerException("'target' cannot be null")
        this.source = source
        this.target = target
    }
}