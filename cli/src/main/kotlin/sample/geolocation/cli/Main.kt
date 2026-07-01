package sample.geolocation.cli

import se.metricspace.opendata.geolocation.GeoLocationService
import kotlinx.coroutines.runBlocking

fun main() {
    val geoLocationService = GeoLocationService(userAgent = "metricspace.location/1.0.7", listOf("se", "no", "dk", "fi"))

    println("--- Välkommen till GeoService CLI ---")
    println("Skriv namnet på en plats för att slå upp den, eller 'q' för att avsluta.")

    runBlocking {
        while (true) {
            print("\nVal (q för att avsluta): ")

            // readlnOrNull returnerar null om input-strömmen stängs (t.ex. Ctrl+D)
            val userInput = readlnOrNull()?.trim() ?: break

            if (userInput.equals("q", ignoreCase = true)) break
            if (userInput.isBlank()) continue

            println("Söker efter '$userInput'...")
            val locations = geoLocationService.findLocations(userInput)
            locations.forEach {
                println(it)
            }
            val location = geoLocationService.findLocation(userInput)

            if (location != null) {
                println("Resultat: ${location.displayName}")
                println("Lat/Lon: ${location.latitude}, ${location.longitude}")
                println("Name: ${location.name}")
                println("AddressType: ${location.addressType}")
                println("Type: ${location.type}")
                println("BoundingBox: ${location.boundingBox}")
                println("Licence: ${location.licence}")
                println("Importance: ${location.importance}")
                println("OsmId: ${location.osmId}")
                println("OsmType: ${location.osmType}")
                println("PlaceId: ${location.placeId}")
                println("PlaceRank: ${location.placeRank}")
            } else {
                println("Hittade tyvärr inget för '$userInput'.")
            }
        }
    }

    geoLocationService.close()
}
