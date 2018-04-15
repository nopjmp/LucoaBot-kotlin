package moe.giga.discord.util;

import net.dv8tion.jda.core.entities.User;

public class MiscUtils {
    public static String username(User user) {
        return String.format("%s#%s", user.getName(), user.getDiscriminator());
    }
}
