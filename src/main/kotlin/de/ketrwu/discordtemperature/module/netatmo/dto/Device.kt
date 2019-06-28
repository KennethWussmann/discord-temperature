package de.ketrwu.discordtemperature.module.netatmo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Device(
    @JsonProperty("station_name") val stationName: String,
    @JsonProperty("dashboard_data") val dashboardData: DashboardData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DashboardData(
        @JsonProperty("time_utc") val lastUpdated: Long,
        @JsonProperty("Temperature") val temperature: Double,
        @JsonProperty("Humidity") val humidity: Double,
        @JsonProperty("Noise") val noise: Int,
        @JsonProperty("CO2") val co2: Int
    )
}