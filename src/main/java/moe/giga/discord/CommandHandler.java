package moe.giga.discord;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.commands.Command;
import moe.giga.discord.contexts.MessageContext;
import moe.giga.discord.contexts.ServerContext;
import moe.giga.discord.contexts.UserContext;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandHandler {
    private final Map<String, Command> commandMap;

    CommandHandler(JDABuilder builder, List<Command> commands) {
        this.commandMap = new HashMap<>();
        for (Command command : commands) {
            CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
            command.init(builder);
            commandMap.putIfAbsent(info.name(), command);
            for (String alias : info.aliases()) {
                commandMap.putIfAbsent(alias, command);
            }

        }
    }

    private boolean containsCommand(ServerContext SC, Message message) {
        String command = commandArgs(message)[0];
        return command.startsWith(SC.getPrefix()) && commandMap.containsKey(command.substring(1));
    }

    private String[] commandArgs(Message message) {
        return commandArgs(message.getContentRaw());
    }

    private String[] commandArgs(String string) {
        return string.split(" ");
    }

    @SubscribeEvent
    public void handleMessage(MessageReceivedEvent event) {
        ServerContext SC = new ServerContext(event.getGuild());
        if (containsCommand(SC, event.getMessage())) {
            String[] args = commandArgs(event.getMessage());
            Command command = commandMap.get(args[0].substring(1));
            CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);

            if (!event.getAuthor().isBot() || info.allowBots()) {
                MessageContext MC = new MessageContext(event);
                MC.setUC(new UserContext(event.getAuthor()));
                MC.setSC(SC);

                args = Arrays.copyOfRange(args, 1, args.length);
                command.onCommand(MC, args);
            }
        }
    }
}
