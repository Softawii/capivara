package com.softawii.capivara.events;

import com.softawii.capivara.core.DroneManager;
import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.controller.MainExceptionController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
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
    private final MainExceptionController exceptionHandler;

    public VoiceEvents(JDA jda, VoiceManager voiceManager, DroneManager droneManager, MainExceptionController exceptionHandler) {
        this.voiceManager = voiceManager;
        this.droneManager = droneManager;
        this.exceptionHandler = exceptionHandler;
        this.droneManager.checkEmptyDrones();
        this.voiceManager.checkRemovedHives();
        jda.addEventListener(this);
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
            AudioChannelUnion joined = event.getChannelJoined();
            AudioChannelUnion left   = event.getChannelLeft();
            Member member = event.getMember();

            if (joined != null && joined.getType() == ChannelType.VOICE) {
                // Check to Delete!
                droneManager.checkToCreateTemporary((VoiceChannel) joined, member);
                // Check to Add Permissions!
                droneManager.checkToChangeChatAccess((VoiceChannel) joined, member, true);
                // Check to Remove Claim Message!
                droneManager.checkToRemoveClaimMessage((VoiceChannel) joined, member);
            }
            if (left != null && left.getType() == ChannelType.VOICE) {
                // Check to Remove Permissions!
                droneManager.checkToChangeChatAccess((VoiceChannel) left, member, false);
                // Check to Delete!
                droneManager.checkToDeleteTemporary((VoiceChannel) left, member, false);
            }
        } catch (Exception e) {
            LOGGER.error("Error on onGuildVoiceUpdate", e);
            handleException(e, event);
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
                droneManager.checkToDeleteTemporary(channel, null, true);

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
            if (event.getChannel().getType() == ChannelType.TEXT) {
                // Checking if it is a drone text channel
                droneManager.recreateControlPanel(event.getChannel().asTextChannel());
            }
        } catch (Exception e) {
            LOGGER.error("Error on ChannelDeleteEvent", e);
            handleException(e, event);
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
            handleException(e, event);
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
                    // Not Found... ignoring
                    LOGGER.debug(method + " : error : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on " + method, e);
            handleException(e, event);
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
                    // Not Found... ignoring
                    LOGGER.debug("onGenericChannelUpdate : error : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on GenericChannelUpdateEvent", e);
            handleException(e, event);
        }
    }
    //endregion

    private void handleException(Exception exception, Event event) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(exception, event);
        }
    }

}
