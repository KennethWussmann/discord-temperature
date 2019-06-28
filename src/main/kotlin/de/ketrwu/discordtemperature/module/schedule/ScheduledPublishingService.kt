package de.ketrwu.discordtemperature.module.schedule

import de.ketrwu.discordtemperature.logger
import de.ketrwu.discordtemperature.service.BroadcastService
import de.ketrwu.discordtemperature.service.PublishingService
import de.ketrwu.discordtemperature.service.TemperatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * [PublishingService] that receives and sends temperature data on a scheduled basis
 */
@Service
@ConditionalOnProperty(prefix = "application.publish", name = ["scheduled.enabled"])
class ScheduledPublishingService : PublishingService {

    @Autowired
    private lateinit var broadcastService: BroadcastService

    @Autowired
    private lateinit var temperatureService: TemperatureService

    private var tries = 10

    companion object {
        private val LOG = logger()
    }

    @Scheduled(fixedRateString = "${'$'}{application.publish.scheduled.fixedRate:10000}")
    fun publish() = try {
        broadcastService.broadcast(temperatureService.getTemperatures())
        tries = 10
    } catch (e: IllegalStateException) {
        if (tries < 0) {
            LOG.error("Failed to startup all services, please check log and configuration!", e)
            System.exit(1)
        }
        LOG.error("Not all services are ready yet, waiting ...")
        tries--
    }
}