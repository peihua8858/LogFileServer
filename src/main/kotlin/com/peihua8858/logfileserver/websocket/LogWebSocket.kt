package com.peihua8858.logfileserver.websocket

import com.peihua8858.logfileserver.entity.Response
import com.peihua8858.logfileserver.utils.toJSONString
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.util.concurrent.CopyOnWriteArraySet


@ServerEndpoint(value = "/websocket", configurator = WebSocketConfig::class)
@Component
class LogWebSocket {
    private var session: Session? = null

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        this.session = session;
        webSocketSet.add(this);
        addOnlineCount();
        try {
            val uri: URI = session.requestURI
            val args = uri.toString().split("platform_name=")
            var platform: String? = null
            if (args.size == 2) {
                platform = args[1]
            }
            if ((platform != null) && (platform != "null")) {
                webSocketsSession[this] = platform
            }
            sendMessage(Response.msg(SYSTEM_MSG_CODE, "有新连接加入！当前在线人数为" + getOnlineCount()))
        } catch (e: IOException) {
            println(e.stackTraceToString())
        }
    }

    @OnClose
    fun onClose() {
        webSocketSet.remove(this)
        webSocketsSession.remove(this)
        subOnlineCount()
        println("有一连接关闭！当前在线人数为" + getOnlineCount())
    }

    @OnMessage
    fun onMessage(message: String?, session: Session?) {
        println("来自客户端的消息:$message")
        synchronized(webSocketSet) {
            for (item in webSocketSet) {
                try {
                    item.sendMessage(message)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun sendInfo(message: String, platForm: String?) {
        synchronized(webSocketSet) {
            for (item in webSocketSet) {
                try {
                    if (platForm != null) {
                        val plat = webSocketsSession.get(item)
                        if (plat != null) {
                            if (plat == platForm) {
                                sendUserMessage(item, Response.msg(message))
                            }
                        } else {
                            sendUserMessage(item, Response.msg(message))
                        }
                    } else {
                        sendUserMessage(item, Response.msg(message))
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }

    @Throws(IOException::class)
    fun sendUserMessage(socket: LogWebSocket, response: Response<*>) {
        sendMessage(socket, response.copy(code = USER_MSG_CODE))
    }

    @Throws(IOException::class)
    fun sendMessage(socket: LogWebSocket, response: Response<*>?) {
        sendMessage(socket, response?.toJSONString())
    }

    @Throws(IOException::class)
    fun sendMessage(socket: LogWebSocket, message: String?) {
        socket.sendMessage(message)
    }

    @Throws(IOException::class)
    fun sendInfo(message: String) {
        synchronized(webSocketSet) {
            for (item in webSocketSet) {
                try {
                    sendUserMessage(item, Response.msg(message))
                } catch (ignored: IOException) {
                }
            }
        }
    }

    @Throws(IOException::class)
    fun sendMessage(response: Response<*>?) {
        sendMessage(response?.toJSONString())
    }

    @Throws(IOException::class)
    fun sendMessage(message: String?) {
        this.session?.basicRemote?.sendText(message)
    }

    @Synchronized
    fun getOnlineCount(): Int {
        return onlineCount
    }

    @Synchronized
    fun addOnlineCount() {
        onlineCount += 1
    }

    @Synchronized
    fun subOnlineCount() {
        onlineCount -= 1
    }

    companion object {
        /**
         * 系统消息
         *
         * @type {number}
         */

        const val SYSTEM_MSG_CODE: Int = 0

        /**
         * 用户消息
         *
         * @type {number}
         */
        const val USER_MSG_CODE: Int = 1
        private var onlineCount: Int = 0
        private val webSocketSet: CopyOnWriteArraySet<LogWebSocket> = CopyOnWriteArraySet<LogWebSocket>()
        private val webSocketsSession: HashMap<LogWebSocket, String> = HashMap()
    }
}