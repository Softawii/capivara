package com.softawii.capivara.listeners;

import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.annotations.ISubGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.List;

@IGroup(name = "Voice", description = "Voice", hidden=false)
public class VoiceGroup {

    private static final Logger LOGGER = LogManager.getLogger(VoiceGroup.class);
    public static VoiceManager voiceManager;

    @ISubGroup(name="Dynamic", description = "Dynamic")
    public static class Dynamic extends ListenerAdapter {

        public static VoiceManager voiceManager;

        /**
         * This command is used to put a 'Voice Dynamic Master' channel in a category
         *
         * @param event : SlashCommandInteractionEvent
         */
        @ICommand(name="set", description = "Set dynamic voice channels to the selected category", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "category", description = "Category to add dynamic voice chat!", required = true, type = OptionType.CHANNEL)
        public static void set(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            // region Verify and Decode
            GuildChannelUnion channel = event.getOption("category").getAsChannel();

            if(channel.getType() != ChannelType.CATEGORY) {
                LOGGER.debug(method + " : error : non category : " + channel);
                event.reply("You need to pass a category!").queue();
                return;
            }

            // endregion

            Category category = channel.asCategory();

            //  Manager (business rule)
            try {
                VoiceHive hive = voiceManager.setDynamicCategory(category);

                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();

                GuildChannel hiveChannel = guild.getGuildChannelById(hive.hiveId());

                MessageEmbed embed = Utils.simpleEmbed("Voice Hive Initialized",
                        "Just click in " + hiveChannel.getAsMention() + " to create a dynamic channel!", Color.GREEN);

                event.replyEmbeds(embed).queue();
            } catch (ExistingDynamicCategoryException e) {
                LOGGER.debug(method + " : error : " + e.getMessage());

                VoiceHive hive;
                try {
                    hive = voiceManager.find(category);
                } catch (KeyNotFoundException ex) {
                    // Wait, this is a bug!
                    LOGGER.debug(method + " : error : " + ex.getMessage());
                    return;
                }
                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();
                GuildChannel hiveChannel = guild.getVoiceChannelById(hive.hiveId());
                MessageEmbed embed = Utils.simpleEmbed("The current category is already a hive of voice channels!",
                        "Just click in " + hiveChannel.getAsMention() + " to create a dynamic channel!", Color.RED);
                event.replyEmbeds(embed).queue();
            }

            LOGGER.debug(method + " : end");
        }

        @ICommand(name="unset", description = "Unset dynamic voice channels to the selected category", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "category", description = "Category to add dynamic voice chat!", required = true, type = OptionType.CHANNEL)
        public static void unset(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            // region Verify and Decode
            GuildChannelUnion channel = event.getOption("category").getAsChannel();

            if(channel.getType() != ChannelType.CATEGORY) {
                LOGGER.debug(method + " : error : non category : " + channel);
                event.reply("You need to pass a category!").queue();
                return;
            }

            // endregion

            Category category = channel.asCategory();

            //  Manager (business rule)
            try {
                voiceManager.unsetDynamicCategory(category);

                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();

                MessageEmbed embed = Utils.simpleEmbed("Voice Hive Uninitialized",
                        "The dynamic voice channels are now unset!", Color.GREEN);

                event.replyEmbeds(embed).queue();
            } catch (KeyNotFoundException e) {
                LOGGER.debug(method + " : error : " + e.getMessage());

                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();
                MessageEmbed embed = Utils.simpleEmbed("The current category is not a hive of voice channels!",
                        "You need to set a dynamic voice channel first!", Color.RED);
                event.replyEmbeds(embed).queue();
            }

            LOGGER.debug(method + " : end");
        }

        @ICommand(name="list", description = "List all dynamic voice channels categories", permissions = {Permission.ADMINISTRATOR})
        public static void list(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            Long guildId = event.getGuild().getIdLong();
            Guild guild = event.getGuild();

            List<VoiceHive> hives = voiceManager.findAllByGuildId(guildId);

            StringBuilder sb = new StringBuilder();
            for(VoiceHive hive : hives) {
                VoiceChannel hiveChannel = guild.getVoiceChannelById(hive.hiveId());
                Category     parent      = hiveChannel.getParentCategory();

                if(hiveChannel == null || parent == null) {
                    LOGGER.debug(method + " : error : hive channel not found : " + hive.hiveId());
                    continue;
                }

                sb.append(hiveChannel.getParentCategory().getAsMention())
                        .append(" : ")
                        .append(hiveChannel.getAsMention())
                        .append("\n");
            }
            String message = sb.toString();

            MessageEmbed embed = Utils.simpleEmbed("Voice Hives", message, Color.GREEN);
            event.replyEmbeds(embed).queue();

            LOGGER.debug(method + " : end");
        }

    }
}
