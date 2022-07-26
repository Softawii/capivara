package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.services.VoiceDroneService;
import com.softawii.capivara.services.VoiceHiveService;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class VoiceManager {

    private final VoiceHiveService  voiceHiveService;
    private final VoiceDroneService voiceDroneService;
    private final Logger LOGGER = LogManager.getLogger(VoiceManager.class);

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

        // Creates a hive
        VoiceChannel hive = category.createVoiceChannel("Hive").complete();

        // DB Keys
        long categoryId           = category.getIdLong();
        long guildId              = category.getGuild().getIdLong();
        VoiceHive.HiveKey hiveKey = new VoiceHive.HiveKey(categoryId, guildId);

        // Returns the hive
        return voiceHiveService.create(new VoiceHive(hiveKey, hive.getIdLong()));
    }

    public void unsetDynamicCategory(Category category) throws KeyNotFoundException {
        LOGGER.debug("Unsetting dynamic category: {}", category);

        VoiceHive voiceHive = voiceHiveService.find(category.getIdLong());

        Objects.requireNonNull(category.getGuild().getVoiceChannelById(voiceHive.hiveId())).delete().complete();

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

            if(channel.getIdLong() != hive.hiveId()) return;

            // Creating the voice drone
            Category hiveCategory = channel.getParentCategory();

            // Defining the drone name
            String username = member.getNickname() == null ? member.getEffectiveName() : member.getNickname();
            String droneName = "Drone - " + username;

            // Create voice
            VoiceChannel voice = hiveCategory.createVoiceChannel(droneName).complete();

            // Move Member to Drone
            Guild guild = voice.getGuild();
            guild.moveVoiceMember(member, voice).queue();

            // TODO: Add embed to show the drone options

            // Add voice to drone db
            voiceDroneService.create(new VoiceDrone(voice.getIdLong(), member.getIdLong()));
        } catch (KeyNotFoundException e) {
            LOGGER.info("Key not found, ignoring...");
        }
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

            if(voiceHive.hiveId() == snowflakeId) {
                voiceHiveService.destroy(voiceHive.hiveKey().getCategoryId());
            }
        } catch (KeyNotFoundException e) {
            LOGGER.debug("Key not found, ignoring...");
        }
    }
}
