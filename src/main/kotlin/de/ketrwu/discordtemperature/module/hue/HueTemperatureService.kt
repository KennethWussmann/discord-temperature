package de.ketrwu.discordtemperature.module.hue

import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHBridgeSearchManager
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.hue.sdk.PHSDKListener
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import com.philips.lighting.model.sensor.PHTemperatureSensor
import de.ketrwu.discordtemperature.RoomTemperature
import de.ketrwu.discordtemperature.logger
import de.ketrwu.discordtemperature.service.TemperatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

/**
 * [TemperatureService] that receives temperature from Philips Hue motion sensors
 */
@Service
@ConditionalOnProperty(prefix = "application.temperature", name = ["hue.enabled"])
class HueTemperatureService : TemperatureService, PHSDKListener {

    @Value("${'$'}{application.temperature.unit:CELSIUS}")
    private lateinit var unit: RoomTemperature.Unit

    @Value("${'$'}{application.temperature.hue.bridgeIp:#{null}}")
    private val bridgeIp: String? = null

    @Value("${'$'}{application.temperature.hue.username:DiscordTemperature}")
    private lateinit var username: String

    @Autowired
    private lateinit var fileStorageService: FileStorageService

    private val sdk = PHHueSDK.create()
    private var accessPoint: PHAccessPoint? = null

    companion object {
        private val LOG = logger()
    }

    @PostConstruct
    fun start() = with(fileStorageService.reload()) {
        sdk.notificationManager.registerSDKListener(this@HueTemperatureService)
        if (lastIp != null && userName != null) {
            connectToLastKnown(this)
        } else if (bridgeIp != null) {
            connectToIp()
        } else {
            findBridges()
        }
    }

    private fun connectToLastKnown(config: FileStorageService.FileConfiguration) {
        LOG.info("Connecting to last known bridge ...")
        accessPoint = PHAccessPoint()
        accessPoint!!.ipAddress = config.lastIp
        accessPoint!!.username = config.userName
        sdk.connect(accessPoint)
    }

    private fun findBridges() {
        LOG.info("Searching bridges ...")
        val sm = sdk.getSDKService(PHHueSDK.SEARCH_BRIDGE) as PHBridgeSearchManager
        sm.search(true, true)
    }

    private fun connectToIp() {
        LOG.info("Connecting to configurated bridge ...")
        val accessPoint = PHAccessPoint()
        accessPoint.ipAddress = bridgeIp
        accessPoint.username = username
        sdk.connect(accessPoint)
    }

    override fun getTemperatures(): List<RoomTemperature> = if (sdk.isAccessPointConnected(accessPoint)) {
        sdk.selectedBridge.resourceCache.sensors.values
            .filter { it is PHTemperatureSensor }
            .map { it as PHTemperatureSensor }
            .filter { it.configuration.on && it.configuration.reachable }
            .map { RoomTemperature(it.state.lastUpdated, it.name, it.state.temperature / 100.0, unit) }
    } else throw IllegalStateException("Connection not established")

    override fun onAccessPointsFound(accessPoints: MutableList<PHAccessPoint>) {
        if (accessPoints.isEmpty()) {
            LOG.error("No access points found!")
            System.exit(1)
        }
        LOG.info("Access points found, connecting ...")
        sdk.connect(accessPoints.first())
        accessPoint = accessPoints.first()
    }

    override fun onAuthenticationRequired(accessPoint: PHAccessPoint) {
        LOG.info("PLEASE PRESS THE BUTTON ON THE HUE BRIDGE!")
        sdk.startPushlinkAuthentication(accessPoint)
        this.accessPoint = accessPoint
    }

    override fun onBridgeConnected(bridge: PHBridge, userName: String) {
        val bridgeIp = bridge.resourceCache.bridgeConfiguration.ipAddress
        fileStorageService.update {
            lastIp = bridgeIp
            this.userName = userName
        }
        LOG.info("Connected to Hue Bridge with IP $bridgeIp and username $userName")
    }

    override fun onConnectionLost(accessPoint: PHAccessPoint) {
        this.accessPoint = accessPoint
        LOG.error("Connection to bridge lost! Trying to reconnect ...")
        connectToLastKnown(fileStorageService.reload())
    }

    override fun onConnectionResumed(p0: PHBridge?) {
        LOG.info("Connection to bridge resumed")
    }

    override fun onCacheUpdated(p0: MutableList<Int>?, p1: PHBridge?) { }

    override fun onParsingErrors(p0: MutableList<PHHueParsingError>?) { }

    override fun onError(p0: Int, p1: String?) { }
}