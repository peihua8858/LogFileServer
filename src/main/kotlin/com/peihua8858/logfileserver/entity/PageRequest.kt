package com.peihua8858.logfileserver.entity

import org.hibernate.validator.constraints.Range
import java.util.*


/**
 * 分页请求参数
 */
open class PageRequest(
    /**
     * 搜索关键词
     */
    var search: String? = null,

    /**
     * 平台名称
     */
    var platform: String? = null,

    /**
     * 第几列排序，即字段名称
     */
    var orderByCol: Int = 0,

    /**
     * 排序方式，1为升序还是0为降序
     */
    var orderBy: String? = null,

    /**
     * 当前页码
     */
    @Range(min = 1)
    var current: Long = 1,

    /**
     * 每页数据条数
     */
    @Range(min = 5)
    var pageSize: Long = 20,

    /**
     * 开始时间
     */
    var startTime: Date? = null,

    /**
     * 结束时间
     */
    var endTime: Date? = null,
)
