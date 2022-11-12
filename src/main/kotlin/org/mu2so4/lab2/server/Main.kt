package org.mu2so4.lab2.server

fun main() {
    val server = Server(12345, 10)

    server.accept()
    server.receiveFile(0)

    server.close()
}
