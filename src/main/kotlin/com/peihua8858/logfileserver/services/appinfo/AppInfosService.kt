package com.peihua8858.logfileserver.services.appinfo

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.service.IService
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.entity.appinfo.AppPageRequest


/**
 * app信息存储服务
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2026/6/29 11:34
 */
interface AppInfosService : IService<AppInfo> {
    /**
     * 查询当前平台[platform]下app 最新安装包，与版本号是否最大无关，只与{@link AppInfo#updateTime}相关
     *
     * sql:
     * <!--  获取指定平台下的最新编译版本  -->
     *         SELECT *
     *         FROM app_info a where update_time
     *         IN (select MAX(update_time) from app_info where platform=#{platform} group by bundle_id)
     *         ORDER BY update_time DESC
     * @param platform 平台名称，Android 或者iOS
     * @author dingpeihua
     * @date 2020/1/22 11:30
     * @version 1.0
     */
    fun queryLastTimeUpdateByPlatform(platform: String?): MutableList<AppInfo>?

    /**
     *
     * 查询当前平台[platform]下app最新安装包，版本号[AppInfo.versionName]最大，[AppInfo.updateTime]最大
     *  <select id="appBundleGroup" resultMap="BaseResultMap">
     *         select
     *         <include refid="Base_Column_List"/>
     *         from app_info where update_time
     *         IN (select max(update_time) from app_info a where version_name
     *         in (select max(version_name) from app_info group by bundle_id) GROUP BY bundle_id)
     *         order by update_time desc
     *     </select>
     * @author dingpeihua
     * @param  platform 平台名称，Android 或者iOS
     * @date 2026/6/29 13:34
     **/
    fun queryLastTimeAndMaxVersionByPlatform(platform: String?): MutableList<AppInfo>?

    /**
     * 查询当前bundle ID 最新安装包
     * 注：参数值bundleId不能为空
     *
     * @param bundleId
     * @author dingpeihua
     * @date 2019/8/2 16:27
     * @version 1.0
     */
    fun queryLastVersionByBundleId(bundleId: String): MutableList<AppInfo>


    /**
     * 查询当前包名下所有APP版本
     *
     * @param model
     * @author dingpeihua
     * @date 2022/1/17 9:06
     * @version 1.0
     */
    fun findByBundleAndPlatform(model: AppPageRequest): IPage<AppInfo>
//    /**
//     * @param appInfoModel
//     * @author dingpeihua
//     * @date 2020/1/22 14:01
//     * @version 1.0
//     */
//    fun findById(appInfoModel: AppInfo?): AppInfo?
//
//    fun findAll(appInfoModel: AppInfo?): MutableList<AppInfo?>?
//
//    fun save(appInfoModel: AppInfo?): Boolean
//
//    fun delete(appInfoModel: AppInfo?): AppInfo?
//
//    fun update(appInfoModel: AppInfo?): AppInfo?
//
//    fun findAppInfos(appInfoModel: AppInfo?): IPage<AppInfo?>?
//
//    /**
//     * 按项目版本分组，获取所有版本最新包列表
//     * 注以下参数不能为空：
//     * [AppPageRequest.getBundleId]
//     * [AppPageRequest.getBuildType]
//     *
//     * @param model
//     * @author dingpeihua
//     * @date 2020/1/22 14:04
//     * @version 1.0
//     */
//    fun appVersionGroup(model: AppInfo?): MutableList<AppInfo?>?
//
//    /**
//     * 按编译类型分组，获取所有编译类型最新包列表，即查看当前APP所有历史构建安装包
//     * 以下参数不能为空：
//     * [AppPageRequest.getBundleId]
//     * [AppPageRequest.getBuildType]
//     * [AppPageRequest.getVersionName]
//     * wrapper.eq("bundle_id", request.getBundleId());
//     * wrapper.eq("build_type", request.getBuildType());
//     * wrapper.eq("version_name", request.getVersionName());
//     *
//     * @param model
//     * @author dingpeihua
//     * @date 2020/1/22 14:03
//     * @version 1.0
//     */
//    fun appBuildList(model: AppPageRequest?): IPage<AppInfo?>?
//
//    /**
//     * apk按构建类型查询
//     * 注：参数值bundleId不能为空
//     *
//     * @param model
//     * @author dingpeihua
//     * @date 2019/8/2 16:27
//     * @version 1.0
//     */
//    fun appBuildTypeList(model: AppInfo?): MutableList<AppInfo?>?
//


}