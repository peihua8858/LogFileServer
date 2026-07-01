package com.peihua8858.logfileserver.entity.filemeta

import com.peihua8858.logfileserver.entity.FileModel

class ImageFileModel : FileModel {
    override var platform: String? = null
    override var fileName: String? = null
    override var filePath: String? = null
    override var fileSize: Long? = null
    override var iconPath: String? = null
    override var buildDescription: String? = null
    override var bundleId: String? = null
    override var versionName: String? = null
    override var versionCode: Int? = null
    override var name: String? = null
    override var buildType: String? = null
    override var downloadUrl: String? = null
}
