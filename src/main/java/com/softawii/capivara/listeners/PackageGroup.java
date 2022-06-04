package com.softawii.capivara.listeners;

import com.softawii.curupira.annotations.*;
import com.softawii.capivara.core.PackageManager;
import com.softawii.capivara.exceptions.*;
import com.softawii.capivara.utils.Utils;
import kotlin.Pair;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
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

@IGroup(name= "Package", description = "Group to manage roles", hidden = false)
public class PackageGroup {

    public static PackageManager packageManager;
    private static final String packageMenu       = "package-menu";
    private static final String packageUniqueMenu = "package-unique-menu";
    private static final String packageButton     = "package-button";
    private static final String roleMenu          = "role-menu";

    @ICommand(name = "create", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to be created", required = true, type= OptionType.STRING)
    @IArgument(name="unique", description = "If the package is unique or not", required = false, type= OptionType.BOOLEAN)
    @IArgument(name="description", description = "The package description", required = false, type= OptionType.STRING)
    @IArgument(name="emoji", description = "The package emoji", required = false, type= OptionType.STRING)
    public static void create(SlashCommandInteractionEvent event) {
        System.out.println("create");

        String name    = event.getOption("name").getAsString();
        boolean unique  = event.getOption("unique") != null && event.getOption("unique").getAsBoolean();
        Long guildId = event.getGuild().getIdLong();
        String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";
        String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : "";
        boolean isUnicode = false;

        try {
            Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
            emojiString = emoji.getFirst();
            isUnicode = emoji.getSecond();
        } catch (MultipleEmojiMessageException e) {
            event.reply("Provided emoji is not a single emoji").queue();
        }

        try {
            packageManager.create(guildId, name, unique, description, emojiString, isUnicode);
            event.reply("Package with name '" + name + "' created").queue();
        } catch (PackageAlreadyExistsException e) {
            event.reply("Package already exists").queue();
        }
    }

    @ICommand(name = "edit", description = "Update a package", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="name", description = "The package to update", required = true, type= OptionType.STRING)
    @IArgument(name="unique", description = "If the package is unique or not", required = false, type= OptionType.BOOLEAN)
    @IArgument(name="description", description = "The package description", required = false, type= OptionType.STRING)
    @IArgument(name="emoji", description = "The package emoji", required = false, type= OptionType.STRING)
    public static void update(SlashCommandInteractionEvent event) {
        System.out.println("update");

        String name    = event.getOption("name").getAsString();
        Long guildId = event.getGuild().getIdLong();


        Boolean unique  = event.getOption("unique") != null ? event.getOption("unique").getAsBoolean() : null;
        String description = event.getOption("description") != null ? event.getOption("description").getAsString() : null;
        String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : null;
        boolean isUnicode = false;

        if(emojiString != null) {
            try {
                Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
                emojiString = emoji.getFirst();
                isUnicode = emoji.getSecond();
            } catch (MultipleEmojiMessageException e) {
                event.reply("Provided emoji is not a single emoji").queue();
            }
        }

        try {
            packageManager.update(guildId, name, unique, description, emojiString, isUnicode);
            event.reply("Package with name '" + name + "' created").queue();
        } catch (PackageDoesNotExistException e) {
            event.reply("Package '" + name +"' does not exist").setEphemeral(true).queue();
        }
    }

    @ICommand(name = "destroy", description = "Create a package to get roles", permissions = {Permission.ADMINISTRATOR})
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


    @ISubGroup(name = "role", description = "role subgroup")
    public static class RoleGroup {

        @ICommand(name = "add", description = "Add a role to a package", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "package", description = "The package to add the role to", required = true, type = OptionType.STRING)
        @IArgument(name = "role", description = "role to be added", required = true, type = OptionType.ROLE)
        @IArgument(name = "name", description = "The name to link to the role", required = false, type = OptionType.STRING)
        @IArgument(name = "description", description = "The description to link to the role", required = false, type = OptionType.STRING)
        @IArgument(name = "emoji", description = "The emoji to link to the role", required = false, type = OptionType.STRING)
        public static void add(SlashCommandInteractionEvent event) {
            Long guildId = event.getGuild().getIdLong();
            String packageName = event.getOption("package").getAsString();
            Role role = event.getOption("role").getAsRole();
            String name = event.getOption("name") != null ? event.getOption("name").getAsString() : role.getName();
            String description = event.getOption("description") != null ? event.getOption("description").getAsString() : "";
            String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : "";
            boolean isUnicode = false;

            try {
                Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
                emojiString = emoji.getFirst();
                isUnicode = emoji.getSecond();
            } catch (MultipleEmojiMessageException e) {
                event.reply("Provided emoji is not a single emoji").queue();
            }

            try {
                packageManager.addRole(guildId, packageName, role, name, description, emojiString, isUnicode);
                event.reply("Role '" + role.getName() + "' added to package '" + packageName + "'").queue();
            } catch (PackageDoesNotExistException e) {
                event.reply("Package does not exist").queue();
            } catch (RoleAlreadyAddedException e) {
                event.reply("Role already added to package").queue();
            } catch (KeyAlreadyInPackageException e) {
                event.reply("Key '" + name + "' already linked to package").queue();
            }
        }

        @ICommand(name = "edit", description = "Edit a role in a package", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "package", description = "The package to edit", required = true, type = OptionType.STRING)
        @IArgument(name = "name", description = "The name to link to the role", required = true, type = OptionType.STRING)
        @IArgument(name = "role", description = "The role to be edited", required = false, type = OptionType.ROLE)
        @IArgument(name = "description", description = "The description to link to the role", required = false, type = OptionType.STRING)
        @IArgument(name = "emoji", description = "The emoji to link to the role", required = false, type = OptionType.STRING)
        public static void edit(SlashCommandInteractionEvent event) {
            // Primary Key
            Long guildId = event.getGuild().getIdLong();
            String packageName = event.getOption("package").getAsString();
            String name = event.getOption("name").getAsString();

            // Attributes
            Role role = event.getOption("role") != null ? event.getOption("role").getAsRole() : null;
            String description = event.getOption("description") != null ? event.getOption("description").getAsString() : null;
            String emojiString = event.getOption("emoji") != null ? event.getOption("emoji").getAsString() : null;
            boolean isUnicode = false;

            if (emojiString != null) {
                try {
                    Pair<String, Boolean> emoji = Utils.getEmoji(emojiString);
                    emojiString = emoji.getFirst();
                    isUnicode = emoji.getSecond();
                } catch (MultipleEmojiMessageException e) {
                    event.reply("Provided emoji is not a single emoji").queue();
                }
            }

            try {
                packageManager.editRole(guildId, packageName, name, role, description, emojiString, isUnicode);
                event.reply("Role '" + role.getName() + "' added to package '" + packageName + "'").queue();
            } catch (PackageDoesNotExistException e) {
                event.reply("Package does not exist").queue();
            } catch (RoleAlreadyAddedException e) {
                event.reply("Role already added to package").queue();
            } catch (RoleDoesNotExistException e) {
                event.reply("Role does not exist").queue();
            }
        }

        @ICommand(name = "remove", description = "Remove a role from a package", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "package", description = "The package to add the role to", required = true, type = OptionType.STRING)
        @IArgument(name = "name", description = "role link to remove", required = true, type = OptionType.STRING)
        public static void remove(SlashCommandInteractionEvent event) {
            Long guildId = event.getGuild().getIdLong();
            String packageName = event.getOption("package").getAsString();
            String roleName = event.getOption("name").getAsString();

            try {
                packageManager.removeRole(guildId, packageName, roleName);
                event.reply(String.format("Role '%s' removed from '%s'", roleName, packageName)).queue();
            } catch (RoleDoesNotExistException | RoleNotFoundException e) {
                event.reply(String.format("Role '%s' does not exists in '%s'", roleName, packageName)).queue();
            } catch (PackageDoesNotExistException e) {
                event.reply("Package does not exist").queue();
            }
        }

    }
    @ICommand(name = "list", description = "List all packages")
    public static void list(SlashCommandInteractionEvent event) {
        boolean showError =  event.getMember().hasPermission(Permission.ADMINISTRATOR);

        Long guildId = event.getGuild().getIdLong();
        List<Role> roles = event.getGuild().getRoles();
        MessageEmbed guildPackages = packageManager.getGuildPackages(guildId, roles, showError);
        event.replyEmbeds(guildPackages).queue();
    }

    @ICommand(name = "message", description = "Generate a message with a button to packages")
    @IArgument(name="title", description = "Title of the message", required = true, type= OptionType.STRING)
    @IArgument(name="description", description = "Description of the message", required = true, type= OptionType.STRING)
    @IArgument(name="button-text", description = "Text of the button", required = true, type= OptionType.STRING)
    @IArgument(name="type", description = "Get Multiple or an Package", required = true, type= OptionType.STRING,
            choices={@IArgument.IChoice(key="Packages", value="packages"), @IArgument.IChoice(key="Unique", value="Unique")})
    @IRange(value=@IArgument(name="package", description = "The package", required = false, type= OptionType.STRING), min = 0, max = 20)
    public static void message(SlashCommandInteractionEvent event) {
        System.out.println("message");
        Long guildId        = event.getGuild().getIdLong();
        String title          = event.getOption("title").getAsString();
        String description    = event.getOption("description").getAsString();
        String buttonText     = event.getOption("button-text").getAsString();
        List<String> packages = new ArrayList<>();

        for(int i = 0; i <= 20; i++) {
            String packageName = event.getOption("package" + i) != null ? event.getOption("package" + i).getAsString() : null;
            if(packageName != null) packages.add(packageName);
        }

        String type = event.getOption("type").getAsString();

        // Get Multiple or All Packages
        if(type.equals("packages")) {
            if(!packages.isEmpty() && !packageManager.checkIfAllPackagesExist(guildId, packages)) {
                event.reply("One or more packages does not exist").queue();
                return;
            }
            MessageEmbed messageEmbed = Utils.simpleEmbed(title, description, Color.GREEN);

            String buttonId = packageButton;
            if(!packages.isEmpty()) {
                String packagesString = String.join(":", packages);
                buttonId += ":" + packagesString;
            }

            Button button = Button.success(buttonId, buttonText);
            event.replyEmbeds(messageEmbed).addActionRow(button).queue();
        } else {
            if(packages.size() != 1) {
                event.reply("You need to specify exactly one package").queue();
                return;
            }

            if(!packageManager.checkIfPackageExists(guildId, packages.get(0))) {
                event.reply("The package does not exist").queue();
                return;
            }
            MessageEmbed messageEmbed = Utils.simpleEmbed(title, description, Color.GREEN);

            String packageName = packages.get(0);
            String buttonId = packageUniqueMenu + ":" + packageName;

            Button button = Button.success(buttonId, buttonText);
            event.replyEmbeds(messageEmbed).addActionRow(button).queue();
        }
    }

    @IButton(id=packageButton)
    public static void packageGetter(ButtonInteractionEvent event) {
        System.out.println("packageGetter " + event.getComponentId());

        Long guildId = event.getGuild().getIdLong();

        String[] packages = event.getComponentId().split(":");
        packages = Arrays.copyOfRange(packages, 1, packages.length);

        List<Emote> emotes = event.getGuild().getEmotes();

        SelectMenu menu = packageManager.getGuildPackagesMenu(guildId, Arrays.stream(packages).toList(), packageMenu, emotes);

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
                    List<Emote> emotes = event.getGuild().getEmotes();
                    menu = packageManager.getGuildPackageRoleMenu(guildId, _package, customId, event.getMember(), roleIds, emotes);
                } catch (PackageDoesNotExistException e) {
                    event.editMessage("Package does not exist").setActionRow().queue();
                    return;
                }

                event.reply("Please select a role").addActionRow(menu).setEphemeral(true).queue();

            }, () -> event.editMessage("No package selected").queue()
        );
    }

    @IButton(id=packageUniqueMenu)
    public static void packageUniqueMenu(ButtonInteractionEvent event) {
        System.out.println("packageUniqueMenu " + event.getComponentId());

        Long guildId = event.getGuild().getIdLong();
        String _package = event.getComponentId().split(":")[1];
        String customId = roleMenu + ":" + _package;

        List<Long> roleIds = event.getGuild().getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());
        SelectMenu menu = null;
        try {
            List<Emote> emotes = event.getGuild().getEmotes();
            menu = packageManager.getGuildPackageRoleMenu(guildId, _package, customId, event.getMember(), roleIds, emotes);
        } catch (PackageDoesNotExistException e) {
            event.editMessage("Package does not exist").setActionRow().queue();
            return;
        }

        event.reply("Please select a role").addActionRow(menu).setEphemeral(true).queue();
    }

    @IMenu(id=roleMenu)
    public static void getting_role(SelectMenuInteractionEvent event) {
        System.out.println(event.getInteraction().getId());

        List<SelectOption> selectOptions = event.getSelectedOptions();

        event.getInteraction().getSelectMenu().getOptions().forEach(opt -> {
            // roleId
            Long roleId = Long.valueOf(opt.getValue());

            Role role = event.getGuild().getRoleById(roleId);

            if(role == null) {
                event.editMessage(event.getMessage().getContentDisplay() + "\nRole " + opt.getLabel() +  " : " +  opt.getValue() + " does not exist").queue();
                return;
            }

            try {
                if (selectOptions.contains(opt)) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                }
            } catch(HierarchyException e) {
                event.getChannel().sendMessage("Bot cannot assignee the role " + opt.getValue() + " to you").queue();
            }
        });

        event.reply("Your roles have been updated").setEphemeral(true).queue();
    }
}