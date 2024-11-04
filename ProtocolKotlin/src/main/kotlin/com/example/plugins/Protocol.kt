import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Protocol(
    var type: Byte,
    var reserved: UShort,
    var contentLength: UInt,
    var content: ByteArray = ByteArray(1500)
) {
    fun packProtocol(): ByteArray {
        val buffer = ByteBuffer.allocate(1507).order(ByteOrder.BIG_ENDIAN)
        buffer.put(type)                            // 1 byte for type
        buffer.putShort(reserved.toShort())         // 2 bytes for reserved
        buffer.putInt(contentLength.toInt())        // 4 bytes for content length
        buffer.put(content)                         // 1500 bytes of content
        return buffer.array()
    }

    companion object {
        fun unpackProtocol(bytes: ByteArray): Protocol {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
            val type = buffer.get()
            val reserved = buffer.short.toUShort()
            val contentLength = buffer.int.toUInt()
            val content = ByteArray(1500)
            buffer.get(content)
            return Protocol(type, reserved, contentLength, content)
        }
    }
}
