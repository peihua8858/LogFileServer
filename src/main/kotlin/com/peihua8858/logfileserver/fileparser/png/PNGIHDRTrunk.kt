package com.peihua8858.logfileserver.fileparser.png

class PNGIHDRTrunk(nSize: Int, szName: String?, nData: ByteArray?, nCRC: ByteArray?) :
    PNGTrunk(nSize, szName!!, nData!!, nCRC) {
    var m_nWidth: Int
    var m_nHeight: Int

    init {
        m_nWidth = readInt(nData!!, 0)
        m_nHeight = readInt(nData, 4)
    }
}