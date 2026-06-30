package com.peihua8858.logfileserver.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeUtil {
}
val Any.formatCurTimeSecond: String
    get() = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
val Any.formatCurrentTimeMillis: String
    get() = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(Date())