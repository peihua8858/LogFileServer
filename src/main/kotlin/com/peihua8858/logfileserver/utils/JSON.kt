package com.peihua8858.logfileserver.utils

import tools.jackson.databind.ObjectMapper

fun Any.toJSONString(): String {
    return ObjectMapper().writerFor(javaClass).writeValueAsString(this)
}

inline fun <reified T> String.parseObject(): T {
    return ObjectMapper().readValue(this, T::class.java)
}

fun String.parseObject(): MutableMap<String, Any> {
    return parseObject<MutableMap<String, Any>>()
}
