package sample.geolocation.cli

import se.metricspace.opendata.geolocation.GeoLocationService
import kotlinx.coroutines.runBlocking

fun main() {
    val service = GeoLocationService(userAgent = "metricspace.location/1.0.2")

    println("--- Välkommen till GeoService CLI ---")
    println("Skriv namnet på en plats för att slå upp den, eller 'q' för att avsluta.")

    runBlocking {
        while (true) {
            print("\nVal (q för att avsluta): ")

            // readlnOrNull returnerar null om input-strömmen stängs (t.ex. Ctrl+D)
            val input = readlnOrNull()?.trim() ?: break

            if (input.equals("q", ignoreCase = true)) break
            if (input.isBlank()) continue

            println("Söker efter '$input'...")
            val location = service.findLocation(input)

            if (location != null) {
                println("Resultat: ${location.displayName}")
                println("Lat/Lon: ${location.latitude}, ${location.longitude}")
            } else {
                println("Hittade tyvärr inget för '$input'.")
            }
        }
    }
}
