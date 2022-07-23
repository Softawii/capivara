package com.softawii.capivara.listeners;

import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import com.softawii.curupira.annotations.ISubGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IGroup(name = "Voice", description = "Voice", hidden=false)
public class VoiceGroup {

    private static final Logger LOGGER = LogManager.getLogger(VoiceGroup.class);

    @ISubGroup(name="Dynamic", description = "Dynamic")
    public static class Dynamic {

        @ICommand(name="set", description = "Set dynamic voice channels to the selected category", permissions = {Permission.ADMINISTRATOR})
        public static void set(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

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
