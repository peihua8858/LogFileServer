package com.peihua8858.logfileserver.services.appinfo.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.entity.appinfo.AppPageRequest
import com.peihua8858.logfileserver.mappers.appinfo.AppInfoMapper
import com.peihua8858.logfileserver.services.appinfo.AppInfosService
import org.springframework.stereotype.Service

/**
 *
 * app 信息查询
 * @author dingpeihua
 * @date 2026/6/29 13:33
 **/
@Service
class AppInfosServiceImpl : ServiceImpl<AppInfoMapper, AppInfo>(), AppInfosService {
    /**
     * 查询当前平台[platform]下app 最新安装包，与版本号是否最大无关，只与{@link AppInfo#updateTime}相关
     *
     * @param platform 平台名称，Android 或者iOS
     * @author dingpeihua
     * @date 2020/1/22 11:30
     * @version 1.0
     */
    override fun queryLastTimeUpdateByPlatform(platform: String?): MutableList<AppInfo>? {
        return baseMapper.queryLastTimeUpdateByPlatform(platform)
    }

    override fun queryLastTimeAndMaxVersionByPlatform(platform: String?): MutableList<AppInfo>? {
        return baseMapper.queryLastTimeAndMaxVersionByPlatform(platform)
    }

    override fun queryLastVersionByBundleId(bundleId: String): MutableList<AppInfo> {
        return baseMapper.queryLastVersionByBundleId(bundleId, "", "") ?: mutableListOf()
    }

    override fun findByBundleAndPlatform(model: AppPageRequest): IPage<AppInfo> {
        val wrapper = QueryWrapper<AppInfo>()
        if (!model.platform.isNullOrEmpty()) {
            wrapper.eq("platform", model.platform)
        }
        wrapper.eq("bundle_id", model.bundleId)
        val keyword = model.search
        if (!keyword.isNullOrEmpty()) {
            wrapper.like("version_name", keyword)
                .or().like("version_code", keyword)
                .or().like("build_number", keyword)
                .or().like("build_type", keyword)
                .or().like("name", keyword)
        }
        wrapper.orderByDesc("update_time")
        val page: Page<AppInfo> = Page(model.current, model.pageSize)
        return baseMapper.selectPage(page, wrapper)
    }
}