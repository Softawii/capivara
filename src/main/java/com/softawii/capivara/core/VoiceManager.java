package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.listeners.VoiceGroup;
import com.softawii.capivara.services.VoiceDroneService;
import com.softawii.capivara.services.VoiceHiveService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class VoiceManager {

    private final VoiceHiveService  voiceHiveService;
    private final VoiceDroneService voiceDroneService;
    private final Logger LOGGER = LogManager.getLogger(VoiceManager.class);

    // region Constants

    public static final String configModal_idle       = "%OWNER% Channel";
    public static final String configModal_playing    = "\uD83C\uDFAE %PLAYING%";
    public static final String configModal_streaming  = "\uD83D\uDCFA %CHANNEL%";

    // fields
    public static final String configModal_fieldIdle      = "set-idle";
    public static final String configModal_fieldPlaying   = "set-playing";
    public static final String configModal_fieldStreaming = "set-streaming";

    // endregion

    public VoiceManager(VoiceHiveService voiceHiveService, VoiceDroneService voiceDroneService) {
        this.voiceHiveService = voiceHiveService;
        this.voiceDroneService = voiceDroneService;
    }

    public boolean isDynamicCategory(Category category) {
        return voiceHiveService.existsByCategoryId(category.getIdLong());
    }

    public VoiceHive setDynamicCategory(Category category) throws ExistingDynamicCategoryException {
        LOGGER.debug("setDynamicCategory: " + category);
        // Verify if the category is already a dynamic category
        if (voiceHiveService.existsByCategoryId(category.getIdLong())) throw new ExistingDynamicCategoryException();

        // Creates a hive (wait for the voice channel to be created)
        VoiceChannel hive = category.createVoiceChannel("➕ Create a New Channel").complete();

        // DB Keys
        long categoryId           = category.getIdLong();
        long guildId              = category.getGuild().getIdLong();

        // Returns the hive
        return voiceHiveService.create(new VoiceHive(categoryId, guildId, hive.getIdLong(), configModal_idle, configModal_playing, configModal_streaming));
    }

    public void unsetDynamicCategory(Category category) throws KeyNotFoundException {
        LOGGER.debug("Unsetting dynamic category: {}", category);

        VoiceHive voiceHive = voiceHiveService.find(category.getIdLong());

        Objects.requireNonNull(category.getGuild().getVoiceChannelById(voiceHive.getVoiceId())).delete().complete();

        voiceHiveService.destroy(category.getIdLong());
    }

    public VoiceHive find(Category category) throws KeyNotFoundException {
        return voiceHiveService.find(category.getIdLong());
    }

    public List<VoiceHive> findAllByGuildId(long guildId) {
        return voiceHiveService.findAllByGuildId(guildId);
    }

    public void checkToCreateTemporary(VoiceChannel channel, Member member) {

        long snowflakeId = channel.getParentCategoryIdLong();

        try {
            // Checking if the current category is a dynamic category
            VoiceHive hive = voiceHiveService.find(snowflakeId);

            if(channel.getIdLong() != hive.getVoiceId()) return;

            // Creating the voice drone
            Category hiveCategory = channel.getParentCategory();
            String droneName = this.getDroneName(member, hive);

            // Create voice
            VoiceChannel voice = hiveCategory.createVoiceChannel(droneName).complete();

            // Move Member to Drone
            Guild guild = voice.getGuild();
            guild.moveVoiceMember(member, voice).queue();

            // Add voice to drone db
            VoiceDrone drone = voiceDroneService.create(new VoiceDrone(voice.getIdLong(), member.getIdLong(), null));

            // TODO: Add embed to show the drone options
            this.createControlPanel(voice, drone);
        } catch (KeyNotFoundException e) {
            LOGGER.debug("Key not found, ignoring...");
        }
    }

    public boolean isVisible(VoiceChannel channel) {
        return channel.getGuild().getPublicRole().getPermissions(channel).contains(Permission.VIEW_CHANNEL);
    }

    public boolean canConnect(VoiceChannel channel) {
        return channel.getGuild().getPublicRole().getPermissions(channel).contains(Permission.VOICE_CONNECT);
    }

    public void createControlPanel(VoiceChannel channel, VoiceDrone drone) {
        LOGGER.debug("Creating control panel for: {}", channel.getId());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("⚙️ Control Panel - " + channel.getName());
        builder.setDescription("Here, you can control your private channel.");
        // Fields to Show
        builder.addField("Owner", channel.getGuild().getMemberById(drone.getOwnerId()).getAsMention(), true);
        builder.addField("User Limit", channel.getUserLimit() == 0 ? "No Limits" : String.valueOf(channel.getUserLimit()), true);
        builder.addField("Visible", isVisible(channel) ? "Yes" : "No", true);
        builder.addField("Connectable", canConnect(channel) ? "Yes" : "No", true);

        // Buttons

        // General Config
        Button connect = Button.success(canConnect(channel) ? "private" : "public", canConnect(channel) ? "Make Private" : "Make Public");
        Button visible = Button.success(isVisible(channel) ? "hide" : "show", isVisible(channel) ? "Hide Channel" : "Show Channel");
        Button limit   = Button.secondary("limit", "Change Limit");

        // Specific Config
        Button invite = Button.primary("invite", "Invite User");
        Button kick   = Button.secondary("kick", "Kick User");
        Button ban    = Button.danger("ban", "Ban User");

        ActionRow general = ActionRow.of(connect, visible, limit);
        ActionRow specific = ActionRow.of(invite, kick, ban);

        // Send the message
        if(drone.getControlPanel() != null) {
           channel.editMessageEmbedsById(drone.getControlPanel(), builder.build()).setActionRows(general, specific).complete();
        } else {
            channel.sendMessageEmbeds(builder.build()).setActionRows(general, specific).queue(q -> {
                drone.setControlPanel(q.getIdLong());
            }, e -> {
                LOGGER.error("Error creating control panel: {}", e.getMessage());
            });
        }
    }

    public String getDroneName(Member member, VoiceHive hive) {
        String droneName = configModal_idle;
        String username = member.getNickname() == null ? member.getEffectiveName() : member.getNickname();

        Optional<Activity> streaming = member.getActivities().stream().filter(activity -> activity.getType() == Activity.ActivityType.STREAMING).findFirst();
        Optional<Activity> playing   = member.getActivities().stream().filter(activity -> activity.getType() == Activity.ActivityType.PLAYING).findFirst();

        if(streaming.isPresent() && !hive.getStreaming().isBlank()) {
            droneName = hive.getStreaming().replaceAll("%CHANNEL%", streaming.get().getName());
        }
        if(playing.isPresent() && !hive.getPlaying().isBlank()) {
            if(streaming.isEmpty()) droneName = hive.getPlaying().replaceAll("%PLAYING%", playing.get().getName());
            else                    droneName = droneName.replaceAll("%PLAYING%", streaming.get().getName());
        }
        if(playing.isEmpty() && streaming.isEmpty()) {
            droneName = hive.getIdle();
        }

        droneName = droneName.replaceAll("%OWNER%", username);

        return droneName;
    }

    public void checkToDeleteTemporary(VoiceChannel channel, Member member) {
        long snowflakeId = channel.getIdLong();

        try {
            VoiceDrone drone = voiceDroneService.find(snowflakeId);

            int online = channel.getMembers().size();

            if(online == 0) {
                voiceDroneService.destroy(snowflakeId);
                channel.delete().queue();
            }
        } catch (KeyNotFoundException e) {
            LOGGER.debug("Key not found, ignoring...");
        }

    }

    public void checkToDeleteHive(Category hive, long snowflakeId) {
        try {
            VoiceHive voiceHive = voiceHiveService.find(hive.getIdLong());

            if(voiceHive.getVoiceId() == snowflakeId) {
                voiceHiveService.destroy(voiceHive.getCategoryId());
            }
        } catch (KeyNotFoundException e) {
            LOGGER.debug("Key not found, ignoring...");
        }
    }

    public Modal getConfigModal(Category category, String id) {
        try {
            VoiceHive voiceHive = voiceHiveService.find(category.getIdLong());

            Modal.Builder builder = Modal.create(id + ":" + category.getIdLong(), "Configuring " + category.getName() + " (Empty for don't use)").addActionRow(
                    TextInput.create(configModal_fieldIdle, "Channel Name when Idle", TextInputStyle.SHORT).setValue(voiceHive.getIdle()).build()
            ).addActionRow(
                    TextInput.create(configModal_fieldPlaying, "Channel Name when Playing", TextInputStyle.SHORT).setValue(voiceHive.getPlaying()).setRequired(false).build()
            ).addActionRow(
                    TextInput.create(configModal_fieldStreaming, "Channel Name when Streaming", TextInputStyle.SHORT).setValue(voiceHive.getStreaming()).setRequired(false).build()
            );

            return builder.build();

        } catch (KeyNotFoundException e) {
            LOGGER.debug("Key not found, ignoring...");
        }
        return null;
    }

    public VoiceHive setConfigModal(ModalInteractionEvent event, Category category) throws KeyNotFoundException {
        VoiceHive voiceHive = voiceHiveService.find(category.getIdLong());

        // Set the new values
        for(ModalMapping mapping : event.getValues()) {
            if(mapping.getId().equals(configModal_fieldIdle))
                voiceHive.setIdle(mapping.getAsString());
            else if(mapping.getId().equals(configModal_fieldPlaying))
                voiceHive.setPlaying(mapping.getAsString());
            else if(mapping.getId().equals(configModal_fieldStreaming))
                voiceHive.setStreaming(mapping.getAsString());
        }
        voiceHiveService.update(voiceHive);

        return voiceHive;
    }
}
