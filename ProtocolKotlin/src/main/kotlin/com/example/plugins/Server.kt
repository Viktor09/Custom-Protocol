package com.example

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import Protocol
import java.io.File


fun main(args: Array<String>) {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val IP = args.getOrElse(0) { "0.0.0.0" }
        val Port = args.getOrElse(1) { "9002" }.toInt()
        val serverSocket = aSocket(selectorManager).tcp().bind(IP, Port)
        val file = File("write_file.txt")


        while (true) {
            val socket = serverSocket.accept()
            println("Accepted connection from ${socket.remoteAddress}")

            launch {
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel(autoFlush = true)

                while (true) {
                    // Read the fixed-size message
                    val buffer = ByteArray(1507) { 0 }
                    receiveChannel.readFully(buffer)

                    // Unpack the received data into a Protocol instance
                    val protocol = Protocol.unpackProtocol(buffer)
                    val type = protocol.type
                    val contentLength = protocol.contentLength.toInt()
                    val contentString = String(protocol.content, 0, contentLength)

                    println("Processing message of type $type")
                    when (type) {
                        1.toByte() -> {
                            if (protocol.contentLength != 0u) {
                                sendChannel.writeStringUtf8("Error!\n")
                            }
                        }
                        2.toByte() -> {
                            if (contentLength <= 0 || contentLength > protocol.content.size) {
                                sendChannel.writeStringUtf8("Error, invalid content length!\n")
                            } else {
                                // Write to file
                                val contentData = contentString + "\n"
                                try {
                                    file.appendText(contentData)
                                    sendChannel.writeStringUtf8("OK, the content is good!\n")
                                } catch (e: Exception) {
                                    sendChannel.writeStringUtf8("Error writing to file: ${e.message}\n")
                                }
                            }
                        }
                        3.toByte() -> {
                            if (protocol.contentLength != 0u) {
                                sendChannel.writeStringUtf8("Error, the provided packet is wrong!\n")
                            } else {
                                val content = file.readText()
                                println("File Content:\n$content")
                                // Clear file content
                                try {
                                    file.writeText("")
                                    sendChannel.writeStringUtf8("OK, the file was cleared!\n")
                                } catch (e: Exception) {
                                    sendChannel.writeStringUtf8("Error clearing the file: ${e.message}\n")
                                }
                            }
                        }
                        4.toByte() -> {
                            sendChannel.writeStringUtf8("Something wrong happened!\n")
                        }
                        5.toByte() -> {
                            if (protocol.contentLength != 0u) {
                                sendChannel.writeStringUtf8("Error, the packet wasn't received!\n")
                            } else {
                                sendChannel.writeStringUtf8("OK, the packet was received!\n")
                            }
                        }
                        else -> throw IllegalArgumentException("Provided message type: $type doesn't exist!\n")
                    }
                    println("Received from client: type=${protocol.type}, contentLength=${protocol.contentLength}, content=$contentString")
                }
            }
        }
    }
}



