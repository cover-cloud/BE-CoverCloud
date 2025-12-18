package com.covercloud.music.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.util.Base64

@Service
class SpotifyTokenService(
    private val spotifyAccountsClient: WebClient,
    @Value("\${spotify.client-id}") clientId: String,
    @Value("\${spotify.client-secret}") clientSecret: String
) {
    private val auth = "Basic " + Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())

    private var token: String? = null
    private var expiresAt: Instant? = null


    fun getToken(): String? {
        if (token != null && expiresAt!!.isAfter(Instant.now().plusSeconds(60))) {
            return token!!
        }


        val res = spotifyAccountsClient.post()
            .uri("/api/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", auth)
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()!!

        token = res["access_token"] as String
        expiresAt = Instant.now().plusSeconds((res["expires_in"] as Int).toLong())
        return token!!
    }
}