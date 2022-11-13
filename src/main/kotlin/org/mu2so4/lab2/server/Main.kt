package org.mu2so4.lab2.server

import java.net.SocketException
import java.util.Scanner
import java.util.logging.LogManager

fun main() {
    Server::class.java.classLoader.getResourceAsStream("logging.properties").
        use{ stream -> LogManager.getLogManager().readConfiguration(stream) }

    val server = Server(12345, 10)
    val serverThread = Thread {
        try {
            while (true) {
                server.accept()
            }
        }
        catch(e: SocketException) {
            if(e.message != "Socket closed") {
                throw e
            }
        }
    }
    serverThread.start()

    val scanner = Scanner(System.`in`)
    println("print 'exit' to close the server")
    var cmd = scanner.next()
    while(cmd != "exit") {
        cmd = scanner.next()
    }
    server.close()
    serverThread.join()
}
