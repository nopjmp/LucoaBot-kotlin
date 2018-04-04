package moe.giga.discord.contexts;

import moe.giga.discord.LucoaBot;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServerContext {
    private Guild guild;

    private String prefix;
    private String starChannel;
    private String logChannel;

    // log events structure?

    public ServerContext(Guild guild) {
        this.guild = guild;

        attachData();
    }

    private void attachData() {
        try (Connection c = LucoaBot.getConnection()) {
            PreparedStatement pstmt = c.prepareStatement("SELECT * FROM servers WHERE server_id = ?");
            pstmt.setString(1, guild.getId());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.prefix = rs.getString("prefix");
                this.logChannel = rs.getString("log_channel");
                this.starChannel = rs.getString("star_channel");
            } else {
                PreparedStatement cstmt = c.prepareStatement("insert into servers (server_id, prefix, log_channel, star_channel) values (?, \".\", null, null)");
                cstmt.setString(1, guild.getId());

                cstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Guild getGuild() {
        return guild;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getStarChannel() {
        return starChannel;
    }

    public String getLogChannel() {
        return logChannel;
    }

    Map<String, String> getServerRoles() {
        try (Connection c = LucoaBot.getConnection()) {
            PreparedStatement pstmt = c.prepareStatement("SELECT * FROM servers_roles WHERE server_id = ?");
            pstmt.setString(1, guild.getId());

            ResultSet results = pstmt.executeQuery();
            Map<String, String> roles = new HashMap<>();
            while (results.next()) {
                roles.put(results.getString("role_spec"), results.getString("role_id"));
            }

            return roles;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    Map<String, List<String>> getServerSelfRoles(String id) {
        try (Connection c = LucoaBot.getConnection()) {
            PreparedStatement pstmt = c.prepareStatement("SELECT * FROM servers_self_roles WHERE server_id = ?");
            pstmt.setString(1, guild.getId());

            ResultSet results = pstmt.executeQuery();
            Map<String, List<String>> roles = new HashMap<>();
            while (results.next()) {
                String role_spec = results.getString("role_spec");
                String role_id = results.getString("role_id");

                roles.putIfAbsent(role_spec, new ArrayList<>());
                roles.get(role_spec).add(role_id);
            }

            return roles;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
