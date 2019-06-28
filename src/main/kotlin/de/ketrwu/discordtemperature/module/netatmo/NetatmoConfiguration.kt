package de.ketrwu.discordtemperature.module.netatmo

import de.ketrwu.discordtemperature.config.FileConfiguration
import de.ketrwu.discordtemperature.service.FileStorageService
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
open class NetatmoConfiguration(
    var refreshToken: String? = null
) : FileConfiguration() {
    override fun getLocation() = File(FileStorageService.APP_DATA, "netatmo.json")
}