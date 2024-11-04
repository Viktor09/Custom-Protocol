# Custom-Protocol

## How to setup and run the project?

### 1. Setup
I provided some images, go to Run/Debug Configuration, after More Actions ->Edit and fill the fields.
[Alt text](/blob/main/ProtocolKotlin/images/client_and_server_setup1.png)
[Alt text](/blob/main/ProtocolKotlin/images/client_and_server_setup2.png)
[Alt text](/blob/main/ProtocolKotlin/images/client_and_server_setup3.png)

### 2. Run
Go to Run/Debug Configuration, select the server and run it, then run the client.

## Implementation

### 1. Protocol.kt
Here I created a class where i define the Protocol with the specified fields.
I created a constructor that keeps the type, reserved, contentLength and content.
Then I have 2 methods that packs the structure and unpack it. I find it really useful the ByteArray that acts like a char * in C.

### 2. Server.kt
I researched a little bit on internet to find how a socket client-server model looks like in kotlin.
I was used to do it in C, but I accepted the challenge and I tried something new.
Just like in C, I am setting up the ip and the port for client-server communication.
`aSocket()` function binds the server to the provided IP and port, making ready to accept client connections.
After that, I enter a loop where server listens for client connection.
The last thing that is done in this code is to handle the cases provided in the task.
File operations are necessary due to stock the contents.

### 3. Client.kt

Like server, here i am setting up the socket connection.
A coroutine reads the messages from server.
And the difference here is the pack function called to transmit the data to the server and handle there.

### 4. Mentions

I used mutex lock in Kotlin because the couroutine must print to the console at the right time, not simultaneously.
In C we don't do that because of the I/O blocking input/output methods (`read()`/`write()`).

## Bibliography

`https://ktor.io/docs/server-sockets.html`
`https://medium.com/@android-world/kotlin-mutex-a-comprehensive-guide-a79d0f4f2de7`

