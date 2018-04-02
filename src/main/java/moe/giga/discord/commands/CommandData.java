package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;

public final class CommandData {
    private final Command command;
    private final CommandInfo info;

    public boolean custom; // custom commands for servers will have this set to true

    public CommandData(Command command, CommandInfo info) {
        this.command = command;
        this.info = info;
    }

    public Command getCommand() {
        return command;
    }

    public CommandInfo getInfo() {
        return info;
    }
}
