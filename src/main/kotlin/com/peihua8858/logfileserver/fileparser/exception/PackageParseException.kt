package com.peihua8858.logfileserver.fileparser.exception

class PackageParseException : PluginException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}