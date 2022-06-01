package com.softwaii.capivara.core;

import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.exceptions.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.Map;

public class PackageManager {

    private Capivara capivara;

    public PackageManager() {
        this.capivara = Capivara.getInstance();
    }

    public void createPackage(String guildID, String packageName) throws PackageAlreadyExistsException, GuildNotFoundException {
        Guild guild = this.capivara.getOrCreateGuild(guildID);
        guild.addPackage(packageName);
    }

    public void destroyPackage(String guildID, String packageName) throws PackageDoesNotExistException, GuildNotFoundException {
        Guild guild = this.capivara.getGuild(guildID);
        guild.removePackage(packageName);
    }

    public void addRoleToPackage(String guildID, String packageName, String roleName, Role role) throws GuildNotFoundException, RoleAlreadyAddedException, PackageDoesNotExistException {
        Guild guild = this.capivara.getGuild(guildID);
        guild.addRole(packageName, roleName, role);
    }

    public void removeRoleFromPackage(String guildID, String packageName, String roleName) throws GuildNotFoundException, PackageDoesNotExistException, RoleNotFoundException {
        Guild guild = this.capivara.getGuild(guildID);
        guild.removeRole(packageName, roleName);
    }

    public MessageEmbed getGuildPackages(String guildID) throws GuildNotFoundException {
        Guild guild = this.capivara.getGuild(guildID);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Packages");
        builder.setDescription("List of packages in this guild");
        builder.setColor(Color.GREEN);

        for (String pkg_name : guild.getPackageNames()) {

            Map<String, Role> map = guild.getRoles(pkg_name);
            StringBuilder sb = new StringBuilder();

            map.forEach((key, value) -> {
                String line = key + ": " + value.getAsMention() + "\n";
                sb.append(line);
            });

            builder.addField(pkg_name, sb.toString(), false);
        }

        return builder.build();
    }
}
