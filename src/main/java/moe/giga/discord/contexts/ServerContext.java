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

    public Guild getGuild() {
        return guild;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) throws IllegalArgumentException {
        if (prefix == null || prefix.length() == 0) {
            throw new IllegalArgumentException("Prefix was invalid!");
        }

        try (Connection c = LucoaBot.getConnection()) {
            PreparedStatement pstmt = c.prepareStatement("UPDATE servers SET prefix = ? WHERE server_id = ?");
            pstmt.setString(1, prefix);
            pstmt.setString(2, guild.getId());

            pstmt.executeUpdate();

            this.prefix = prefix;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getStarChannel() {
        return starChannel;
    }

    public void setStarChannel(String starChannel) {
        try (Connection c = LucoaBot.getConnection()) {
            PreparedStatement pstmt = c.prepareStatement("UPDATE servers SET star_channel = ? WHERE server_id = ?");
            pstmt.setString(1, starChannel);
            pstmt.setString(2, guild.getId());

            pstmt.executeUpdate();

            this.starChannel = starChannel;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLogChannel() {
        return logChannel;
    }

    public void setLogChannel(String logChannel) {
        try (Connection c = LucoaBot.getConnection()) {
            PreparedStatement pstmt = c.prepareStatement("UPDATE servers SET log_channel = ? WHERE server_id = ?");
            pstmt.setString(1, logChannel);
            pstmt.setString(2, guild.getId());

            pstmt.executeUpdate();

            this.logChannel = logChannel;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
