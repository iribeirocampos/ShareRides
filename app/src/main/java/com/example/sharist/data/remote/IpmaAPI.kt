package com.example.sharist.data.remote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlin.math.*


object IpmaApi {

    // ---------------------------
    // DATA CLASSES
    // ---------------------------

    data class WeatherResponse(
        val data: List<ForecastDay>
    )

    data class ForecastDay(
        val forecastDate: String,
        val tMin: String,
        val tMax: String,
        val precipitaProb: String?,
        val idWeatherType: Int?
    )
    data class IpmaLocation(
        val globalIdLocal: Int,
        val local: String,
        val latitude: Double,
        val longitude: Double
    )

    data class LocationsResponse(
        val data: List<IpmaLocation>
    )

    // ---------------------------
    // RETROFIT INTERFACE
    // ---------------------------

    interface Service {

        @GET("open-data/forecast/meteorology/cities/daily/{id}.json")
        suspend fun getForecast(@Path("id") cityId: Int): WeatherResponse

        @GET("open-data/distrits-islands.json")
        suspend fun getLocations(): LocationsResponse
    }

    // ---------------------------
    // RETROFIT INSTANCE
    // ---------------------------

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ipma.pt/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(Service::class.java)

    fun findNearestLocation(
        userLat: Double,
        userLon: Double,
        locations: List<IpmaLocation>
    ): IpmaLocation {

        return locations.minByOrNull {
            distance(
                userLat,
                userLon,
                it.latitude,
                it.longitude
            )

        }!!
    }

    fun distance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        val earthRadius = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    // ---------------------------
    // PUBLIC METHODS
    // ---------------------------

    suspend fun getForecast(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {

        val locations = api.getLocations().data

        val nearest = findNearestLocation(
            latitude,
            longitude,
            locations
        )
        return api.getForecast(nearest.globalIdLocal)
    }
}