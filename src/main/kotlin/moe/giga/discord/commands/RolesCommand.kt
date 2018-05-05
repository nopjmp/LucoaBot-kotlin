package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Role

@IsCommand
class RolesCommand : Command() {
    override val name = "roles"
    override val aliases = arrayOf("lsar")
    override val description = "Lists self assignable roles. Defaults to $AS_MENTION_WORD as mentions."
    override val usage = "roles <mentions ($AS_MENTION)>"

    private fun formatRole(asMention: Boolean, role: Role): String {
        return if (asMention)
            role.asMention
        else
            role.name
    }

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val asMention = args.firstOrNull()?.toBoolean() ?: AS_MENTION
        val roles = MC.serverCtx.getServerSelfRoles()

        val embedBuilder = EmbedBuilder().setTitle("Self Assignable Roles")

        val proc = roles.mapValues { it.value.map { MC.serverCtx.guild.getRoleById(it) } }

        if (proc.isNotEmpty()) {
            embedBuilder.addField("default", proc["default"]?.joinToString { formatRole(asMention, it) }, true)
            proc.filterKeys { it != "default" }.onEach {
                embedBuilder.addField(it.key, it.value.joinToString { formatRole(asMention, it) }, true)
            }
        } else {
            embedBuilder.setDescription("There are no self assignable roles.")
        }

        MC.sendMessage(embedBuilder.build()).queue()
    }

    companion object {
        const val AS_MENTION = true
        const val AS_MENTION_WORD = "show"
    }
}