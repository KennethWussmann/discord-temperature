package de.ketrwu.discordtemperature.module.netatmo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeviceList(
    val devices: List<Device> = mutableListOf()
)