package com.softawii.capivara.listeners;

import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.IButton;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

@IGroup(name = "role", description = "Role Group", hidden = false)
public class RoleGroup {

    // Specific Action
    public static final String actionButton = "role-action-button";
    public static final String removeAction = "role-remove-action";
    // Confirm or Deny Action
    public static final String confirmAction = "role-confirm-action";
    public static final String cancelAction = "role-cancel-action";

    @ICommand(name="create", description = "Create a role", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "type", description = "Role type", type = OptionType.STRING, required = true, choices = {
            @IArgument.IChoice(value="Channel", key="Channel"),
            @IArgument.IChoice(value="Category", key="Category"),
            @IArgument.IChoice(value="Free", key="Free")
    })
    @IArgument(name = "name", description = "Role name", type = OptionType.STRING, required = false)
    public static void create(SlashCommandInteractionEvent event) {
        event.getChannel().sendMessage("Role created").queue();
    }

    // DELETE SECTION -> DELETE / CONFIRM / CANCEL
    @ICommand(name="delete", description = "Delete a role", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="role", description = "Role", type = OptionType.ROLE, required = true)
    public static void delete(SlashCommandInteractionEvent event) {
        // Not null, because it's server side and required
        Role role = event.getOption("role").getAsRole();
        String confirmId = String.format("%s:%s:%s", confirmAction, removeAction,  role.getId());
        String cancelId  = cancelAction;

        MessageEmbed messageEmbed = Utils.simpleEmbed("Are you sure?",
                "You really want to delete the role " + role.getAsMention() + "??",
                Color.RED);

        Button successButton = Button.success(confirmId, "Yes");
        Button cancelButton  = Button.danger(cancelId, "No");

        event.replyEmbeds(messageEmbed).addActionRow(successButton, cancelButton).setEphemeral(true).queue();
    }

    @ICommand(name="permissions", description = "update", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="role", description = "Role", type = OptionType.ROLE, required = true)
    @IArgument(name="related", description = "Related", type = OptionType.CHANNEL, required = true)
    @IArgument(name="type", description = "Apply to Channel or Category", type = OptionType.STRING, required = true,
        choices = {
            @IArgument.IChoice(value="Channel", key="Channel"),
            @IArgument.IChoice(value="Category", key="Category")
    })
    @IArgument(name="permissions", description = "Permissions", type = OptionType.STRING, required = true,
        choices = {
            @IArgument.IChoice(value="Read", key="Read"),
            @IArgument.IChoice(value="Write", key="Write")
    })
    public static void permissions(SlashCommandInteractionEvent event) {
        event.getChannel().sendMessage("Role created").queue();
    }


    @IButton(id=confirmAction)
    public static void confirm(ButtonInteractionEvent event) {
        System.out.println("Received confirm: " + event.getComponentId());

        /*
        Expected Args:
        - buttonId
        - actionId
        - roleId
         */
        String[] args = event.getComponentId().split(":");

        if(args.length != 3) {
            event.reply("Invalid arguments -> " + event.getComponentId()).setEphemeral(true).queue();
            return;
        }

        String actionId = args[1];
        String roleId   = args[2];

        if(actionId.equals(removeAction)) {
            // Guild never null because it's a button in a server
            Role role = event.getGuild().getRoleById(roleId);

            if(role == null) {
                event.reply("Role not found").setEphemeral(true).queue();
                return;
            }

            MessageEmbed embed = Utils.simpleEmbed("Role deleted",
                                            "Role '" + role.getName() + "' deleted", Color.RED);
            event.editMessageEmbeds(embed).setActionRows().queue();
            role.delete().queue();
        }
    }

    @IButton(id=cancelAction)
    public static void cancel(ButtonInteractionEvent event) {
        event.getMessage().delete().queue();
        event.reply("Cancelled").setEphemeral(true).queue();
    }

}

