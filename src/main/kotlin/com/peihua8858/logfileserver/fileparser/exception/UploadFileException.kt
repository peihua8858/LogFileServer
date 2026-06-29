package com.peihua8858.logfileserver.fileparser.exception

/**
 * 上传文件异常
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/22 9:37
 */
class UploadFileException : PluginException {
    constructor(message: String?) : super(message) {}
    constructor(cause: Throwable?) : super(cause) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}