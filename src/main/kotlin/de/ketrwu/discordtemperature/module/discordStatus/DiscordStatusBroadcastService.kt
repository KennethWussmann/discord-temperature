package de.ketrwu.discordtemperature.module.discordStatus

import de.ketrwu.discordtemperature.RoomTemperature
import de.ketrwu.discordtemperature.logger
import de.ketrwu.discordtemperature.service.BroadcastService
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
@ConditionalOnProperty(prefix = "application.broadcast", name = ["discordStatus.enabled"])
class DiscordStatusBroadcastService : BroadcastService {

    @Value("${'$'}{application.broadcast.roomName:#{null}}")
    private var roomName: String? = null

    @Value("${'$'}{application.broadcast.roomDisplayName:#{null}}")
    private var roomDisplayName: String? = null

    @Value("${'$'}{application.broadcast.discordStatus.token}")
    private lateinit var token: String

    @Value("${'$'}{application.broadcast.discordStatus.format}")
    private lateinit var format: String

    @Value("${'$'}{application.broadcast.discordStatus.statusType:DEFAULT}")
    private lateinit var statusType: Game.GameType

    private var jda: JDA? = null

    companion object {
        private val LOG = logger()
    }

    @PostConstruct
    fun connect() {
        jda = JDABuilder(AccountType.CLIENT).setToken(token).build()
    }

    override fun broadcast(temperatures: List<RoomTemperature>) {
        if (roomName != null) {
            val temp = temperatures.firstOrNull { it.roomName.equals(roomName, true) }
            if (temp == null) {
                LOG.error("Room with name $roomName not found! Please check configuration")
                System.exit(1)
                return
            }
            temp.roomName = roomDisplayName ?: temp.roomName
            jda?.presence?.game = Game.of(
                statusType,
                format
                    .replace("{temperature}", "${temp.temperature}°${temp.unit.symbol}")
                    .replace("{humidity}", temp.humidity?.let { "$it%" } ?: "")
                    .replace("{noise}", temp.noise?.let { "$it dB" } ?: "")
                    .replace("{co2}", temp.co2?.let { "$it ppm" } ?: "")
                    .replace("{room}", temp.roomName)
            )
        } else throw UnsupportedOperationException("Broadcasting multiple temperatures is not supported yet! Please specify a room name in the configuration!")
    }
}