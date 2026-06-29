package com.peihua8858.logfileserver.entity.appinfo

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model
import com.fasterxml.jackson.annotation.JsonFormat
import com.peihua8858.logfileserver.utils.isAndroidPlatform
import com.peihua8858.logfileserver.utils.isIOSPlatform
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer
import java.util.*

@TableName("app_info")
data class AppInfo(
    /**
     * 主键ID ，自增长
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer::class)
    var id: Long? = null,

    /**
     * 名称
     */
    @TableField("name")
    var name: String? = null,

    /**
     * 版本号
     */
    @TableField("version_code")
    var versionCode: Int? = null,

    /**
     * 版本名称
     */
    @TableField("version_name")
    var versionName: String? = null,

    /**
     * 文件大小
     */
    @TableField("file_size")
    var fileSize: Long? = null,

    /**
     * 文件路径
     */
    @TableField("file_path")
    var filePath: String? = null,

    /**
     * 文件名称
     */
    @TableField("file_name")
    var fileName: String? = null,

    /**
     * 项目图标
     */
    @TableField("icon_path")
    var iconPath: String? = null,

    /**
     * 下载次数
     */
    @TableField("download_count")
    var downloadCount: Int? = null,

    /**
     * 状态标识
     */
    @TableField("status")
    var status: Int? = null,

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var createTime: Date? = null,

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(
        pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8"
    )
    var updateTime: Date? = null,

    /**
     * 编译号码
     */
    @TableField("build_number")
    var buildNumber: Int? = null,

    /**
     * 打包时代码提交日志
     */
    @TableField("change_log")
    var changeLog: String? = null,

    /**
     * 平台名称，Android或iOS
     */
    @TableField("platform")
    var platform: String? = null,

    /**
     * 下载地址
     */
    @TableField("download_url")
    var downloadUrl: String? = null,

    /**
     * 持续时间
     */
    @TableField("duration")
    var duration: String? = null,

    /**
     * app包名
     */
    @TableField("bundle_id")
    var bundleId: String? = null,

    /**
     * 项目名称
     */
    @TableField("project_name")
    var projectName: String? = null,

    /**
     * 插件版本名称
     */
    @TableField("plugin_version")
    var pluginVersion: String? = null,

    /**
     * 编译类型，如：release、debug、develop等
     */
    @TableField("build_type")
    var buildType: String? = null,

    /**
     * IOS plist 文件地址
     */
    @TableField("plist_url")
    var plistUrl: String? = null,

    /**
     * APP安装包渠道名称
     */
    @TableField("flavor")
    var flavor: String? = null,

    /**
     * APP编译描述信息
     */
    @TableField("build_description")
    var buildDescription: String? = null,
) : Model<AppInfo>() {
    fun wasIOS(): Boolean {
        return isIOSPlatform
    }

    fun wasAndroid(): Boolean {
        return isAndroidPlatform
    }
}