package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyAlreadyInPackageException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.services.VoiceHiveService;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VoiceManager {

    private final VoiceHiveService voiceHiveService;

    public VoiceManager(VoiceHiveService voiceHiveService) {
        this.voiceHiveService = voiceHiveService;
    }

    public boolean isDynamicCategory(Category category) {
        return voiceHiveService.existsByCategoryId(category.getIdLong());
    }

    public VoiceHive setDynamicCategory(Category category) throws ExistingDynamicCategoryException {

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
        VoiceHive voiceHive = voiceHiveService.find(category.getIdLong());

        category.getChannels().stream().filter(channel -> channel.getIdLong() == voiceHive.hiveId()).findFirst().ifPresent(channel -> {
            channel.delete().queue();
        });

        voiceHiveService.destroy(category.getIdLong());
    }

    public VoiceHive find(Category category) throws KeyNotFoundException {
        return voiceHiveService.find(category.getIdLong());
    }

    public List<VoiceHive> findAllByGuildId(long guildId) {
        return voiceHiveService.findAllByGuildId(guildId);
    }

    public void createTemporary(Category category, Member member) {

    }

    public void deleteTemporary(Category category, Member member) {

    }
}
