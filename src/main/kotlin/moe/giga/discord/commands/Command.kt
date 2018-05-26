package moe.giga.discord.commands

import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import net.dv8tion.jda.core.JDABuilder

abstract class Command {
    abstract fun execute(MC: MessageContext, args: List<String>)

    open fun init(builder: JDABuilder) {

    }

    open val name: String = ""
    open val description: String = ""
    open val usage: String
        get() = name
    open val alias: String? = null
    open val hidden: Boolean = false
    open val level: AccessLevel = AccessLevel.USER
}
