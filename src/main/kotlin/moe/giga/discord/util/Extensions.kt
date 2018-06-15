package moe.giga.discord.util

import net.dv8tion.jda.core.entities.User
import java.sql.ResultSet

fun User.username() = "${this.name}#${this.discriminator}"

fun ResultSet.isEmpty() = !this.isBeforeFirst && this.row == 0
