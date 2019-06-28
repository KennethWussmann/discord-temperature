package de.ketrwu.discordtemperature.module.netatmo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import de.ketrwu.discordtemperature.RoomTemperature
import de.ketrwu.discordtemperature.logger
import de.ketrwu.discordtemperature.module.netatmo.dto.DeviceList
import de.ketrwu.discordtemperature.module.netatmo.dto.NetatmoEnvelope
import de.ketrwu.discordtemperature.module.netatmo.dto.OAuthTokenResponse
import de.ketrwu.discordtemperature.service.FileStorageService
import de.ketrwu.discordtemperature.service.TemperatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
@ConditionalOnProperty(prefix = "application.temperature", name = ["netatmo.enabled"])
open class NetatmoTemperatureService : TemperatureService, CacheLoader<String, OAuthTokenResponse>() {

    @Value("${'$'}{application.temperature.unit:CELSIUS}")
    private lateinit var unit: RoomTemperature.Unit

    @Value("${'$'}{application.temperature.netatmo.clientId}")
    private lateinit var clientId: String

    @Value("${'$'}{application.temperature.netatmo.clientSecret}")
    private lateinit var clientSecret: String

    @Value("${'$'}{application.temperature.netatmo.deviceId}")
    private lateinit var deviceId: String

    @Autowired
    private lateinit var fileStorageService: FileStorageService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    val cache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(2, TimeUnit.HOURS)
        .build(this)
    private val baseUri = URI("https://api.netatmo.com")
    private var initialized = false

    companion object {
        private val LOG = logger()
    }

    @PostConstruct
    fun start() {
        FuelManager.instance.basePath = baseUri.toString()
        val config = fileStorageService.load(NetatmoConfiguration::class)
        if (config.refreshToken == null) {
            val authUrl = URI("http://localhost:8080/netatmo")
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(authUrl)
            } else {
                LOG.info("PLEASE OPEN THIS URL IN YOUR BROWSER TO AUTHORIZE NETATMO: $authUrl")
            }
        } else {
            initialized = true
        }
    }

    override fun load(refreshToken: String): OAuthTokenResponse {
        LOG.info("Refreshing Netatmo access token ...")
        val (_, _, result) = Fuel
            .post(
                "/oauth2/token",
                listOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken,
                    "client_id" to clientId,
                    "client_secret" to clientSecret
                )
            )
            .responseString()
        return if (result is Result.Success) {
            saveToken(result.get())
        } else {
            throw RuntimeException("There was an unknown error refreshing your token!")
        }
    }

    open fun requestAccessToken(redirectUri: String, code: String): OAuthTokenResponse {
        val (_, _, result) = Fuel
            .post(
                "/oauth2/token",
                listOf(
                    "grant_type" to "authorization_code",
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "code" to code,
                    "redirect_uri" to redirectUri
                )
            )
            .responseString()
        return if (result is Result.Success) {
            saveToken(result.get())
        } else {
            throw RuntimeException("There was an unknown error!")
        }
    }

    private fun saveToken(json: String): OAuthTokenResponse {
        val tokenResponse = objectMapper.readValue(json, OAuthTokenResponse::class.java)
        fileStorageService
            .load(NetatmoConfiguration::class)
            .save<NetatmoConfiguration> {
                refreshToken = tokenResponse!!.refreshToken
            }
        LOG.info("Successfully authenticated against Netatmo! Expires in ${tokenResponse!!.expiresIn}")
        initialized = true
        return tokenResponse
    }

    fun getAuthorizeUrl(redirectUri: String): String = UriComponentsBuilder.newInstance()
        .uri(baseUri)
        .path("/oauth2/authorize")
        .queryParam("client_id", clientId.urlEncoded())
        .queryParam("redirect_uri", redirectUri.urlEncoded())
        .queryParam("scope", "read_homecoach".urlEncoded())
        .queryParam("state", UUID.randomUUID().toString().urlEncoded())
        .build()
        .toUriString()

    private fun String.urlEncoded() = URLEncoder.encode(this, "UTF-8")

    override fun getTemperatures(): List<RoomTemperature> = if (initialized) {
        val (_, _, result) = Fuel
            .get("/api/gethomecoachsdata", listOf(
                "access_token" to cache.get(fileStorageService.load(NetatmoConfiguration::class).refreshToken!!).accessToken,
                "device_id" to deviceId
            ))
            .responseString()
        if (result is Result.Success) {
            objectMapper
                .readValue<NetatmoEnvelope<DeviceList>>(result.get(), object : TypeReference<NetatmoEnvelope<DeviceList>>() {})
                .body
                .devices
                .map {
                    RoomTemperature(
                        lastUpdated = Date(it.dashboardData.lastUpdated * 1000L),
                        roomName = it.stationName,
                        temperature = it.dashboardData.temperature,
                        humidity = it.dashboardData.humidity,
                        noise = it.dashboardData.noise,
                        co2 = it.dashboardData.co2,
                        unit = unit
                    )
                }
        } else emptyList()
    } else throw IllegalStateException("Not ready yet")
}