package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.username
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.utils.PermissionUtil
import java.time.Instant

@IsCommand
class UserInfoCommand : Command() {
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

    private fun resolveUser(jda: JDA, arg: String) =
            jda.userCache.find { it.id == arg || it.name == arg || it.username() == arg || it.asMention == arg }
                    ?: throw IllegalArgumentException("No such user found.")

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.serverCtx.guild == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val arg = args.firstOrNull()
        val user = if (arg != null) resolveUser(MC.jda, arg) else MC.userCtx.user
        val member = MC.serverCtx.guild.getMember(user) ?: throw IllegalArgumentException("User not on server.")
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