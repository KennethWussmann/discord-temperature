package de.ketrwu.discordtemperature.service

import de.ketrwu.discordtemperature.RoomTemperature

/**
 * A service that can broadcast the temperature information to external services.
 */
interface BroadcastService {

    fun broadcast(temperatures: List<RoomTemperature>)

}