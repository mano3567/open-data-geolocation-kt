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

class GeoLocationService(private val userAgent: String) {
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
                parameter("limit", "1")
            }

            if (response.status == HttpStatusCode.OK) {
                val osmLista: List<OsmResult> = response.body()

                val rawResult = osmLista.firstOrNull() ?: return null

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

    fun close() {
        httpClient.close()
    }
}
