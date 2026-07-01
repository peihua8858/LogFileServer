package com.peihua8858.logfileserver.configs

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class MyMetaObjectHandler : MetaObjectHandler {
    override fun insertFill(metaObject: MetaObject?) {
        logger.info("新增的时候干点不可描述的事情")
    }

    override fun updateFill(metaObject: MetaObject?) {
        logger.info("更新的时候干点不可描述的事情")
    }

    companion object {
        protected val logger: Logger = LoggerFactory.getLogger(MyMetaObjectHandler::class.java)
    }
}
