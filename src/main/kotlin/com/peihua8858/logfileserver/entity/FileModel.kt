package com.peihua8858.logfileserver.entity

interface FileModel {
    var platform: String?
    var fileName: String?
    var filePath: String?
    var fileSize: Long?
    var iconPath: String?
    var buildDescription: String?
    var bundleId: String?
    var versionName: String?
    var versionCode: Int?
    var name: String?
    var buildType: String?
    var downloadUrl: String?
}
