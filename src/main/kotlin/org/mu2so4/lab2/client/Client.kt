package org.mu2so4.lab2.client

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.lang.IllegalArgumentException
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.io.path.fileSize

private const val FILENAME_SIZE_MAX = 4096
private const val FILE_SIZE_MAX = 1024L * 1024 * 1024 * 1024

class Client(destAddress: String, port: Int): Closeable {
    private val socket = Socket(destAddress, port)

    fun sendFile(name: String) {
        val file = File(name)
        val filename = file.name
        val filenameSize = filename.length

        if(filenameSize > FILENAME_SIZE_MAX) {
            throw IllegalArgumentException("filename is too long")
        }

        val fileSize = file.toPath().fileSize()
        if(fileSize > FILE_SIZE_MAX) {
            throw IllegalArgumentException("file is too large")
        }

        val fileStream = FileInputStream(file)
        val outputStream = socket.getOutputStream()

        val byteSizes = ByteBuffer.allocate(10).
            putShort(filenameSize.toShort()).putLong(fileSize).array()

        outputStream.write(byteSizes)
        outputStream.write(filename.toByteArray())
        fileStream.transferTo(socket.getOutputStream())
        fileStream.close()

        val inputStream = socket.getInputStream()
        val byteResponseSize = ByteArray(4)
        inputStream.read(byteResponseSize)
        val responseSize = ByteBuffer.wrap(byteResponseSize).int
        val byteResponse = ByteArray(responseSize)
        inputStream.read(byteResponse)
        println(byteResponse.toString(Charsets.UTF_8))
    }

    override fun close() {
        socket.close()
    }
}
