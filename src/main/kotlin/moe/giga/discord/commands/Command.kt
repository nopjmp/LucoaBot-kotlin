package moe.giga.discord.commands

import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import net.dv8tion.jda.core.JDABuilder

interface Command {
    fun execute(MC: MessageContext, args: List<String>)

    fun init(builder: JDABuilder) {

    }

    val name: String
        get() = ""
    val description: String
        get() = ""
    val usage: String
        get() = name
    val alias: String?
        get() = null
    val hidden: Boolean
        get() = false
    val level: AccessLevel
        get() = AccessLevel.USER
}
