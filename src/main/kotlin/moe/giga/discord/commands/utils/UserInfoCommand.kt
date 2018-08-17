package moe.giga.discord.commands.utils

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.username
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.utils.PermissionUtil
import java.time.Instant

@Suppress("unused")
class UserInfoCommand : Command {
    override val name = "userinfo"
    override val description = "Displays information about the user"
    override val usage = "userinfo <id | username | username#discriminator | mention>"

    private val keyPermissions = listOf(
            Permission.KICK_MEMBERS,
            Permission.BAN_MEMBERS,
            Permission.MANAGE_PERMISSIONS,
            Permission.MANAGE_ROLES,
            Permission.MANAGE_SERVER,
            Permission.MANAGE_WEBHOOKS,
            Permission.NICKNAME_MANAGE,
            Permission.MESSAGE_MENTION_EVERYONE
    )

    private fun isApplied(permissions: Long, perms: Permission) =
            permissions and perms.rawValue == perms.rawValue

    private fun resolveUser(guild: Guild, arg: String) =
            guild.memberCache.find {
                it.user.id == arg || it.user.name == arg || it.user.username() == arg || it.nickname == arg || it.asMention == arg
            }?.user ?: throw IllegalArgumentException("No such user found.")

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val arg = args.joinToString(" ")
        val user = if (arg.isNotBlank()) resolveUser(MC.server.guild, arg) else MC.user.user
        val member = MC.server.guild.getMember(user) ?: throw IllegalArgumentException("User not on server.")
        val effectivePermission = PermissionUtil.getEffectivePermission(member)

        val builder = EmbedBuilder()
                .setAuthor(user.username(), null, user.effectiveAvatarUrl)
                .setFooter("ID: ${user.id}", null)
                .setThumbnail(user.effectiveAvatarUrl)
                .setDescription(user.asMention)
                .setTimestamp(Instant.now())
                .addField("Status", member.onlineStatus.key.capitalize(), true)
                .addField("Joined", member.joinDate.toString(), true)
                .addField("Registered", user.creationTime.toString(), true)
                .addField("Roles [${member.roles.size}]", member.roles.joinToString(" ") { it.asMention }, true)

        // XXX: Permissions use "getName" directly due to confusion between Enum.name and Permission.name
        if (isApplied(effectivePermission, Permission.ADMINISTRATOR)) {
            builder.addField("Key Permissions", Permission.ADMINISTRATOR.getName(), true)
        } else {
            val perms = keyPermissions.filter { isApplied(effectivePermission, it) }
            if (perms.isNotEmpty())
                builder.addField("Key Permissions", perms.joinToString { it.getName() }, true)
        }

        MC.sendMessage(builder.build()).queue()
    }
}