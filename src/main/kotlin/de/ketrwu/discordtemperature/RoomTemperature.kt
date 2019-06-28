package de.ketrwu.discordtemperature

import java.util.Date

data class RoomTemperature(
    val lastUpdated: Date,
    var roomName: String,
    val temperature: Double,
    val unit: Unit,
    val humidity: Double? = null,
    val noise: Int? = null,
    val co2: Int? = null
) {
    enum class Unit(val symbol: String) {
        CELSIUS("C"),
        FAHRENHEIT("F")
    }
}