package com.peihua8858.logfileserver.entity.appinfo

import com.peihua8858.logfileserver.entity.PageRequest

/**
 * App 安装包接口请求参数
 */
class AppPageRequest : PageRequest() {
    /**
     * App包名
     */
    var bundleId: String? = null

    /**
     * App编译类型
     */
    var buildType: String? = null

    /**
     * App版本号
     */
    var versionName: String? = null

    public override fun toString(): String {
        return "AppPageRequest{" +
                "bundleId='" + bundleId + '\'' +
                ", buildType='" + buildType + '\'' +
                ", versionName='" + versionName + '\'' +
                '}'
    }
}
