package nu.westlin.udpsplitter

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Användning: <lyssnarport> <målport1> <målport2> ... <målportN>")
        return
    }

    // Första argumentet är porten vi lyssnar på
    val listenPort = args[0].toIntOrNull() ?: throw IllegalArgumentException("Ogiltig lyssnarport: ${args[0]}")

    // Resten av argumenten blir målportar
    // drop(1) hoppar över det första elementet och ger oss resten
    val targetPorts = args.drop(1).map { portStr ->
        portStr.toIntOrNull() ?: throw IllegalArgumentException("Ogiltig målport: $portStr")
    }

    // Anropa din funktion
    udpSplitter(listenPort, targetPorts)
}

fun udpSplitter(listenPort: Int, targetPorts: List<Int>) {
    val targetAddress = InetAddress.getByName("127.0.0.1")
    val buffer = ByteArray(4096) // Bufferstorlek som täcker de flesta simracing-spel

    println("--- UDP Port Splitter ---")
    println("Lyssnar på: $listenPort")
    println("Skickar till: localhost:$targetPorts")
    println("Läge: Direkt vidarebefordran (ingen FPS-begränsning)")

    try {
        DatagramSocket(listenPort).use { socket ->
            while (true) {
                // 1. Vänta på inkommande paket från spelet
                val incomingPacket = DatagramPacket(buffer, buffer.size)
                socket.receive(incomingPacket)

                // 2. Skapa och skicka paket destinationerna
                targetPorts.forEach { port ->
                    val outPacket = DatagramPacket(
                        incomingPacket.data,
                        incomingPacket.length,
                        targetAddress,
                        port
                    )
                    socket.send(outPacket)
                }
            }
        }
    } catch (e: SocketException) {
        System.err.println("Kunde inte öppna socket: ${e.message}")
        System.err.println("Tips: Kontrollera att port $listenPort inte redan används av ett annat program.")
        System.err.println("Applikationen avslutas")
        return
    } catch (e: Exception) {
        System.err.println("Ett oväntat fel uppstod: ${e.message}")
        System.err.println("Applikationen avslutas")
        return
    }
}