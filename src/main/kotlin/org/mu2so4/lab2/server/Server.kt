package org.mu2so4.lab2.server

import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket

class Server(port: Int, backlog: Int = 50): AutoCloseable {
    private val socketServer = ServerSocket(port, backlog)
    private val clients = mutableListOf<Socket>()

    fun accept(): Boolean = clients.add(socketServer.accept())

    fun receiveFile() {
        val fileOutput = FileOutputStream(File("out.txt"))
        val inputStream = clients[0].getInputStream()
        inputStream.transferTo(fileOutput)

    }

    override fun close() {
        clients.forEach { e -> e.close() }
        socketServer.close()
    }
}