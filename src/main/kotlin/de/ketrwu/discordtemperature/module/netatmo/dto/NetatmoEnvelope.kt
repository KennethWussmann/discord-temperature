package de.ketrwu.discordtemperature.module.netatmo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NetatmoEnvelope<T>(
    val body: T,
    val status: String
)