package com.softawii.capivara.listeners.events;

import com.softawii.capivara.core.DroneManager;
import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.listeners.VoiceGroup;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
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
        VoiceChannel joined = (VoiceChannel) event.getChannelJoined();
        VoiceChannel left   = (VoiceChannel) event.getChannelLeft();
        Member       member = event.getMember();

        if (joined != null) voiceManager.checkToCreateTemporary(joined, member);
        if (left != null) voiceManager.checkToDeleteTemporary(left, member);
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
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


        if (event.getChannel().getType() == ChannelType.VOICE) {
            VoiceChannel channel = event.getChannel().asVoiceChannel();
            // Checking if it is a drone voice channel
            voiceManager.checkToDeleteTemporary(channel, null);

            // Checking if it is a hive voice channel
            Category hive        = channel.getParentCategory();
            long     snowflakeId = channel.getIdLong();

            if (hive != null) {
                voiceManager.checkToDeleteHive(hive, snowflakeId);
            }
        }
    }

    @Override
    public void onChannelUpdateParent(@NotNull ChannelUpdateParentEvent event) {
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
    }

    @Override
    public void onGenericChannelUpdate(@NotNull GenericChannelUpdateEvent<?> event) {
        if (event.getChannel().getType() == ChannelType.VOICE) {
            VoiceChannel channel = event.getChannel().asVoiceChannel();

            try {
                voiceManager.createControlPanel(channel);
            } catch (KeyNotFoundException e) {
                // Not  Found...
                LOGGER.debug("onGenericChannelUpdate : error : " + e.getMessage());
            }
        }
    }


    //endregion

}
