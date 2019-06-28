package de.ketrwu.discordtemperature.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import java.io.File

@JsonIgnoreProperties("$${'$'}beanFactory")
abstract class FileConfiguration {

    @Autowired
    @JsonIgnore
    private lateinit var objectMapper: ObjectMapper

    @JsonIgnore
    abstract fun getLocation(): File

    @Suppress("UNCHECKED_CAST")
    fun <T : FileConfiguration> save(task: (T.() -> Unit)? = null) {
        task?.invoke(this as T)
        objectMapper.writeValue(getLocation(), this)
    }

    fun createIfNotExists() = getLocation()
        .let {
            if (!it.exists()) {
                objectMapper.writeValue(it, this)
            }
        }
}