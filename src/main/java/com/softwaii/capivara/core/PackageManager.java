package com.softwaii.capivara.core;

import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.exceptions.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackageManager {

    private Capivara capivara;

    public PackageManager() {
        this.capivara = Capivara.getInstance();
    }

    public void createPackage(String guildID, String packageName, boolean unique) throws PackageAlreadyExistsException {
        Guild guild = this.capivara.getOrCreateGuild(guildID);
        guild.addPackage(packageName, unique);
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

    public MessageEmbed packageGetRoleMessage(String guildID, List<String> packages, String title, String description) throws GuildNotFoundException, PackageDoesNotExistException {

        Guild guild = this.capivara.getGuild(guildID);

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(title);
        builder.setDescription(description);

        if(!packages.isEmpty()) {

            for (String pkg_name : packages) {
                if(!guild.getPackageNames().contains(pkg_name)) throw new PackageDoesNotExistException("Package " + pkg_name + " does not exist in guild " + guildID);
            }

            builder.addField("Packages", String.join("\n ", packages), false);
        }

        return builder.build();
    }

    public SelectMenu createPackageMenu(String guildID, String id, List<String> packages) throws GuildNotFoundException {
        SelectMenu.Builder builder = SelectMenu.create(id)
                .setPlaceholder("Select your package")
                .setRequiredRange(1, 1);

        if(packages.isEmpty()) {
            packages = this.capivara.getGuild(guildID).getPackageNames();
        }

        List<SelectOption> optionList = new ArrayList<>();

        for(String pkg : packages) {
            builder.addOption(pkg, pkg);
        }

        return builder.build();
    }

    public SelectMenu getSelectMenu(String guildId, Member member, String pkg_name, String id) throws GuildNotFoundException {
        Guild guild = this.capivara.getGuild(guildId);
        return guild.createRoleMenu(member, pkg_name, id);
    }

    public Map<String, Role> getRoles(String guildID, String packageName) throws GuildNotFoundException {
        Guild guild = this.capivara.getGuild(guildID);
        return guild.getRoles(packageName);
    }
}
