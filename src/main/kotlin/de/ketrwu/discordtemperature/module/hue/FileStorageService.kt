package de.ketrwu.discordtemperature.module.hue

import com.fasterxml.jackson.databind.ObjectMapper
import de.ketrwu.discordtemperature.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct
import javax.swing.JFileChooser

@Service
class FileStorageService {

    private val appData = File(JFileChooser().fileSystemView.defaultDirectory, "DiscordTemperature")
    private val configFile = File(appData, "hue.json")
    private var fileConfiguration = FileConfiguration()

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        private val LOG = logger()
    }

    private fun createFiles() {
        if (!appData.exists()) {
            LOG.info("Created configuration directory \"$appData\"")
            appData.mkdirs()
        }
        if (!configFile.exists()) {
            LOG.info("Created configuration file \"$configFile\"")
            update()
        }
    }

    @PostConstruct
    fun start() {
        createFiles()
        reload()
    }

    fun reload() = objectMapper
        .readValue(configFile, FileConfiguration::class.java)
        .also {
            LOG.info("Reloading configuration file \"$configFile\"")
            fileConfiguration = it
        }

    fun update(task: (FileConfiguration.() -> Unit)? = null) {
        task?.invoke(fileConfiguration)
        objectMapper.writeValue(configFile, fileConfiguration)
    }

    data class FileConfiguration(var lastIp: String? = null, var userName: String? = null)
}