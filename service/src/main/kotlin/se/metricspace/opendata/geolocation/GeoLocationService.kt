package se.metricspace.opendata.geolocation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Location(
    val addressType: String,
    val boundingBox: List<String>,
    val displayName: String,
    val importance: Double,
    val latitude: Double,
    val licence: String,
    val longitude: Double,
    val name: String,
    val osmId: Long,
    val osmType: String,
    val placeId: Long,
    val placeRank: Int,
    val type: String
)

data class ViewBox(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double
) {
    // Hjälpfunktion för att formatera till Nominatims strängformat
    fun toQueryParam(): String = "$left,$top,$right,$bottom"
}

class GeoLocationService(private val userAgent: String, private val countryCodes: List<String> = emptyList(), private val acceptLanguage: String? = null, private val viewBox: ViewBox? = null) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    private data class OsmResult(
        val addresstype: String,
        val boundingbox: List<String>,
        val display_name: String,
        val importance: Double,
        val lat: String,
        val licence: String,
        val lon: String,
        val name: String,
        val osm_id: Long,
        val osm_type: String,
        val place_id: Long,
        val place_rank: Int,
        val type: String
    )

    suspend fun findLocation(someLocation: String): Location? {
        return try {
            val response = httpClient.get("https://nominatim.openstreetmap.org/search") {
                header("User-Agent", userAgent)
                parameter("q", someLocation)
                parameter("format", "json")
                parameter("limit", "15")
                if (countryCodes.isNotEmpty()) {
                    parameter("countrycodes", countryCodes.joinToString(","))
                }

                acceptLanguage?.let {
                    parameter("accept-language", it)
                }

                viewBox?.let {
                    parameter("viewbox", it.toQueryParam())
                    parameter("bounded", "0")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val osmLista: List<OsmResult> = response.body()
                val rawResult = osmLista.maxByOrNull { it.importance } ?: return null

                Location(
                    addressType = rawResult.addresstype,
                    boundingBox = rawResult.boundingbox,
                    displayName = rawResult.display_name,
                    importance = rawResult.importance,
                    latitude = rawResult.lat.toDouble(),
                    licence = rawResult.licence,
                    longitude = rawResult.lon.toDouble(),
                    name = rawResult.name,
                    osmId = rawResult.osm_id,
                    osmType = rawResult.osm_type,
                    placeId = rawResult.place_id,
                    placeRank = rawResult.place_rank,
                    type = rawResult.type
                )
            } else {
                null
            }
        } catch (e: Exception) {
            // Fångar nätverksfel eller felaktig JSON
            println("Some problem in findLocation: ${e.message}")
            null
        }
    }

    suspend fun findLocations(someLocation: String): List<Location> {
        return try {
            val response = httpClient.get("https://nominatim.openstreetmap.org/search") {
                header("User-Agent", userAgent)
                parameter("q", someLocation)
                parameter("format", "json")
                parameter("limit", "15")

                if (countryCodes.isNotEmpty()) {
                    parameter("countrycodes", countryCodes.joinToString(","))
                }

                acceptLanguage?.let {
                    parameter("accept-language", it)
                }

                viewBox?.let {
                    parameter("viewbox", it.toQueryParam())
                    parameter("bounded", "0")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val osmLista: List<OsmResult> = response.body()
                val locations: List<Location> = osmLista
                    .sortedByDescending { it.importance } // Eller .sortedBy { it.place_rank }
                    .map { result ->
                        Location(
                            addressType = result.addresstype,
                            boundingBox = result.boundingbox,
                            displayName = result.display_name,
                            importance = result.importance,
                            latitude = result.lat.toDouble(),
                            licence = result.licence,
                            longitude = result.lon.toDouble(),
                            name = result.name,
                            osmId = result.osm_id,
                            osmType = result.osm_type,
                            placeId = result.place_id,
                            placeRank = result.place_rank,
                            type = result.type
                        )
                    }
                locations
            } else {
                emptyList<Location>()
            }
        } catch (e: Exception) {
            println("Some problem in findLocation: ${e.message}")
            emptyList<Location>()
        }
    }

    fun close() {
        httpClient.close()
    }
}
