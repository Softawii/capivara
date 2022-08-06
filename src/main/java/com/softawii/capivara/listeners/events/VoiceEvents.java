package com.softawii.capivara.listeners.events;

import com.softawii.capivara.core.DroneManager;
import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class VoiceEvents extends ListenerAdapter {

    private final VoiceManager voiceManager;
    private final DroneManager droneManager;

    private final Logger LOGGER = LogManager.getLogger(VoiceEvents.class);

    public VoiceEvents(VoiceManager voiceManager, DroneManager droneManager) {
        this.voiceManager = voiceManager;
        this.droneManager = droneManager;
    }

    //region Voice Events

    /**
     * This event is used to verify if the user is in a drone voice channel, if so, it will check to delete the drone
     *
     * @param event : VoiceChannelCreateEvent
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        try {
            VoiceChannel joined = (VoiceChannel) event.getChannelJoined();
            VoiceChannel left   = (VoiceChannel) event.getChannelLeft();
            Member       member = event.getMember();

            if (joined != null) {
                // Check to Delete!
                droneManager.checkToCreateTemporary(joined, member);
                // Check to Add Permissions!
                droneManager.checkToChangeChatAccess(joined, member, true);
            }
            if (left != null) {
                // Check to Remove Permissions!
                droneManager.checkToChangeChatAccess(left, member, false);
                // Check to Delete!
                droneManager.checkToDeleteTemporary(left, false);
            }
        } catch (Exception e) {
            LOGGER.error("Error on onGuildVoiceUpdate", e);
        }
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        try {
            // Checking if the channel is a category
            // If it is a dynamic category, it will be deleted
            if (event.getChannel().getType() == ChannelType.CATEGORY) {
                Category category = event.getChannel().asCategory();
                if (voiceManager.isDynamicCategory(category)) {
                    try {
                        voiceManager.unsetDynamicCategory(category);
                    } catch (KeyNotFoundException e) {
                        // Ok... it's not a dynamic category
                    }
                }
            }

            // Checking if the channel is a voice channel
            // If it is a drone voice channel, it will be deleted
            // If it is a hive voice channel, it will be deleted
            if (event.getChannel().getType() == ChannelType.VOICE) {
                VoiceChannel channel = event.getChannel().asVoiceChannel();
                // Checking if it is a drone voice channel
                droneManager.checkToDeleteTemporary(channel, true);

                // Checking if it is a hive voice channel
                Category hive        = channel.getParentCategory();
                long     snowflakeId = channel.getIdLong();

                if (hive != null) {
                    voiceManager.checkToDeleteHive(hive, snowflakeId);
                }
            }

            // Checking if the channel is a text channel
            // If it is a drone text channel, it will be deleted
            // Cancelled because if the channel was deleted, it was intentionally
            if(event.getChannel().getType() == ChannelType.TEXT) {
                // Checking if it is a drone text channel
                droneManager.recreateControlPanel(event.getChannel().asTextChannel());
            }
        } catch (Exception e) {
            LOGGER.error("Error on ChannelDeleteEvent", e);
        }
    }

    @Override
    public void onChannelUpdateParent(@NotNull ChannelUpdateParentEvent event) {
        try {
            if (event.getChannelType() == ChannelType.VOICE) {
                VoiceChannel hive         = event.getChannel().asVoiceChannel();
                Category     hiveCategory = event.getOldValue();

                if (hiveCategory != null) {
                    try {
                        VoiceHive dbHive = voiceManager.find(hiveCategory);

                        if (dbHive.getVoiceId() == hive.getIdLong()) {
                            hive.getManager().setParent(hiveCategory).queue(q -> {
                            }, e -> {
                            });
                        }

                    } catch (KeyNotFoundException e) {
                        // The category is not a hive, just ignore it...
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on ChannelUpdateParentEvent", e);
        }
    }

    @Override
    public void onGenericPermissionOverride(@NotNull GenericPermissionOverrideEvent event) {
        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        String              method  = "onGenericPermissionOverride";
        try {
            if (channel.getType() == ChannelType.VOICE) {
                VoiceChannel voice = event.getChannel().asVoiceChannel();

                try {
                    droneManager.createControlPanel(voice);
                } catch (KeyNotFoundException e) {
                    // Not  Found...
                    LOGGER.debug(method + " : error : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on " + method, e);
        }
    }

    @Override
    public void onGenericChannelUpdate(@NotNull GenericChannelUpdateEvent<?> event) {
        try {
            if (event.getChannel().getType() == ChannelType.VOICE) {
                VoiceChannel channel = event.getChannel().asVoiceChannel();

                try {
                    droneManager.createControlPanel(channel);
                } catch (KeyNotFoundException e) {
                    // Not  Found...
                    LOGGER.debug("onGenericChannelUpdate : error : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on GenericChannelUpdateEvent", e);
        }
    }


    //endregion

}
