package com.peihua8858.logfileserver.entity

import com.baomidou.mybatisplus.core.metadata.IPage

/**
 * 分页数据响应数据
 * 
 * @param <T> 数据列表集合
</T> */
data class ResponsePage<T>(
    /**
     * 查询数据列表
     */
    var result: MutableList<T> = mutableListOf(),
    /**
     * 数据总数
     */
    private var totalNum: Long = 0,

    /**
     * 每页显示条数，默认 10
     */
    private var pageSize: Long = 10,

    /**
     * 当前页
     */
    private var curPage: Long = 1,

    /**
     * 扩展参数
     */
    private val values: Any? = null,

    /**
     * 总页数
     */
    private var totalPage: Long = 0,
) {
    constructor(page: IPage<T>) : this(
        page.getRecords(),
        page.total,
        page.size,
        page.current,
        totalPage = page.pages
    )
}
