package de.ketrwu.discordtemperature

import de.ketrwu.discordtemperature.module.hue.FileStorageService
import de.ketrwu.discordtemperature.service.PublishingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["de.ketrwu.discordtemperature"])
open class DiscordTemperatureApplication : SpringBootServletInitializer() {

    @Suppress("unused")
    @Autowired
    private lateinit var publishingService: PublishingService

    @Suppress("unused")
    @Autowired
    private lateinit var fileStorageService: FileStorageService

    override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
        return builder.sources(DiscordTemperatureApplication::class.java)
    }
}

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    SpringApplication.run(DiscordTemperatureApplication::class.java, *args)
}
