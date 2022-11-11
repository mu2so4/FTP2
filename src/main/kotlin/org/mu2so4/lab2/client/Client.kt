package org.mu2so4.lab2.client

import java.io.Closeable
import java.io.FileInputStream
import java.net.Socket

class Client(destAddress: String, port: Int): Closeable {
    private val socket = Socket(destAddress, port)

    fun sendFile(fileStream: FileInputStream) {
        fileStream.transferTo(socket.getOutputStream())
    }

    override fun close() {
        socket.close()
    }
}