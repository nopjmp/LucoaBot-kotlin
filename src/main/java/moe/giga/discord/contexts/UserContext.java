package moe.giga.discord.contexts;

import moe.giga.discord.permissions.AccessLevel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public final class UserContext {
    private User user;
    private AccessLevel permissions;

    private ServerContext serverContext;
    private Member member;


    public UserContext(User user) {
        this.user = user;
        this.permissions = AccessLevel.USER;

        this.serverContext = null;
        this.member = null;
    }

    private void resolvePermissions() {
        if (this.serverContext != null && (this.user == this.serverContext.getGuild().getOwner().getUser())) {
            this.permissions = AccessLevel.ADMIN;
        }
    }

    public Member getMember() {
        return member;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public AccessLevel getPermissions() {
        return permissions;
    }

    public User getUser() {
        return user;
    }

    public String getHumanRole() {
        return this.permissions.toString();
    }
}
