package com.peihua8858.logfileserver.mappers.appinfo

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.SelectProvider

interface AppInfoMapper : BaseMapper<AppInfo> {
    /**
     * 查询当前平台[platform]下app 最新安装包，与版本号是否最大无关，只与{@link AppInfo#updateTime}相关
     *
     * @param platform 平台名称，Android 或者iOS
     * @author dingpeihua
     * @date 2020/1/22 11:30
     * @version 1.0
     */
    @Select(
        """
        SELECT a.*
        FROM app_info a
        JOIN (
            SELECT bundle_id, MAX(update_time) AS max_update_time
            FROM app_info
            WHERE platform = #{platform}
            GROUP BY bundle_id
        ) t
          ON a.bundle_id = t.bundle_id
         AND a.update_time = t.max_update_time
        WHERE a.platform = #{platform}
        ORDER BY a.update_time DESC
        """
    )
    fun queryLastTimeUpdateByPlatform(@Param("platform") platform: String?): MutableList<AppInfo>?

    /**
     *
     * 查询当前平台[platform]下app最新安装包，版本号[AppInfo.versionName]最大，[AppInfo.updateTime]最大
     *  select
     *         <include refid="Base_Column_List"/>
     *         from app_info where update_time
     *         IN (select max(update_time) from app_info a where version_name
     *         in (select max(version_name) from app_info group by bundle_id) GROUP BY bundle_id)
     *         order by update_time desc
     * @author dingpeihua
     * @param
     * @return
     * @date 2026/6/29 14:42
     **/
    @Select(
        """
        SELECT a.*
        FROM app_info a
        JOIN (
        SELECT bundle_id,max(update_time)AS max_update_time from app_info a where version_name
             IN (SELECT bundle_id,max(version_name) from app_info  WHERE platform = #{platform} group by bundle_id) GROUP BY bundle_id
        ) t
          ON a.bundle_id = t.bundle_id
         AND a.update_time = t.max_update_time
        WHERE a.platform = #{platform}
        ORDER BY a.update_time DESC
        """
    )
    fun queryLastTimeAndMaxVersionByPlatform(@Param("platform") platform: String?): MutableList<AppInfo>?

    /**
     * <!--  app筛选指定包名下最新编译版本-->
     *     <select id="findByBundle" resultMap="BaseResultMap" parameterMap="AppVersionParamMap">
     *         SELECT
     *         <include refid="Base_Column_List"/>
     *         from app_info WHERE update_time =(SELECT max(update_time)as update_time from app_info where
     *         bundle_id=#{bundleId}
     *         <if test="buildType!=null and buildType!=''">
     *             and build_type=#{buildType}
     *         </if>
     *         <if test="versionName!=null and versionName!=''">
     *             and version_name=#{versionName}
     *         </if>
     *         )
     *     </select>
     */
    @SelectProvider(type = AppInfoSqlProvider::class, method = "queryByBundle")
    fun queryLastVersionByBundleId(
        @Param("bundleId") bundleId: String,
        @Param("buildType") buildType: String,
        @Param("versionName") versionName: String
    ): MutableList<AppInfo>?
}