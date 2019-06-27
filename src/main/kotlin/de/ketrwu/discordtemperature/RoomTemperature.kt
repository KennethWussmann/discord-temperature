package de.ketrwu.discordtemperature

import java.util.Date

data class RoomTemperature(
    val lastUpdated: Date,
    var roomName: String,
    val temperature: Double,
    val unit: Unit
) {
    enum class Unit(val symbol: String) {
        CELSIUS("C"),
        FAHRENHEIT("F")
    }
}