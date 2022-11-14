package org.mu2so4.lab2.client

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.io.path.fileSize

private const val FILENAME_SIZE_MAX = 4096
private const val FILE_SIZE_MAX = 1024L * 1024 * 1024 * 1024

class Client(private val destAddress: String, private val port: Int) {

    fun sendFile(name: String) {
        val file = File(name)
        val filename = file.name.toByteArray(Charsets.UTF_8)
        val filenameSize = filename.size

        if(filenameSize > FILENAME_SIZE_MAX) {
            throw IllegalArgumentException("filename is too long")
        }

        val fileSize = file.toPath().fileSize()
        if(fileSize > FILE_SIZE_MAX) {
            throw IllegalArgumentException("file is too large")
        }

        val checksumStream = FileInputStream(file)
        val digest = MessageDigest.getInstance("SHA-1")
        val checksum = digest.digest(checksumStream.readAllBytes())
        checksumStream.close()

        val fileStream = FileInputStream(file)
        val byteSizes = ByteBuffer.allocate(10).
            putShort(filenameSize.toShort()).putLong(fileSize).array()

        val socket = Socket(destAddress, port)
        val outputStream = socket.getOutputStream()
        outputStream.write(byteSizes)
        outputStream.write(checksum)
        outputStream.write(filename)
        try {
            fileStream.transferTo(outputStream)
        }
        catch(e: IOException) {
            println(e.message)
            println("Connection closed")
            fileStream.close()
            return
        }
        fileStream.close()

        val inputStream = socket.getInputStream()
        val byteResponseSize = ByteArray(4)
        inputStream.read(byteResponseSize)
        val responseSize = ByteBuffer.wrap(byteResponseSize).int
        val byteResponse = ByteArray(responseSize)
        inputStream.read(byteResponse)
        println(byteResponse.toString(Charsets.UTF_8))
    }
}
