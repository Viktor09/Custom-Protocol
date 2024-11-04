package com.example

import Protocol
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    runBlocking {

        val selectorManager = SelectorManager(Dispatchers.IO)
        val IP = args.getOrElse(0) { "127.0.0.1" }
        val Port = args.getOrElse(1) { "9002" }.toInt()
        val socket = aSocket(selectorManager).tcp().connect(IP, Port)
        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)
        val consoleMutex = Mutex()
        val responseChannel = Channel<String>()

        consoleMutex.withLock {
            println("Connected to server at $IP:$Port")
        }

        // Launch a coroutine to handle receiving messages from the server
        launch(Dispatchers.IO) {
            try {
                while (true) {
                    val message = receiveChannel.readUTF8Line()
                    if (message != null) {
                        responseChannel.send(message)
                    } else {
                        responseChannel.send("Server closed the connection")
                        socket.close()
                        selectorManager.close()
                        exitProcess(0)
                    }
                }
            } catch (e: Throwable) {
                responseChannel.send("Disconnected from server.")
            }
        }

        while (true) {
            val type: Byte?
            val typeInput: String
            consoleMutex.withLock {
                print("Enter message type: ")
                typeInput = readln()
            }
            type = typeInput.toByteOrNull()
            if (type == null) {
                consoleMutex.withLock {
                    println("Invalid input. Please enter a valid byte value.")
                }
                continue
            }

            val protocol: Protocol = when (type) {
                1.toByte(), 3.toByte(), 4.toByte(), 5.toByte() -> Protocol(
                    type = type,
                    reserved = 0u,
                    contentLength = 0u,
                    content = ByteArray(1500) { 0 }
                )
                2.toByte() -> prepareToPackProtocol(type, consoleMutex)
                else -> {
                    consoleMutex.withLock {
                        println("Provided message type: $type doesn't exist!")
                    }
                    continue
                }
            }

            val packedData = protocol.packProtocol()
            sendChannel.writeFully(packedData)

            if(type != 1.toByte()){
                // Wait for server's response
                val serverResponse = responseChannel.receive()
                consoleMutex.withLock {
                    println("Server: $serverResponse")
                }
                // Check for disconnection
                if (serverResponse == "Server closed the connection" || serverResponse == "Disconnected from server.") {
                    break
                }
            }


        }

    }
}

private suspend fun prepareToPackProtocol(type: Byte, consoleMutex: Mutex): Protocol {
    val contentString: String
    consoleMutex.withLock {
        print("Enter content: ")
        contentString = readln()
    }

    val contentBytes = contentString.toByteArray()
    val paddedContent = ByteArray(1500) { 0 }
    System.arraycopy(
        contentBytes, 0, paddedContent, 0,
        contentBytes.size.coerceAtMost(1500)
    )

    return Protocol(
        type = type,
        reserved = 0u,
        contentLength = contentBytes.size.toUInt().coerceAtMost(1500u),
        content = paddedContent
    )
}
