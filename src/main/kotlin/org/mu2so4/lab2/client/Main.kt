package org.mu2so4.lab2.client

import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.system.exitProcess

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

    val client = Client("127.0.0.1", 12345)

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
