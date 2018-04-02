package moe.giga.discord.contexts;

import moe.giga.discord.LucoaBot;
import net.dv8tion.jda.core.entities.Guild;

public final class ServerContext {
    private LucoaBot bot;
    private Guild guild;

    private String prefix;

    public ServerContext(LucoaBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;

        // attach data here
    }

    public Guild getGuild() {
        return guild;
    }

    public String getPrefix() {
        return prefix;
    }
}
