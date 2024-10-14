package com.softawii.capivara.controller;

import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.interactions.DiscordButton;
import com.softawii.curupira.v2.annotations.interactions.DiscordMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
@DiscordController(value = "admin", description = "hello!", guildId = 588123122805506052L, permissions = Permission.ADMINISTRATOR)
public class AdminController {

    private static final int guildPageSize = 5;
    private static final String adminGuildNextPage = "admin-guild-next-page";
    private static final String adminGuildPrevPage = "admin-guild-prev-page";
    private static final String adminGuildsMenu    = "admin-guild-menu-intrct";

    private void sendGetGuildsResponse(IReplyCallback callback, JDA jda, List<Guild> guilds, int page) {
        int maxPages = (int) Math.ceil((double) guilds.size() / AdminController.guildPageSize);

        if(page < 0) {
            page = 0;
        } else if(page >= maxPages) {
            page = maxPages - 1;
        }

        // Get the guilds for the current page
        int start = page * AdminController.guildPageSize;
        int end = Math.min(start + AdminController.guildPageSize, guilds.size());
        List<Guild> guildsPage = guilds.subList(start, end);

        StringSelectMenu.Builder menu = StringSelectMenu.create(adminGuildsMenu).setMaxValues(1).setMinValues(0);
        for (Guild guild : guildsPage) {
            menu.addOption(guild.getName(), guild.getId(), guild.getMembers().size() + " members");
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(jda.getSelfUser().getName(), jda.getSelfUser().getAvatarUrl())
                .setThumbnail(jda.getSelfUser().getAvatarUrl())
                .setTitle("Guilds (Page " + (page + 1) + "/" + maxPages + ")")
                .setDescription("All connected guilds")
                .setColor(Color.CYAN);

        Button next = Button.primary(adminGuildNextPage + ':' + (page + 1), "Next").withDisabled(page == maxPages - 1);
        Button prev = Button.primary(adminGuildPrevPage + ':' + (page - 1), "Previous").withDisabled(page == 0);

        callback.replyEmbeds(embed.build()).addActionRow(menu.build()).addActionRow(next, prev).setEphemeral(true).queue();
    }

    @DiscordCommand(name = "guilds", description = "list all guilds", ephemeral = true)
    public void getGuildsCommand(SlashCommandInteractionEvent event, JDA jda) {
        sendGetGuildsResponse(event, jda, jda.getGuilds(),0);
    }

    @DiscordButton(name = adminGuildPrevPage)
    public void getPreviousPage(ButtonInteractionEvent event, JDA jda) {
        int page = Integer.parseInt(event.getComponentId().split(":")[1]);
        sendGetGuildsResponse(event, jda, jda.getGuilds(), page);
        event.getMessage().delete().queue();
    }

    @DiscordButton(name = adminGuildNextPage)
    public void getNextPage(ButtonInteractionEvent event, JDA jda) {
        int page = Integer.parseInt(event.getComponentId().split(":")[1]);
        sendGetGuildsResponse(event, jda, jda.getGuilds(), page);
        event.getMessage().delete().queue();
    }

    @DiscordMenu(name = adminGuildsMenu, ephemeral = true)
    public MessageEmbed getGuildDetails(StringSelectInteractionEvent event, JDA jda) {
        String id = event.getSelectedOptions().get(0).getValue();

        Guild guild = jda.getGuildById(id);

        if(guild == null) {
            return new EmbedBuilder()
                    .setTitle("Guild Not Found")
                    .setDescription("The guild with the ID `" + id + "` was not found or the bot is not a member of this guild.")
                    .setColor(Color.RED)
                    .build();
        } else {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(guild.getName() + " Information")
                    .setThumbnail(guild.getIconUrl())
                    .addField("Owner", guild.getOwner().getAsMention(), false)
                    .addField("Member Count", String.valueOf(guild.getMemberCount()), false)
                    .addField("Boost Count", String.valueOf(guild.getBoostCount()), false)
                    .addField("Boost Tier", "Level " + guild.getBoostTier().getKey(), false)
                    .addField("Guild Creation Date", guild.getTimeCreated().toLocalDate().toString(), false)
                    .addField("Locale", String.valueOf(guild.getLocale()), false)
                    .setFooter("Guild ID: " + guild.getIdLong(), null)
                    .setColor(Color.CYAN);

            return embed.build();
        }
    }
}
