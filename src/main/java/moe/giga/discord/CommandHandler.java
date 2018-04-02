package moe.giga.discord;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.commands.CommandData;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandHandler extends ListenerAdapter {
    private final List<CommandData> commands;
    private final Map<String, CommandData> commandMap;

    private static final String PREFIX = "~";

    public CommandHandler(List<CommandData> commands) {
        this.commands = commands;
        this.commandMap = new HashMap<>();
        for (CommandData commandData : commands) {
            CommandInfo info = commandData.getInfo();
            commandMap.putIfAbsent(info.name(), commandData);
            for (String alias : info.aliases()) {
                commandMap.putIfAbsent(alias, commandData);
            }
        }
    }

    private boolean containsCommand(Message message) {
        String command = commandArgs(message)[0];
        return command.startsWith(PREFIX) && commandMap.containsKey(command.substring(1));
    }

    private String[] commandArgs(Message message) {
        return commandArgs(message.getContentDisplay());
    }

    private String[] commandArgs(String string) {
        return  string.split(" ");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (containsCommand(event.getMessage())) {
            String[] args = commandArgs(event.getMessage());
            CommandData commandData = commandMap.get(args[0].substring(1));

            if (!event.getAuthor().isBot() || commandData.getInfo().allowBots()) {
                commandData.getCommand().onCommand(event, args);
            }
        }
    }
}
