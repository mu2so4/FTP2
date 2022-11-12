package org.mu2so4.lab2.server

import java.net.ServerSocket
import java.net.Socket

class Server(port: Int, backlog: Int = 50): AutoCloseable {
    private val socketServer = ServerSocket(port, backlog)
    private val clients = mutableListOf<Socket>()

    fun accept(): Boolean = clients.add(socketServer.accept())

    fun receiveFile(): ByteArray {
        val buf = ByteArray(100)
        clients[0].getInputStream().read(buf)
        return buf
    }

    override fun close() {
        clients.forEach { e -> e.close() }
        socketServer.close()
    }
}