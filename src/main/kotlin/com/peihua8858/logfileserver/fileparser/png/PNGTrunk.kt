package com.peihua8858.logfileserver.fileparser.png

import kotlin.Throws
import java.io.IOException
import java.io.FileOutputStream
import java.io.DataInputStream

open class PNGTrunk protected constructor(var size: Int, var name: String, var data: ByteArray, var cRC: ByteArray?) {
//    var data: ByteArray
//        protected set

//    protected constructor(nSize: Int, szName: String, nData: ByteArray, nCRC: ByteArray?) : this(nSize, szName, nCRC) {
//        data = nData
//    }

    @Throws(IOException::class)
    fun writeToStream(outStream: FileOutputStream) {
        val nSize = ByteArray(4)
        nSize[0] = (size and -0x1000000 shr 24).toByte()
        nSize[1] = (size and 0xFF0000 shr 16).toByte()
        nSize[2] = (size and 0xFF00 shr 8).toByte()
        nSize[3] = (size and 0xFF).toByte()
        outStream.write(nSize)
        outStream.write(name.toByteArray(charset("ASCII")))
        outStream.write(data, 0, size)
        outStream.write(cRC)
    }

    companion object {
        @Throws(IOException::class)
        fun generateTrunk(input: DataInputStream): PNGTrunk {
            val nSize = readPngInt(input)
            val nData = ByteArray(4)
            input.readFully(nData)
            val szName = String(nData, charset("ASCII"))
            val nDataBuffer = ByteArray(nSize)
            input.readFully(nDataBuffer)
            val nCRC = ByteArray(4)
            input.readFully(nCRC)
            return if (szName.equals("IHDR", ignoreCase = true)) {
                PNGIHDRTrunk(nSize, szName, nDataBuffer, nCRC)
            } else PNGTrunk(nSize, szName, nDataBuffer, nCRC)
        }

        fun writeInt(nDes: ByteArray, nPos: Int, nVal: Int) {
            nDes[nPos] = (nVal and -0x1000000 shr 24).toByte()
            nDes[nPos + 1] = (nVal and 0xff0000 shr 16).toByte()
            nDes[nPos + 2] = (nVal and 0xff00 shr 8).toByte()
            nDes[nPos + 3] = (nVal and 0xff).toByte()
        }

        @Throws(IOException::class)
        fun readPngInt(input: DataInputStream): Int {
            val buffer = ByteArray(4)
            input.readFully(buffer)
            return readInt(buffer, 0)
        }

        @JvmStatic
        fun readInt(nDest: ByteArray, nPos: Int): Int { //读一个int
            var pos = nPos
            return (nDest[pos++].toInt() and 0xFF shl 24
                    or (nDest[pos++].toInt() and 0xFF shl 16)
                    or (nDest[pos++].toInt() and 0xFF shl 8)
                    or (nDest[pos].toInt() and 0xFF))
        }

        fun writeCRC(nData: ByteArray, nPos: Int) {
            val chunklen = readInt(nData, nPos)
            val sum = CRCChecksum(nData, nPos + 4, 4 + chunklen) xor -0x1
            writeInt(nData, nPos + 8 + chunklen, sum)
        }

        var crc_table: IntArray? = null
        fun CRCChecksum(nBuffer: ByteArray, nOffset: Int, nLength: Int): Int {
            var c = -0x1
            var n: Int
            if (crc_table == null) {
                var mkc: Int
                var mkn: Int
                var mkk: Int
                crc_table = IntArray(256)
                mkn = 0
                while (mkn < 256) {
                    mkc = mkn
                    mkk = 0
                    while (mkk < 8) {
                        mkc = if (mkc and 1 == 1) {
                            -0x12477ce0 xor (mkc ushr 1)
                        } else {
                            mkc ushr 1
                        }
                        mkk++
                    }
                    crc_table!![mkn] = mkc
                    mkn++
                }
            }
            n = nOffset
            while (n < nLength + nOffset) {
                c = crc_table!![c xor nBuffer[n].toInt() and 0xff] xor (c ushr 8)
                n++
            }
            return c
        }
    }
}