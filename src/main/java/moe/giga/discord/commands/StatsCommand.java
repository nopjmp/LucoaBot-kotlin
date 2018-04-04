package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.contexts.MessageContext;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@CommandInfo(name = "stats", description = "Displays stats about the bot.")
public final class StatsCommand extends Command {

    private Properties properties = new Properties();

    @Override
    public void init(JDABuilder builder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            this.properties.load(loader.getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommand(MessageContext MC, String[] args) {
        String version = this.properties.getProperty("info.build.version", "(unknown)");
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        SelfUser bot = MC.getJDA().getSelfUser();

        Duration uptime = Duration.ofMillis(rb.getUptime());
        String uptimeStr = uptime.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();

        String memoryStr = String.format("%dMB / %dMB", heapMemoryUsage.getUsed() / (1024 * 1024),
                heapMemoryUsage.getMax() / (1024 * 1024));

        User owner = MC.getJDA().asBot().getApplicationInfo().complete().getOwner();

        List<Guild> guilds = MC.getJDA().getGuilds();

        int memberCount = guilds.stream()
                .map(guild -> guild.getMembers().size())
                .reduce(0, (a, b) -> a + b);

        MC.sendMessage(new EmbedBuilder().setColor(3447003)
                .setAuthor("LucoaBot " + version, bot.getAvatarUrl())
                .addField("Owner", owner.getName() + "#" + owner.getDiscriminator(), true)
                .addField("Uptime", uptimeStr, true)
                .addField("Bot ID", bot.getId(), true)
                .addField("Memory Usage", memoryStr, true)
                .addField("Servers", String.valueOf(guilds.size()), true)
                .addField("Users", String.valueOf(memberCount), true)
                .build());
    }
}
