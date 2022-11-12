package org.mu2so4.lab2.server

import java.io.File
import java.io.FileOutputStream
import java.lang.Long.min
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

private const val UPLOAD_DIR_NAME = "uploads"
private const val OK_RESPONSE = "File uploaded successfully"
private const val ERR_RESPONSE = "Failed to upload file"
private const val SO_TIMEOUT = 1000
private const val BUF_SIZE = 1024

class Server(port: Int, backlog: Int = 50): AutoCloseable {
    private val socketServer = ServerSocket(port, backlog)
    private val clients = mutableListOf<Socket>()

    fun accept() {
        val clientSocket = socketServer.accept()
        clientSocket.soTimeout = SO_TIMEOUT
        clients.add(clientSocket)
    }

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

        val fileStream = FileOutputStream(file)
        var receivedByteCount = 0L
        val buffer = ByteArray(BUF_SIZE)
        while(receivedByteCount < fileSize) {
            try {
                val read = inputStream.read(
                    buffer, 0, min(
                        fileSize - receivedByteCount,
                        BUF_SIZE.toLong()
                    ).toInt()
                )
                fileStream.write(buffer, 0, read)
                receivedByteCount += read
            }
            catch(e: SocketTimeoutException) {
                break
            }
        }
        fileStream.close()

        val outputStream = clients[clientID].getOutputStream()
        val response = if(receivedByteCount == fileSize) {
            OK_RESPONSE
        } else {
            ERR_RESPONSE
        }

        val responseSize = ByteBuffer.allocate(4).
            putInt(response.length).array()
        outputStream.write(responseSize)
        outputStream.write(response.toByteArray())
    }

    override fun close() {
        clients.forEach { e -> e.close() }
        socketServer.close()
    }
}
