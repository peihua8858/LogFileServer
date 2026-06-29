package com.peihua8858.logfileserver.websocket

import jakarta.servlet.http.HttpSession
import jakarta.websocket.HandshakeResponse
import jakarta.websocket.server.HandshakeRequest
import jakarta.websocket.server.ServerEndpointConfig
import org.springframework.context.annotation.Configuration

@Configuration
class WebSocketConfig: ServerEndpointConfig.Configurator() {
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