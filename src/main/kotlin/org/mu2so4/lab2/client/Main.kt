package org.mu2so4.lab2.client

import org.mu2so4.lab2.server.Server
import java.util.Scanner
import java.util.Properties

fun main(args: Array<String>) {
    val filename: String
    if(args.size != 2) {
        val scanner = Scanner(System.`in`)
        filename = scanner.nextLine()
        scanner.close()
    }
    else {
        filename = args[1]
    }

    val resource = Server::class.java.classLoader.
        getResourceAsStream("server.properties")
    val properties = Properties()
    properties.load(resource)

    val address = properties.getProperty("address") ?: "127.0.0.1"
    val port = properties.getProperty("port")?.toInt() ?: 12345

    val client = Client(address, port)

    client.sendFile(filename)
    client.close()

    /*val prop_file = File("server.properties")

    val props  = Client::class.classLoader.getResourceAsStream("pairs_ids.txt").use {
        Properties().apply { load(it) }
    }
    FileInputStream(prop_file).use { props.load(it) }

    println("port: ${props.getProperty("port")}")*/



    //println()
}
