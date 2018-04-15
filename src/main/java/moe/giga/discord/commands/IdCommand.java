package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.contexts.MessageContext;

@CommandInfo(name="id", hidden = true)
public final class IdCommand extends Command {
    @Override
    public void onCommand(MessageContext MC, String[] args) {
        MC.sendFormattedMessage("ID: %s", MC.getUC().getUser().getId());
    }
}
