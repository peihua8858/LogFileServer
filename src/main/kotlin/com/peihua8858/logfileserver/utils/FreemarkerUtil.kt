package com.peihua8858.logfileserver.utils

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.DefaultObjectWrapper
import freemarker.template.Template
import java.io.StringWriter

class FreemarkerUtil

fun Map<String, Any?>.generateFreemarker(filePath: String): String {
    try {
        val cfg = Configuration(Configuration.VERSION_2_3_30)
        cfg.templateLoader = ClassTemplateLoader(FreemarkerUtil::class.java, "/")
        cfg.objectWrapper = DefaultObjectWrapper(Configuration.VERSION_2_3_30)
        val temp: Template = cfg.getTemplate(filePath)
        val writer = StringWriter()
        temp.process(this, writer)
        return writer.toString()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}