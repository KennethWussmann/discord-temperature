package de.ketrwu.discordtemperature.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.ketrwu.discordtemperature.config.FileConfiguration
import de.ketrwu.discordtemperature.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct
import javax.swing.JFileChooser
import kotlin.reflect.KClass

@Service
class FileStorageService {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        val APP_DATA = File(JFileChooser().fileSystemView.defaultDirectory, "DiscordTemperature")
        private val LOG = logger()
    }

    private fun createFiles() {
        if (!APP_DATA.exists()) {
            LOG.info("Created configuration directory \"$APP_DATA\"")
            APP_DATA.mkdirs()
        }
    }

    @PostConstruct
    fun start() {
        createFiles()
    }

    fun <T : FileConfiguration> load(clazz: KClass<T>): T = applicationContext
        .getBean(clazz.java)
        .also { it.createIfNotExists() }
        .getLocation()
        .let { objectMapper.readValue(it, clazz.java) }
        .also { applicationContext.autowireCapableBeanFactory.autowireBean(it) }


}