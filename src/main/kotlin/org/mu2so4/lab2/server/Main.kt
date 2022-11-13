package org.mu2so4.lab2.server

import java.net.SocketException
import java.util.Scanner

fun main() {
    val server = Server(12345, 10)

    val scanner = Scanner(System.`in`)

    val serverThread = Thread {
        try {
            while (true) {
                server.accept()
            }
        }
        catch(e: SocketException) {
            if(e.message == "Socket closed") {
                println(e.message)
            }
            else {
                throw e
            }
        }
    }
    serverThread.start()

    println("print 'exit' to close the server")
    var cmd = scanner.next()
    while(cmd != "exit") {
        cmd = scanner.next()
    }
    server.close()
    serverThread.join()
}
