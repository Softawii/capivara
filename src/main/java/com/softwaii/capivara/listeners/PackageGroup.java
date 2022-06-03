package com.softwaii.capivara.listeners;

import com.softawii.curupira.annotations.*;
import com.softwaii.capivara.core.PackageManager;
import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.exceptions.RoleDoesNotExistException;
import com.softwaii.capivara.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@IGroup(name= "Getter Manager", description = "Group to manage roles")
public class PackageGroup {

    public static PackageManager packageManager;
    private static final String packageMenu   = "package-menu";
    private static final String packageButton = "package-button";
    private static final String roleMenu      = "role-menu";

    @ICommand(name = "package-create", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="unique", description = "If the package is unique or not", required = false, type= OptionType.BOOLEAN)
    @IArgument(name="description", description = "The package description", required = false, type= OptionType.STRING)
    public static void create(SlashCommandInteractionEvent event) {
        System.out.println("create");

        String name    = event.getOption("name").getAsString();
        boolean unique  = event.getOption("unique") != null && event.getOption("unique").getAsBoolean();
        Long guildId = event.getGuild().getIdLong();
        String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";

        try {
            packageManager.create(guildId, name, unique, description);
            event.reply("Package with name '" + name + "' created").queue();
        } catch (PackageAlreadyExistsException e) {
            event.reply("Package already exists").queue();
        }
    }

    @ICommand(name = "package-destroy", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to remove the role to", required = true, type= OptionType.STRING)
    public static void destroy(SlashCommandInteractionEvent event) {
        System.out.println("destroy");

        Long guildId = event.getGuild().getIdLong();
        String name = event.getOption("name").getAsString();

        try {
            packageManager.destroy(guildId, name);
            event.reply("Package with name '" + name + "' destroyed").queue();
        } catch (PackageDoesNotExistException e) {
            event.reply("Package does not exist").queue();
        }
    }

    @ICommand(name = "package-add", description = "Add a role to a package")
    @IArgument(name="package", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="role", description = "role to be added", required = true, type= OptionType.ROLE)
    @IArgument(name="name", description = "The name to link to the role", required = false, type= OptionType.STRING)
    @IArgument(name="description", description = "The description to link to the role", required = false, type= OptionType.STRING)
    public static void add(SlashCommandInteractionEvent event) {
        Long   guildId = event.getGuild().getIdLong();
        String packageName = event.getOption("package").getAsString();
        Role   role = event.getOption("role").getAsRole();
        String name = event.getOption("name") != null ? event.getOption("name").getAsString() : role.getName();
        String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";

        try {
            packageManager.addRole(guildId, packageName, role, name, description);
            event.reply("Role '" + role.getName() + "' added to package '" + packageName + "'").queue();
        } catch (PackageDoesNotExistException e) {
            event.reply("Package does not exist").queue();
        } catch (RoleAlreadyAddedException e) {
            event.reply("Role already added to package").queue();
        }
    }

    @ICommand(name = "package-remove", description = "Remove a role from a package")
    @IArgument(name="package", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="name", description = "role link to remove", required = true, type= OptionType.STRING)
    public static void remove(SlashCommandInteractionEvent event)  {
        Long   guildId = event.getGuild().getIdLong();
        String packageName = event.getOption("package").getAsString();
        String roleName = event.getOption("name").getAsString();

        try {
            packageManager.removeRole(guildId, packageName, roleName);
            event.reply(String.format("Role '%s' removed from '%s'", roleName, packageName)).queue();
        } catch (RoleDoesNotExistException e) {
            event.reply(String.format("Role '%s' does not exists in '%s'", roleName, packageName)).queue();
        }
    }

    @ICommand(name="package-list", description = "List all packages")
    public static void list(SlashCommandInteractionEvent event) {
        Long guildId = event.getGuild().getIdLong();
        List<Role> roles = event.getGuild().getRoles();
        MessageEmbed guildPackages = packageManager.getGuildPackages(guildId, roles);
        event.replyEmbeds(guildPackages).queue();
    }

    @ICommand(name = "package-message", description = "Generate a message with a button to packages")
    @IArgument(name="title", description = "Title of the message", required = true, type= OptionType.STRING)
    @IArgument(name="description", description = "Description of the message", required = true, type= OptionType.STRING)
    @IArgument(name="button-text", description = "Text of the button", required = true, type= OptionType.STRING)
    @IRange(value=@IArgument(name="package", description = "The package", required = false, type= OptionType.STRING), min = 0, max = 20)
    public static void message(SlashCommandInteractionEvent event) {
        System.out.println("message");
        String guildId        = event.getGuild().getId();
        String title          = event.getOption("title").getAsString();
        String description    = event.getOption("description").getAsString();
        String buttonText     = event.getOption("button-text").getAsString();
        List<String> packages = new ArrayList<>();

        for(int i = 0; i <= 25; i++) {
            String packageName = event.getOption("package" + i) != null ? event.getOption("package" + i).getAsString() : null;
            if(packageName != null) packages.add(packageName);
        }

        // TODO: Verify if package exists
        MessageEmbed messageEmbed = Utils.simpleEmbed(title, description, Color.GREEN);
        // TODO: Button Name if packages is different from null (or empty)
        // Like package:package1:package2:package3...
        Button button = Button.success(packageButton, buttonText);
        event.replyEmbeds(messageEmbed).addActionRow(button).queue();
    }

    @IButton(id=packageButton)
    public static void packageGetter(ButtonInteractionEvent event) {
        System.out.println("packageGetter");

        Long guildId = event.getGuild().getIdLong();
        // TODO: get packages from event
        //String[] packages = event.getInteraction().getId().split(":");

        SelectMenu menu = packageManager.getGuildPackagesMenu(guildId, null, packageMenu);

        event.reply("Please select a package").addActionRow(menu).setEphemeral(true).queue();
    }

    @IMenu(id=packageMenu)
    public static void package_selector(SelectMenuInteractionEvent event) {
        System.out.println("package_selector");

        // The selected option
        Optional<SelectOption> option = event.getInteraction().getSelectedOptions().stream().findFirst();

        option.ifPresentOrElse(opt -> {
                String _package  = opt.getValue();
                String customId = roleMenu + ":" + _package;
                Long guildId   = event.getGuild().getIdLong();

                List<Long> roleIds = event.getGuild().getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());
                    SelectMenu menu = null;
                    try {
                        menu = packageManager.getGuildPackageRoleMenu(guildId, _package, customId, event.getMember(), roleIds);
                    } catch (PackageDoesNotExistException e) {
                        event.reply("Package does not exist").queue();
                        return;
                    }

                    event.editMessage("Please select a role").setActionRow(menu).queue();

            }, () -> event.editMessage("No package selected").setActionRow().queue()
        );
    }

    @IMenu(id=roleMenu)
    public static void getting_role(SelectMenuInteractionEvent event) {
        String guildId = event.getGuild().getId();

        System.out.println(event.getInteraction().getId());

        List<SelectOption> selectOptions = event.getSelectedOptions();

        event.getInteraction().getSelectMenu().getOptions().forEach(opt -> {
            // roleId
            Long roleId = Long.valueOf(opt.getValue());

            Role role = event.getGuild().getRoleById(roleId);

            if(role == null) {
                event.editMessage("Role " + opt.getValue() + " does not exist").queue();
                return;
            }

            try {
                if (selectOptions.contains(opt)) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                }
            } catch(HierarchyException e) {
                event.editMessage("Bot cannot assignee the role " + opt.getValue() + " to you").queue();
            }
        });

        event.reply("Your roles have been updated").setEphemeral(true).queue();
    }
}