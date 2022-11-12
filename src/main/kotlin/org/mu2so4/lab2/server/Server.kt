package org.mu2so4.lab2.server

import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

private const val UPLOAD_DIR_NAME = "uploads"

class Server(port: Int, backlog: Int = 50): AutoCloseable {
    private val socketServer = ServerSocket(port, backlog)
    private val clients = mutableListOf<Socket>()

    fun accept(): Boolean = clients.add(socketServer.accept())

    fun receiveFile(clientID: Int) {
        val inputStream = clients[clientID].getInputStream()

        val uploadsDirectory = File(UPLOAD_DIR_NAME)
        if(uploadsDirectory.isFile) {
            uploadsDirectory.delete()
        }
        if(!uploadsDirectory.exists()) {
            uploadsDirectory.mkdir()
        }

        val fileHeader = ByteArray(10)
        inputStream.read(fileHeader)
        val wrap = ByteBuffer.wrap(fileHeader)
        val fileNameSize = wrap.short
        val fileSize = wrap.long

        val byteFileName = ByteArray(fileNameSize.toInt())
        inputStream.read(byteFileName)
        val fullFile = File(byteFileName.toString(Charsets.UTF_8))
        val file = File("$UPLOAD_DIR_NAME/${fullFile.name}")
        file.createNewFile()

        val fileOutput = FileOutputStream(file)
        inputStream.transferTo(fileOutput)
        inputStream.close()
    }

    override fun close() {
        clients.forEach { e -> e.close() }
        socketServer.close()
    }
}