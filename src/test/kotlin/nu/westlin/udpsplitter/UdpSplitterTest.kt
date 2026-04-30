package nu.westlin.udpsplitter

import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class UdpSplitterTest {

    @Test
    fun `test splitting`() {
        val listenPort = 15000
        val targetPorts = listOf(16000, 17000)
        val message = "Speed: 240km/h"

        // Vi använder en Map för att lagra det som landar på varje port
        val receivedData = ConcurrentHashMap<Int, String>()

        // 1. Starta mottagare för varje målport
        targetPorts.forEach { port ->
            thread(isDaemon = true) {
                val socket = DatagramSocket(port)
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                socket.use {
                    it.receive(packet)
                    receivedData[port] = String(packet.data, 0, packet.length)
                }
            }
        }

        // 2. Starta din splitter (som nu är i en egen funktion)
        thread(isDaemon = true) {
            udpSplitter(listenPort, targetPorts)
        }

        // 3. Skicka test-datagrammet
        val senderSocket = DatagramSocket()
        val bytes = message.toByteArray()
        val sendPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName("127.0.0.1"), listenPort)
        senderSocket.send(sendPacket)
        senderSocket.close()

        // 4. AWAITILITY - Här magin händer
        // Vi väntar upp till 2 sekunder, men fortsätter SÅ FORT båda värdena finns där.
        await.atMost(Duration.ofSeconds(2)).untilAsserted {
            assertEquals(message, receivedData[16000], "Port 16000 saknar data")
            assertEquals(message, receivedData[17000], "Port 17000 saknar data")
        }
    }
}