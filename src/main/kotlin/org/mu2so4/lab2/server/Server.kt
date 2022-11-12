package org.mu2so4.lab2.server

import java.net.ServerSocket
import java.util.concurrent.Executors

private const val SO_TIMEOUT = 1000

class Server(port: Int, backlog: Int = 50): AutoCloseable {
    private val socketServer = ServerSocket(port, backlog)
    private val workerPool = Executors.newFixedThreadPool(20)
    private var lastId = 0

    fun accept() {
        val clientSocket = socketServer.accept()
        clientSocket.soTimeout = SO_TIMEOUT
        workerPool.submit(SocketWorker(clientSocket, lastId))
        lastId++
    }

    override fun close() {
        socketServer.close()
        workerPool.shutdown()
    }
}
