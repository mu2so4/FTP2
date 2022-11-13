package org.mu2so4.lab2.server

import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.logging.Logger

private const val SO_TIMEOUT = 1000
internal val logger = Logger.getLogger(Server::class.qualifiedName)

class Server(port: Int, backlog: Int = 50): AutoCloseable {
    private val socketServer = ServerSocket(port, backlog)
    private val workerPool = Executors.newFixedThreadPool(20)
    private var lastId = 0

    init {
        logger.info("Server started")
    }

    fun accept() {
        val clientSocket = socketServer.accept()
        clientSocket.soTimeout = SO_TIMEOUT
        logger.info("JOB ID $lastId started")
        workerPool.submit(SocketWorker(clientSocket, lastId))
        lastId++
    }

    override fun close() {
        socketServer.close()
        workerPool.shutdown()
        logger.info("Server closed")
    }
}
