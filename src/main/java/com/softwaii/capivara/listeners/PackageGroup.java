package com.softwaii.capivara.listeners;

import com.softawii.curupira.annotations.*;
import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.core.Capivara;
import com.softwaii.capivara.core.PackageManager;
import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.exceptions.RoleDoesNotExistException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.*;
import java.util.stream.Collectors;

@IGroup(name= "Getter Manager", description = "Group to manage roles")
public class PackageGroup {

    public static Capivara capivara;
    public static PackageManager packageManager;
    public static Map<String, Guild> guilds = new HashMap<>();
    private static final String packageMenu   = "package-menu";
    private static final String packageButton = "package-button";
    private static final String roleMenu      = "role-menu";

    @ICommand(name = "package-create", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="unique", description = "If the package is unique or not", required = false, type= OptionType.BOOLEAN)
    public static void create(SlashCommandInteractionEvent event) {
        System.out.println("create");

        String name    = event.getOption("name").getAsString();
        boolean unique  = event.getOption("unique") != null && event.getOption("unique").getAsBoolean();
        Long guildId = event.getGuild().getIdLong();

        try {
            packageManager.create(guildId, name, unique);
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

//    @ICommand(name = "package-message", description = "Generate a message with a button to packages")
    @IArgument(name="title", description = "Title of the message", required = true, type= OptionType.STRING)
    @IArgument(name="description", description = "Description of the message", required = true, type= OptionType.STRING)
    @IRange(value=@IArgument(name="package", description = "The package", required = false, type= OptionType.STRING), min = 0, max = 22)
    public static void message(SlashCommandInteractionEvent event) {
        System.out.println("message");
        String guildId        = event.getGuild().getId();
        String title          = event.getOption("title").getAsString();
        String description    = event.getOption("description").getAsString();
        List<String> packages = new ArrayList<>();

        for(int i = 0; i <= 25; i++) {
            String packageName = event.getOption("package" + i) != null ? event.getOption("package" + i).getAsString() : null;
            if(packageName != null) packages.add(packageName);
        }

//        try {
////            MessageEmbed embed = packageManager.packageGetRoleMessage(guildId, packages, title, description);
////            Button button = Button.success(packageButton + ":" + String.join(":", packages), "Click to get roles");
////            event.replyEmbeds(embed).addActionRow(button).queue();
//        } catch (GuildNotFoundException e) {
//            event.reply("Guild not found").queue();
//        } catch (PackageDoesNotExistException e) {
//            event.reply("Package does not exist").queue();
//        }
    }

    @IButton(id=packageButton)
    public static void packageGetter(ButtonInteractionEvent event) {
        System.out.println("packageGetter");

        String guildId = event.getGuild().getId();
        String[] packages = event.getInteraction().getId().split(":");

        List<String> packageList;
        if(packages.length > 1) {
            packages = Arrays.copyOfRange(packages, 1, packages.length);
            packageList = Arrays.stream(packages).collect(Collectors.toList());
        } else {
            packageList = new ArrayList<>();
        }

//        try {
//            SelectMenu menu = packageManager.createPackageMenu(guildId, packageMenu, packageList);
//            event.reply("Please select a package").addActionRow(menu).queue();
//        } catch (GuildNotFoundException e) {
//            event.reply("Guild not found").queue();
//        }
    }

    @IMenu(id=packageMenu)
    public static void package_selector(SelectMenuInteractionEvent event) {
        System.out.println("package_selector");

        Optional<SelectOption> option = event.getInteraction().getSelectedOptions().stream().findFirst();

        option.ifPresentOrElse(opt -> {
                String _package  = opt.getValue();
                String custom_id = roleMenu + ":" + _package;
                String guildId   = event.getGuild().getId();

//            try {
//                SelectMenu menu = packageManager.getSelectMenu(guildId, event.getMember(), _package, custom_id);
//                event.editMessage("Please select your role").setActionRow(menu).queue();
//            } catch (GuildNotFoundException e) {
//                event.reply("Guild not found").queue();
//            }
            }, () -> event.editMessage("No package selected").setActionRow().queue()
        );
    }

    @IMenu(id=roleMenu)
    public static void getting_role(SelectMenuInteractionEvent event) {
        String guildId = event.getGuild().getId();

        System.out.println(event.getInteraction().getId());

//        String _package = event.getInteraction().getSelectMenu().getId().split(":")[1];
//        Map<String, Role> roles;
////        try {
////            roles = packageManager.getRoles(guildId, _package);
////        } catch (GuildNotFoundException e) {
////            event.reply("Guild not found").queue();
////            return;
////        }
//
//        List<SelectOption> selectOptions = event.getSelectedOptions();
//
//        event.getInteraction().getSelectMenu().getOptions().forEach(opt -> {
//            String key = opt.getValue();
//
//
//            if(roles.containsKey(key)) {
//                Role role = roles.get(key);
//
//                if(selectOptions.contains(opt)) {
//                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
//                } else {
//                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
//                }
//            }
//        });

        event.reply("Your roles have been updated").setEphemeral(true).queue();
    }
}