package de.ketrwu.discordtemperature

import org.slf4j.LoggerFactory

fun Any.logger() = LoggerFactory.getLogger(this::class.java)