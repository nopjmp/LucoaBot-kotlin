package moe.giga.discord.util

import net.dv8tion.jda.core.entities.User

fun User.username() = "${this.name}#${this.discriminator}"