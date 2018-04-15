package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.contexts.MessageContext;

@CommandInfo(name = "whoami", hidden = true)
public final class WhoAmICommand extends Command {

    @Override
    public void onCommand(MessageContext MC, String[] args) {
        MC.sendFormattedMessage("You are **%s**", MC.getUC().getHumanRole());
    }
}
