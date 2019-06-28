package de.ketrwu.discordtemperature.module.hue

import de.ketrwu.discordtemperature.config.FileConfiguration
import de.ketrwu.discordtemperature.service.FileStorageService
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
open class HueConfiguration(
    var lastIp: String? = null,
    var userName: String? = null
) : FileConfiguration() {
    override fun getLocation(): File = File(FileStorageService.APP_DATA, "hue.json")
}