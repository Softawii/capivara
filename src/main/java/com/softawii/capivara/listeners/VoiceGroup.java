package com.softawii.capivara.listeners;

import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.annotations.ISubGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IGroup(name = "Voice", description = "Voice", hidden=false)
public class VoiceGroup {

    private static final Logger LOGGER = LogManager.getLogger(VoiceGroup.class);

    @ISubGroup(name="Dynamic", description = "Dynamic")
    public static class Dynamic {

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

            // Verify and Decode
            GuildChannelUnion channel = event.getOption("category").getAsChannel();

            if(channel.getType() != ChannelType.CATEGORY) {
                LOGGER.debug(method + " : error : non category : " + channel);
                event.reply("You need to pass a category!").queue();
                return;
            }

            Category category = channel.asCategory();
            Member   member   = event.getMember();

            // TODO: Manager (business rule)

            LOGGER.debug(method + " : end");
        }

        @ICommand(name="unset", description = "Unset dynamic voice channels to the selected category", permissions = {Permission.ADMINISTRATOR})
        public static void unset(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            LOGGER.debug(method + " : end");
        }

        @ICommand(name="list", description = "List all dynamic voice channels categories", permissions = {Permission.ADMINISTRATOR})
        public static void list(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            LOGGER.debug(method + " : end");
        }

    }
}
