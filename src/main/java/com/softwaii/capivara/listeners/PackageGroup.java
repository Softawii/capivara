package com.softwaii.capivara.listeners;

import com.softawii.curupira.annotations.*;
import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.core.Capivara;
import com.softwaii.capivara.core.PackageManager;
import com.softwaii.capivara.exceptions.*;
import com.softwaii.capivara.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.awt.*;
import java.util.*;
import java.util.List;

@IGroup(name= "Getter Manager", description = "Group to manage roles")
public class PackageGroup {

    public static Capivara capivara = Capivara.getInstance();
    public static PackageManager packageManager = capivara.getPackageManager();
    public static Map<String, Guild> guilds = new HashMap<>();
    private static final String packageMenu   = "package-menu";
    private static final String packageButton = "package-button";
    private static final String roleMenu      = "role-menu";

    @ICommand(name = "package-create", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to add the role to", required = true, type= OptionType.STRING)
    public static void create(SlashCommandInteractionEvent event) {
        System.out.println("create");

        String name = event.getOption("name").getAsString();
        String guildId = event.getGuild().getId();

        try {
            packageManager.createPackage(guildId, name);
            event.reply("Package with name '" + name + "' created").queue();
        } catch (PackageAlreadyExistsException e) {
            event.reply("Package already exists").queue();
        } catch (GuildNotFoundException e) {
            event.reply("Guild not found").queue();
        }
    }

    @ICommand(name = "package-destroy", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to remove the role to", required = true, type= OptionType.STRING)
    public static void destroy(SlashCommandInteractionEvent event) {
        System.out.println("destroy");

        String guildId = event.getGuild().getId();
        String pkgName = event.getOption("name").getAsString();

        try {
            packageManager.destroyPackage(guildId, pkgName);
            event.reply("Package with name '" + pkgName + "' destroyed").queue();
        } catch (PackageDoesNotExistException e) {
            event.reply("Package does not exist").queue();
        } catch (GuildNotFoundException e) {
            event.reply("Guild not found").queue();
        }
    }

    @ICommand(name = "package-add", description = "Add a role to a package")
    @IArgument(name="package", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="role", description = "role to be added", required = true, type= OptionType.ROLE)
    public static void add(SlashCommandInteractionEvent event) {
        System.out.println("add");

        String guildId  = event.getGuild().getId();
        String pkgName  = event.getOption("package").getAsString();
        Role   role     = event.getOption("role").getAsRole();

        try {
            packageManager.addRoleToPackage(guildId, pkgName, role.getName(), role);
            event.reply("Role '" + role.getName() + "' added to package '" + pkgName + "'").queue();
        } catch (GuildNotFoundException e) {
            event.reply("Guild not found").queue();
        } catch (RoleAlreadyAddedException e) {
            event.reply("Role already added").queue();
        } catch (PackageDoesNotExistException e) {
            event.reply("Package does not exist").queue();
        }
    }

    @ICommand(name = "package-remove", description = "Remove a role from a package")
    @IArgument(name="package", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="role", description = "role to be added", required = true, type= OptionType.ROLE)
    public static void remove(SlashCommandInteractionEvent event) {
        System.out.println("remove");

        String guildId  = event.getGuild().getId();
        String pkgName  = event.getOption("package").getAsString();
        Role   role     = event.getOption("role").getAsRole();

        try {
            packageManager.removeRoleFromPackage(guildId, pkgName, role.getName());
            event.reply("Role '" + role.getName() + "' removed from package '" + pkgName + "'").queue();
        } catch (GuildNotFoundException e) {
            event.reply("Guild not found").queue();
        } catch (PackageDoesNotExistException e) {
            event.reply("Package does not exist").queue();
        } catch (RoleNotFoundException e) {
            event.reply("Role not found").queue();
        }
    }

    @ICommand(name="package-list", description = "List all packages")
    public static void list(SlashCommandInteractionEvent event) {
        System.out.println("list");

        String guildId = event.getGuild().getId();

        try {
            MessageEmbed embed = packageManager.getGuildPackages(guildId);
            event.replyEmbeds(embed).queue();
        } catch (GuildNotFoundException e) {
            event.reply("Guild not found").queue();
        }
    }

    @ICommand(name = "gen", description = "Generate a message with a button to packages")
    public static void newMessage(SlashCommandInteractionEvent event) {
        System.out.println("generate");
        String guild_id = event.getGuild().getId();
        if(guilds.containsKey(guild_id)) {
            Guild guild = guilds.get(guild_id);

            // Creating the message
            MessageEmbed embed = Utils.createEmbed("Packages", "Get your role", "008000");

            // Button
            Button button = Button.success(packageButton, "Get your role");

            event.replyEmbeds(embed).addActionRow(button).queue();
        }
        else {
            event.reply("No packages created").setEphemeral(true).queue();
        }
    }

    @IButton(id=packageButton)
    public static void package_getter(ButtonInteractionEvent event) {
        String guild_id = event.getGuild().getId();
        Guild guild = guilds.get(guild_id);

        // Package
        SelectMenu _packageMenu = Utils.createPackageMenu(guild.getPackageNames(), packageMenu);

        event.reply("Please select your package").addActionRow(_packageMenu).setEphemeral(true).queue();
    }

    @IMenu(id=packageMenu)
    public static void package_selector(SelectMenuInteractionEvent event) {
        System.out.println("package_selector");

        Optional<SelectOption> option = event.getInteraction().getSelectedOptions().stream().findFirst();

        option.ifPresentOrElse(opt -> {

                    String guild_id  = event.getGuild().getId();
                    Guild guild      = guilds.get(guild_id);

                    String _package  = opt.getValue();
                    String custom_id = roleMenu + ":" + _package;
                    SelectMenu menu  = Utils.createRoleMenu(event.getMember(), guild, _package, custom_id);

                    event.editMessage("Please select your role").setActionRow(menu).queue();
                }, () -> event.editMessage("No package selected").setActionRow().queue()
        );
    }

    @IMenu(id=roleMenu)
    public static void getting_role(SelectMenuInteractionEvent event) {
        String guild_id = event.getGuild().getId();
        Guild guild = guilds.get(guild_id);

        System.out.println(event.getInteraction().getId());

        String _package = event.getInteraction().getSelectMenu().getId().split(":")[1];
        Map<String, Role> roles = guild.getRoles(_package);

        List<SelectOption> selectOptions = event.getSelectedOptions();

        event.getInteraction().getSelectMenu().getOptions().forEach(opt -> {
            String key = opt.getValue();

            if(roles.containsKey(key)) {
                Role role = roles.get(key);

                if(selectOptions.contains(opt)) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                }
            }
        });

        event.reply("Your roles have been updated").setEphemeral(true).queue();
    }

}