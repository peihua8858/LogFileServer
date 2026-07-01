package com.peihua8858.logfileserver.mappers.appinfo

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AppInfoSqlProvider {
    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AppInfoSqlProvider::class.java)
    }

    /**
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
    fun queryByBundle(params: Map<String, Any>): String {
        val bundleId = params["bundleId"]?.toString().orEmpty()
        val buildType = params["buildType"]?.toString().orEmpty()
        val versionName = params["versionName"]?.toString().orEmpty()
        val sql = StringBuilder()
        sql.append("SELECT * FROM app_info ")
        sql.append("WHERE bundle_id = #{bundleId} ")

        if (buildType.isNotEmpty()) {
            sql.append("AND build_type = #{buildType} ")
        }

        if (versionName.isNotEmpty()) {
            sql.append("AND version_name = #{versionName} ")
        }
        sql.append("AND update_time = (")
        sql.append("  SELECT MAX(update_time) FROM app_info WHERE bundle_id = #{bundleId} ")
        if (buildType.isNotEmpty()) {
            sql.append("AND build_type = #{buildType} ")
        }
        if (versionName.isNotEmpty()) {
            sql.append("AND version_name = #{versionName} ")
        }
        sql.append(")")
        LOG.info("queryByBundle>>bundleId:$bundleId,buildType:$buildType,versionName:$versionName,$sql")
        return sql.toString()
    }
}