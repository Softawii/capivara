package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.listeners.VoiceGroup;
import com.softawii.capivara.services.VoiceDroneService;
import com.softawii.capivara.services.VoiceHiveService;
import com.softawii.curupira.exceptions.MissingPermissionsException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            TextChannel  text  = hiveCategory.createTextChannel(droneName).complete();

            // Hiding channel
            Role publicRole = channel.getGuild().getPublicRole();
            text.getManager().putRolePermissionOverride(publicRole.getIdLong(), 0, Permission.VIEW_CHANNEL.getRawValue()).complete();

            // Move Member to Drone
            Guild guild = voice.getGuild();
            guild.moveVoiceMember(member, voice).queue();

            // Add voice to drone db
            VoiceDrone drone = voiceDroneService.create(new VoiceDrone(voice.getIdLong(), text.getIdLong(), member.getIdLong(), null));

            this.createControlPanel(voice, text, drone, member);
        } catch (KeyNotFoundException e) {
            LOGGER.debug("Key not found, ignoring...");
        }
    }

    private boolean isVisible(VoiceChannel channel) {
        return channel.getGuild().getPublicRole().hasPermission(channel, Permission.VIEW_CHANNEL);
    }

    private boolean canConnect(VoiceChannel channel) {
        return channel.getGuild().getPublicRole().hasPermission(channel, Permission.VOICE_CONNECT);
    }

    public void createControlPanel(VoiceChannel channel) throws KeyNotFoundException {
        createControlPanel(channel, false);
    }

    public void createControlPanel(VoiceChannel channel, boolean forceSendInVoice) throws KeyNotFoundException {
        VoiceDrone drone =  voiceDroneService.find(channel.getIdLong());
        Member member    =  channel.getGuild().getMemberById(drone.getOwnerId());
        GuildMessageChannel text = channel.getGuild().getTextChannelById(drone.getChatId());
        text = text != null && !forceSendInVoice ? text : channel;
        createControlPanel(channel, text, drone, member);
    }

    private void createControlPanel(VoiceChannel voiceChannel, GuildMessageChannel textChannel, VoiceDrone drone, Member member) {
        LOGGER.debug("Creating control panel for: {}", voiceChannel.getId());

        // region Embed Creator
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("⚙️ Control Panel - " + voiceChannel.getName());
        builder.setDescription("Here, you can control your private voiceChannel.");
        // Fields to Show
        builder.addField("Owner", voiceChannel.getGuild().getMemberById(drone.getOwnerId()).getAsMention(), true);
        builder.addField("User Limit", voiceChannel.getUserLimit() == 0 ? "No Limits" : String.valueOf(voiceChannel.getUserLimit()), true);
        builder.addField("Visible", isVisible(voiceChannel) ? "Yes" : "No", true);
        builder.addField("Connectable", canConnect(voiceChannel) ? "Yes" : "No", true);
        builder.addField("Status", drone.isPermanent() ? "Permanent" : "Temporary", true);

        // Tutorials
        builder.addField("Invite User", "/dynamic invite @user to invite someone to your channel", false);
        builder.addField("Kick User", "/dynamic kick @user to kick someone from your channel", false);
        builder.addField("Ban User", "/dynamic ban @user to ban someone from your channel", false);

        // Other things
        builder.setColor(Color.YELLOW);
        builder.setAuthor(member.getUser().getAsTag(), null, member.getUser().getAvatarUrl());

        // endregion

        // region Buttons
        // General Config

        Button config     = Button.primary(VoiceGroup.Dynamic.droneConfig           , "🔧 Settings");
        Button visibility = Button.secondary(VoiceGroup.Dynamic.droneHideShow       , isVisible(voiceChannel) ? "👻 Hide" : "👀 Visible");
        Button connect    = Button.secondary(VoiceGroup.Dynamic.dronePublicPrivate  , canConnect(voiceChannel) ? "📢 Public" : "🔒 Private");
        Button permanent  = Button.danger(VoiceGroup.Dynamic.dronePermTemp          , drone.isPermanent()     ? "⏳ Temporary" : "✨ Permanent");

        ActionRow general = ActionRow.of(config, visibility, connect, permanent);
        // endregion

        // Send the message
        sendControlPanel(textChannel, drone, builder, List.of(general));
    }

    private void sendControlPanel(GuildMessageChannel channel, VoiceDrone drone, EmbedBuilder builder, List<ActionRow> actionRows) {
        // We already have a message, so we need to update it
        if(drone.getControlPanel() != null) {
            channel.editMessageEmbedsById(drone.getControlPanel(), builder.build()).setActionRows(actionRows).queue(q -> { /* It's ok! */}, e -> {
                LOGGER.error("Error updating control panel: {}", e.getMessage());
                // If error, we need to create a new one
                sendNewControlPanel(channel, drone, builder, actionRows);
            });
        } else {
            sendNewControlPanel(channel, drone, builder, actionRows);
        }
    }

    private void sendNewControlPanel(GuildMessageChannel channel, VoiceDrone drone, EmbedBuilder builder, List<ActionRow> actionRows) {
        channel.sendMessageEmbeds(builder.build()).setActionRows(actionRows).queue(q -> {
            drone.setControlPanel(q.getIdLong());
            try {
                voiceDroneService.update(drone);
            } catch (KeyNotFoundException ex) {
                // WTF????
                throw new RuntimeException(ex);
            }
        }, ee -> LOGGER.error("Error creating control panel: {}", ee.getMessage()));
    }

    public void makePermanent(VoiceChannel channel, boolean permanent) throws KeyNotFoundException {
        VoiceDrone drone = voiceDroneService.find(channel.getIdLong());
        drone.setPermanent(permanent);
        voiceDroneService.update(drone);
    }

    private String getDroneName(Member member, VoiceHive hive) {
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

    public void checkToDeleteTemporary(VoiceChannel channel, boolean wasDeleted) {
        long snowflakeId = channel.getIdLong();

        try {
            VoiceDrone drone = voiceDroneService.find(snowflakeId);

            // Rule 1: If permanent, the drone will not be deleted
            if (drone.isPermanent() && !wasDeleted) return;

            int online = channel.getMembers().size();
            if(online == 0) {
                voiceDroneService.destroy(snowflakeId);

                TextChannel textChannel = channel.getGuild().getTextChannelById(drone.getChatId());
                if(textChannel != null) textChannel.delete().queue();
                channel.delete().queue();
            }
        } catch (KeyNotFoundException e) {
            // Do nothing
        }

    }

    public void checkToDeleteHive(Category hive, long snowflakeId) {
        try {
            VoiceHive voiceHive = voiceHiveService.find(hive.getIdLong());

            if(voiceHive.getVoiceId() == snowflakeId) {
                voiceHiveService.destroy(voiceHive.getCategoryId());
            }
        } catch (KeyNotFoundException e) {
            // Do nothing
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

    public void toggleDroneVisibility(Guild guild, MessageChannelUnion channel, Member member) throws MissingPermissionsException, KeyNotFoundException {
        VoiceChannel voiceChannel = getVoiceChannel(guild, channel, member);

        Role publicRole = guild.getPublicRole();

        if(!isVisible(voiceChannel)) voiceChannel.upsertPermissionOverride(publicRole).grant(Permission.VIEW_CHANNEL).queue();
        else                          voiceChannel.upsertPermissionOverride(publicRole).deny(Permission.VIEW_CHANNEL).queue();
    }

    public void toggleDronePublicPrivate(Guild guild, MessageChannelUnion channel, Member member) throws MissingPermissionsException, KeyNotFoundException {
        VoiceChannel voiceChannel = getVoiceChannel(guild, channel, member);

        Role publicRole = guild.getPublicRole();
        if(!canConnect(voiceChannel)) voiceChannel.upsertPermissionOverride(publicRole).grant(Permission.VOICE_CONNECT).queue();
        else                          voiceChannel.upsertPermissionOverride(publicRole).deny(Permission.VOICE_CONNECT).queue();
    }

    @Nullable
    private VoiceChannel getVoiceChannel(Guild guild, MessageChannelUnion channel, Member member) throws KeyNotFoundException, MissingPermissionsException {
        VoiceDrone drone;
        if(channel.getType() == ChannelType.VOICE) drone = voiceDroneService.find(channel.getIdLong());
        else                                       drone = voiceDroneService.findByChatId(channel.getIdLong());

        if(drone.getOwnerId() != member.getIdLong() || !member.hasPermission(Permission.MANAGE_CHANNEL)) {
            throw new MissingPermissionsException();
        }

        VoiceChannel voiceChannel = channel.getType() == ChannelType.VOICE ? channel.asVoiceChannel() : guild.getVoiceChannelById(drone.getChannelId());
        return voiceChannel;
    }

    public void toggleDronePermTemp(Guild guild, MessageChannelUnion channel, Member member) throws MissingPermissionsException, KeyNotFoundException {
        VoiceDrone drone;
        if(channel.getType() == ChannelType.VOICE) drone = voiceDroneService.find(channel.getIdLong());
        else                                       drone = voiceDroneService.findByChatId(channel.getIdLong());

        if(drone.getOwnerId() != member.getIdLong() || !member.hasPermission(Permission.MANAGE_CHANNEL)) {
            throw new MissingPermissionsException();
        }
        drone.setPermanent(!drone.isPermanent());
        voiceDroneService.update(drone);
        createControlPanel(guild.getVoiceChannelById(drone.getChannelId()));
    }
}
