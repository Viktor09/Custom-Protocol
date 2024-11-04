# Custom-Protocol

## How to setup and run the project?

### 1. Setup
I provided some images, go to Run/Debug Configuration, after More Actions ->Edit and fill the fields.
[Alt text](ProtocolKotlin/images/client_and_server_setup1.png)
[Alt text](ProtocolKotlin/images/client_and_server_setup2.png)
[Alt text](ProtocolKotlin/images/client_and_server_setup3.png)

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
