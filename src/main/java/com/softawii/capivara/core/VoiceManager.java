package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.services.VoiceHiveService;
import com.softawii.capivara.utils.Utils;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class VoiceManager {

    public static final String configModal_idle      = "%OWNER% Channel";
    public static final String configModal_playing   = "\uD83C\uDFAE %PLAYING%";
    public static final String configModal_streaming = "\uD83D\uDCFA %CHANNEL%";
    public static final String configModal_fieldIdle      = "set-idle";
    public static final String configModal_fieldPlaying   = "set-playing";
    public static final String configModal_fieldStreaming = "set-streaming";
    public static final String configModal_createText = "set-text";
    private final VoiceHiveService voiceHiveService;
    private final Logger           LOGGER = LogManager.getLogger(VoiceManager.class);

    public VoiceManager(VoiceHiveService voiceHiveService) {
        this.voiceHiveService = voiceHiveService;
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
        long categoryId = category.getIdLong();
        long guildId    = category.getGuild().getIdLong();

        // Returns the hive
        return voiceHiveService.create(new VoiceHive(categoryId, guildId, hive.getIdLong(), configModal_idle, configModal_playing, configModal_streaming, false));
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

    public void checkToDeleteHive(Category hive, long snowflakeId) {
        try {
            VoiceHive voiceHive = voiceHiveService.find(hive.getIdLong());

            if (voiceHive.getVoiceId() == snowflakeId) {
                voiceHiveService.destroy(voiceHive.getCategoryId());
            }
        } catch (KeyNotFoundException e) {
            // Do nothing
        }
    }

    public Modal getConfigModal(Category category, String id) {
        try {
            VoiceHive voiceHive = voiceHiveService.find(category.getIdLong());

            Modal.Builder builder = Modal.create(id + ":" + category.getIdLong(), Utils.getProperString(category.getName(), Modal.MAX_TITLE_LENGTH)).addActionRow(
                    TextInput.create(configModal_fieldIdle, "Channel Name when Idle", TextInputStyle.SHORT).setValue(voiceHive.getIdle().isBlank() ? null : voiceHive.getIdle()).build()
            ).addActionRow(
                    TextInput.create(configModal_fieldPlaying, "Channel Name when Playing", TextInputStyle.SHORT).setValue(voiceHive.getPlaying().isBlank() ? null : voiceHive.getPlaying()).setRequired(false).build()
            ).addActionRow(
                    TextInput.create(configModal_fieldStreaming, "Channel Name when Streaming", TextInputStyle.SHORT).setValue(voiceHive.getStreaming().isBlank() ? null : voiceHive.getStreaming()).setRequired(false).build()
            ).addActionRow(
                    TextInput.create(configModal_createText, "Create Text Channel", TextInputStyle.SHORT).setValue(voiceHive.getCreateTextChannel().toString()).setRequired(true).build()
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
        for (ModalMapping mapping : event.getValues()) {
            if (mapping.getId().equals(configModal_fieldIdle))
                voiceHive.setIdle(mapping.getAsString());
            else if (mapping.getId().equals(configModal_fieldPlaying))
                voiceHive.setPlaying(mapping.getAsString());
            else if (mapping.getId().equals(configModal_fieldStreaming))
                voiceHive.setStreaming(mapping.getAsString());
            else if (mapping.getId().equals(configModal_createText))
                voiceHive.setCreateTextChannel(mapping.getAsString().equalsIgnoreCase("true"));
        }
        voiceHiveService.update(voiceHive);

        return voiceHive;
    }
}
