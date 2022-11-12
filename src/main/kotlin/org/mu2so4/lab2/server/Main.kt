package org.mu2so4.lab2.server

fun main() {
    val server = Server(12345, 10)

    server.accept()
    val charset = Charsets.UTF_8
    val subFile = server.receiveFile()
    println(subFile.toString(charset))

    server.close()
}