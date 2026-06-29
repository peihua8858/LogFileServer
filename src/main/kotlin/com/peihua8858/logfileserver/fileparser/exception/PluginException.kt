package com.peihua8858.logfileserver.fileparser.exception

import java.lang.Exception

open class PluginException : Exception {
    constructor(message: String?) : super(message) {}
    constructor(cause: Throwable?) : super(cause) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}