package com.softwaii.laputa.listeners;

import com.softawii.curupira.annotations.*;
import com.softwaii.laputa.client.Guild;
import com.softwaii.laputa.exceptions.PackageAlreadyExistsException;
import com.softwaii.laputa.exceptions.RoleAlreadyAddedException;
import com.softwaii.laputa.utils.Utils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.*;

@IGroup(name= "Getter Manager", description = "Group to manage roles")
public class GetterManager {

    private static final String packageMenu   = "package-menu";
    private static final String packageButton = "package-button";
    private static final String roleMenu      = "role-menu";
    private static final String getRole       = "get-role";
    public static Map<String, Guild> guilds = new HashMap<>();

    @ICommand(name = "create", description = "Create a package to get roles")
    @IArgument(name="package", description = "The package to add the role to", required = true, type= OptionType.STRING)
    public static void create(SlashCommandInteractionEvent event) {
        System.out.println("create");
        // Never Null because It's a Guild Command
        String guild_id = event.getGuild().getId();
        // Never Null because is required
        String pkg_name = event.getOption("package").getAsString();

        Guild guild;
        // Creating a Guild if it doesn't exist
        if(!guilds.containsKey(guild_id)) {
            guild = new Guild();
            guilds.put(guild_id, guild);
        }
        // Just getting it
        else {
            guild = guilds.get(guild_id);
        }

        try {
            // Adding package
            guild.addPackage(pkg_name);
            event.reply("Package " + pkg_name + " created").queue();
        } catch (PackageAlreadyExistsException e) {
            event.reply("Package already exists").setEphemeral(true).queue();
        }
    }

    @ICommand(name = "add", description = "Add a role to a package")
    @IArgument(name="package", description = "The package to add the role to", required = true, type= OptionType.STRING)
    @IArgument(name="role", description = "role to be added", required = true, type= OptionType.ROLE)
    public static void add(SlashCommandInteractionEvent event) {
        System.out.println("add");

        // Never Null because It's a Guild Command
        String guild_id = event.getGuild().getId();
        // Never Null because is required
        String pkg_name = event.getOption("package").getAsString();
        Role role       = event.getOption("role").getAsRole();

        Guild guild;
        // Creating a Guild if it doesn't exist
        if(!guilds.containsKey(guild_id)) {
            guild = new Guild();
            guilds.put(guild_id, guild);
        }
        // Just getting it
        else {
            guild = guilds.get(guild_id);
        }
        // Adding role to package
        try {
            guild.addRole(pkg_name, role.getName(), role);
            event.reply("Role " + role.getName() + " added to package " + pkg_name).queue();
        } catch (RoleAlreadyAddedException e) {
            event.reply("Role already added").setEphemeral(true).queue();
        }
    }

    @ICommand(name = "generate", description = "Generate a message with a button to packages")
    public static void generate_message(SlashCommandInteractionEvent event) {
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

    public void remove(SlashCommandInteractionEvent event) {
        System.out.println("remove");
    }

    public void list(SlashCommandInteractionEvent event) {
        System.out.println("list");
    }

    public void status(SlashCommandInteractionEvent event) {
        System.out.println("status");
    }
}