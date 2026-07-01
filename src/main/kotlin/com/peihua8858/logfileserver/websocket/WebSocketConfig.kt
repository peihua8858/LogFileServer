package com.peihua8858.logfileserver.websocket

import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpSession
import jakarta.websocket.HandshakeResponse
import jakarta.websocket.server.HandshakeRequest
import jakarta.websocket.server.ServerEndpointConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServerEndpointExporter

@Configuration
class WebSocketConfig : ServerEndpointConfig.Configurator() {

    @Bean
    fun serverEndpointExporter(servletContext: ServletContext): ServerEndpointExporter {
        return object : ServerEndpointExporter() {
            override fun afterPropertiesSet() {
                // Skip ServerContainer check in test environments
            }

            override fun afterSingletonsInstantiated() {
                if (servletContext.getAttribute("jakarta.websocket.server.ServerContainer") != null) {
                    super.afterSingletonsInstantiated()
                }
            }
        }
    }

    override fun modifyHandshake(sec: ServerEndpointConfig?, request: HandshakeRequest?, response: HandshakeResponse?) {
        super.modifyHandshake(sec, request, response)

        /*如果没有监听器,那么这里获取到的HttpSession是null*/
        val ssf = request?.httpSession
        if (ssf != null) {
            val httpSession = request.httpSession as HttpSession
            //关键操作
            sec?.userProperties?.put("sessionId", httpSession.id)
            println("获取到的SessionID：" + httpSession.id)
        }
    }

}
