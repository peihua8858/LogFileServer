package com.peihua8858.logfileserver.controller

import com.baomidou.mybatisplus.core.metadata.IPage
import com.peihua8858.logfileserver.entity.Response
import com.peihua8858.logfileserver.entity.ResponsePage
import com.peihua8858.logfileserver.entity.appinfo.AppInfo
import com.peihua8858.logfileserver.entity.appinfo.AppPageRequest
import com.peihua8858.logfileserver.services.appinfo.AppInfosService
import com.peihua8858.logfileserver.utils.QrcodeUtil
import com.peihua8858.logfileserver.utils.generateFreemarker
import com.peihua8858.logfileserver.utils.serverUrl
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * App 相关信息查询处理
 */
@Controller
@RequestMapping("/app")
class AppController @Autowired constructor(
    private val appInfosService: AppInfosService,
) {
    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(AppController::class.java)
    }

    /**
     * 安装包首页列表
     *
     * @param request
     * @param model
     * @return
     * @ignoreParams request
     * @ignoreParams model
     */
    @GetMapping
    fun homePage(
        request: HttpServletRequest,
        model: Model,
        @RequestParam(value = "p", defaultValue = "", required = false) platform: String,
        @RequestParam(value = "v", defaultValue = "0", required = false) isVersionMax: Int
    ): String {
        request.serverName
        val result = if (isVersionMax == 1) {
            appInfosService.queryLastTimeUpdateByPlatform(platform)
        } else {
            appInfosService.queryLastTimeAndMaxVersionByPlatform(platform)
        }
        model.addAttribute("result", result)
        model.addAttribute("p", platform)
        return "/pc/app/app_home"
    }

    /**
     * 安装引导页
     *
     * @param model 页面模型
     * @return
     * @ignoreParams device
     * @ignoreParams model
     */
    @GetMapping("/guide")
    fun installGuide(model: Model): String {
        return "/pc/app/install_guide"
    }

    /**
     * 安装包按包名、编译类型筛选列表
     *
     * @param model    页面模型
     * @param bundleId 项目包名
     * @param type     编译类型
     * @return
     * @ignoreParams servletRequest
     * @ignoreParams device
     * @ignoreParams model
     */
    @GetMapping("/{bundleId}")
    fun appDetail(
        request: HttpServletRequest, model: Model,
        @PathVariable bundleId: String?,
        @RequestParam(value = "type", defaultValue = "") type: String?
    ): String {
        if (bundleId.isNullOrBlank()) {
            throw NullPointerException("Request parameter bundleId is invalid")
        }
        LOG.info("result>>>bundleId:$bundleId")
        LOG.info("result>>>type:$type")
        var result: AppInfo? = null
        val results: MutableList<AppInfo> = this.appInfosService.queryLastVersionByBundleId(bundleId)
        if (results.size > 1) {
            val r = results[0]
            return findListByBundleId(request, model, r.platform, r.bundleId)
        } else if (results.isNotEmpty()) {
            result = results[0]
        }
        return detailView(request, bundleId, model, type, result)
    }

    /**
     * 安装包详情页
     * 
     * @param model  页面模型
     * @param id     安装包主键ID
     * @param type   编译类型
     * @return
     * @ignoreParams servletRequest
     * @ignoreParams device
     * @ignoreParams model
     */
    @GetMapping("/detail/{id}")
    fun appDetailView(
        request: HttpServletRequest, model: Model,
        @PathVariable id: String?, @RequestParam(value = "type", defaultValue = "") type: String?
    ): String {
        if (id.isNullOrEmpty()) {
            throw java.lang.NullPointerException("Request parameter id is invalid")
        }
        LOG.info("result>>>id:$id")
        val longId = id.toLong()
        var result: AppInfo? = null
        if (longId != 0L) {
            result = this.appInfosService.getById(longId)
        } else {
            val results: MutableList<AppInfo> = this.appInfosService.queryLastVersionByBundleId(id)
            if (results.size > 1) {
                val r = results[0]
                return findListByBundleId(request, model, r.platform, r.bundleId)
            } else if (results.isNotEmpty()) {
                result = results[0]
            }
        }
        return detailView(request, id, model, type, result)
    }

    /**
     * 获取指定项目下是所有安装包
     *
     * @param model
     * @return
     * @ignoreParams device
     * @ignoreParams model
     */
    @GetMapping("versions")
    fun findListByBundleId(
        request: HttpServletRequest, model: Model,
        @RequestParam(value = "p", defaultValue = "", required = false) platform: String?,
        @RequestParam(value = "b") bundleId: String?
    ): String {
        val requestParam = AppPageRequest()
        requestParam.bundleId = bundleId
        requestParam.platform = platform
        val pageData: IPage<AppInfo> = appInfosService.findByBundleAndPlatform(requestParam)
        val records = pageData.getRecords()
        model.addAttribute("appinfos", records)
        model.addAttribute("b", bundleId)
        val serverUrl = request.serverUrl
        if (records != null && !records.isEmpty()) {
            val data = records[0]
            val icon: String? = data.iconPath
            val p: String? = data.platform
            model.addAttribute("p", p)
            model.addAttribute("icon", "$serverUrl/$icon")
        }
        model.addAttribute("sortLink", "$serverUrl/$bundleId")
        model.addAttribute("totalPage", pageData.getPages())
        model.addAttribute("currentPage", pageData.current)
        return "/pc/app/app_list"
    }

    /**
     * 分页加载当前平台、当前包名对应的安装包数据
     *
     * @param platform 平台名称
     * @param bundleId APP包名
     * @param curPage  当前页码
     * @param pageSize 每页显示条数
     * @author dingpeihua
     * @date 2022/1/17 20:08
     * @version 1.0
     */
    @RequestMapping("moreVersions")
    @ResponseBody
    fun findListByBundleId(
        request: HttpServletRequest,
        @RequestParam(value = "p", defaultValue = "", required = false) platform: String?,
        @RequestParam(value = "b") bundleId: String?,
        @RequestParam(value = "s", defaultValue = "", required = false) keyword: String?,
        @RequestParam(value = "curPage", defaultValue = "1") curPage: Int,
        @RequestParam(value = "pageSize", defaultValue = "20") pageSize: Int
    ): Response<ResponsePage<AppInfo>> {
        val requestParam = AppPageRequest()
        requestParam.current = curPage.toLong()
        requestParam.pageSize = pageSize.toLong()
        requestParam.bundleId = bundleId
        requestParam.search = keyword
        requestParam.platform = platform
        val pageData: IPage<AppInfo> = appInfosService.findByBundleAndPlatform(requestParam)
        return Response.ok(pageData)
    }

    fun detailView(request: HttpServletRequest, id: String?, model: Model, type: String?, result: AppInfo?): String {
        if (result != null) {
            LOG.info("result>>>$result")
            val serverUrl = request.serverUrl
            model.addAttribute("result", result)
            val url = "$serverUrl/app/detail/" + result.id
            val plistUrl: String = getPlistUrl(request, result.id!!)
            val base64: String? = QrcodeUtil.getBase64QRCode(url, 400, 400, 1)
            model.addAttribute("headerTitle", "App 详情")
            model.addAttribute("qCodeImage", "data:image/jpg;base64,$base64")
            model.addAttribute("isSuccess", true)
            model.addAttribute("plistUrl", plistUrl)
            model.addAttribute("type", type)
            model.addAttribute("sslKey", "$serverUrl/files/keystore/ca.crt")

            return "/pc/app/detail"
        }
        throw NullPointerException("Not found app info by $id")
    }

    fun getPlistUrl(request: HttpServletRequest, id: Long): String {
        val serverUrl = request.serverUrl
        return if ("https".equals(request.scheme, ignoreCase = true)) {
            "$serverUrl/app/downloadPlistFile/$id"
        } else {
            "https://${request.serverName}:8043/app/downloadPlistFile/$id"
        }
    }
    /**
     * iOS安装APP之前下载plist文件
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param id       安装包主键ID
     * @ignoreParams request
     * @ignoreParams response
     * @Description 根据传入的ID生成plist文件返回给iOS系统
     */
    @GetMapping("/downloadPlistFile/{id}")
    fun downloadPlistFile(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @PathVariable("id") id: String
    ) {
        LOG.info("id:$id")
        val result: AppInfo? = appInfosService.getById(id.toLong())
        LOG.info("result:$result")
        if (result == null) {
            response.status = 400
        } else {
            val serverUrl = request.serverUrl
            val data: MutableMap<String, Any?> = HashMap()
            data["downloadUrl"] = "$serverUrl/app/realDownload/" + result.id
            data["identifier"] = result.bundleId
            data["version"] = result.versionName
            data["subtitle"] = result.name
            data["title"] = result.name
            try {
                val content: String = data.generateFreemarker("plist.ftl")
                response.setHeader("Content-Type", "application/octet-stream; charset=utf-8")
                response.setHeader("Content-Disposition", "attachment;filename=manifest.plist")
                response.writer.write(content.toCharArray())
            } catch (e: Exception) {
                LOG.error("文件下载失败", e)
                response.status = 400
            }
        }
    }
}