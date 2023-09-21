package com.programmersbox.common

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class AvatarApiService {
    companion object {
        private const val BASE_URL = "https://api.punkapi.com/v2"
    }

    private val json: Json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000
        }
    }

    suspend fun getCharacters(perPage: Int, page: Int) =
        client.get("$BASE_URL/beers?page=$page&per_page=$perPage")
            .bodyAsText()
            .let { json.decodeFromString<List<Beer>>(it) }
}

@Serializable
data class Beer(
    val id: Long,
    val name: String,
    val description: String,
    @SerialName("image_url")
    val imageUrl: String,
)