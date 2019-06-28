package de.ketrwu.discordtemperature.module.discordRpc

import club.minnced.discord.rpc.DiscordEventHandlers
import de.ketrwu.discordtemperature.RoomTemperature
import de.ketrwu.discordtemperature.logger
import de.ketrwu.discordtemperature.service.BroadcastService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.concurrent.thread


/**
 * [BroadcastService] that broadcasts the temperature to given rooms
 */
@Service
@ConditionalOnProperty(prefix = "application.broadcast", name = ["discordRpc.enabled"])
class DiscordRPCBroadcastService : BroadcastService {

    @Value("${'$'}{application.broadcast.roomName:#{null}}")
    private var roomName: String? = null

    @Value("${'$'}{application.broadcast.roomDisplayName:#{null}}")
    private var roomDisplayName: String? = null

    @Value("${'$'}{application.broadcast.discordRpc.clientId:593793748056539136}")
    private lateinit var clientId: String

    @Value("${'$'}{application.broadcast.discordRpc.format}")
    private lateinit var format: String

    companion object {
        private val LOG = logger()
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
            discord {
                presence {
                    details = temp.roomName
                    state = format
                        .replace("{temperature}", "${temp.temperature}°${temp.unit.symbol}")
                        .replace("{humidity}", temp.humidity?.let { "$it%" } ?: "")
                        .replace("{noise}", temp.noise?.let { "$it dB" } ?: "")
                        .replace("{co2}", temp.co2?.let { "$it ppm" } ?: "")
                        .replace("{room}", temp.roomName)
                    startTimestamp = temp.lastUpdated.time / 1000
                }
            }
        } else throw UnsupportedOperationException("Broadcasting multiple temperatures is not supported yet! Please specify a room name in the configuration!")
    }

    @PostConstruct
    private fun connect() {
        discord {
            val handlers = handlers {
                errored = DiscordEventHandlers.OnStatus { code, message -> LOG.info("Discord RPC status: $code: $message") }
            }
            Discord_Initialize(clientId, handlers, true, null)
            thread(name = "RPC-Callback-Handler") {
                while (!Thread.currentThread().isInterrupted) {
                    Discord_RunCallbacks()
                    Thread.sleep(2000)
                }
            }
        }
    }
}