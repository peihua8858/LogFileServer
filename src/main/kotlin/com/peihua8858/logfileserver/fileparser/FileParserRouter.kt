package com.peihua8858.logfileserver.fileparser

import org.springframework.stereotype.Component

/**
 * 解析器路由器
 * 自动收集所有 FileParser Bean，按优先级排序，根据文件类型匹配
 */
@Component
class FileParserRouter(
    private val parsers: List<FileParser<*>> // Spring 自动注入所有实现
) {
    /**
     * 按 order() 排序后的解析器列表
     */
    private val sortedParsers: List<FileParser<*>> by lazy {
        parsers.sortedBy { it.order() }
    }

    /**
     * 根据文件类型查找匹配的解析器
     * @throws IllegalArgumentException 无匹配解析器时抛出
     */
    fun resolve(extensionName: String, contentType: String): FileParser<*> {
        return sortedParsers.firstOrNull { it.supports(extensionName, contentType) }
            ?: throw IllegalArgumentException("No parser found for extension=$extensionName, contentType=$contentType")
    }
}