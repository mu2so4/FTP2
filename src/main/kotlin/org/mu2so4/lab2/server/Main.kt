package org.mu2so4.lab2.server

import java.util.Scanner

fun main() {
    val server = Server(12345, 10)

    val scanner = Scanner(System.`in`)

    val serverThread = Thread {
        while (true) {
            server.accept()
        }
    }
    serverThread.start()

    println("print 'exit' to close the server")
    var cmd = scanner.next()
    while(cmd != "exit") {
        cmd = scanner.next()
    }
    server.close()
}
