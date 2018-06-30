package moe.giga.discord

import java.util.concurrent.atomic.AtomicLong

class BotStatistics {
    val messages = AtomicLong(0)
    val processedCommands = AtomicLong(0)
}