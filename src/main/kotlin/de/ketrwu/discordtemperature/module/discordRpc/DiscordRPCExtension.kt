package de.ketrwu.discordtemperature.module.discordRpc

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence

fun discord(block: DiscordRPC.() -> Unit) = DiscordRPC.INSTANCE.apply(block)
fun handlers(block: DiscordEventHandlers.() -> Unit) = DiscordEventHandlers().apply(block)
fun DiscordRPC.presence(block: DiscordRichPresence.() -> Unit) {
    Discord_UpdatePresence(DiscordRichPresence().apply(block))
}