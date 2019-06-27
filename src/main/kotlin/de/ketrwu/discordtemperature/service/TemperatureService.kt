package de.ketrwu.discordtemperature.service

import de.ketrwu.discordtemperature.RoomTemperature

/**
 * A service that can get temperature informations from external services
 */
interface TemperatureService {

    fun getTemperatures(): List<RoomTemperature>

}