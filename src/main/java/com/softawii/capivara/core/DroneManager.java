package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.InvalidInputException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.exceptions.NotInTheDroneException;
import com.softawii.capivara.exceptions.OwnerInTheChannelException;
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
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.*;
import java.util.List;

@Component
public class DroneManager {

    private final Logger LOGGER = LogManager.getLogger(VoiceManager.class);
    private final VoiceDroneService voiceDroneService;
    private final VoiceHiveService  voiceHiveService;
    private final VoiceManager voiceManager;

    private final String renameDrone     = "drone-manager-rename";
    private final String limitDrone      = "drone-manager-limit";
    private final String connectDrone    = "drone-manager-connect";
    private final String visibilityDrone = "drone-manager-visibility";


    public DroneManager(VoiceDroneService voiceDroneService, VoiceHiveService voiceHiveService, VoiceManager voiceManager) {
        this.voiceDroneService = voiceDroneService;
        this.voiceHiveService = voiceHiveService;
        this.voiceManager = voiceManager;
    }

    public boolean canInteract(VoiceChannel channel, Member member) throws KeyNotFoundException {
        VoiceDrone drone = voiceDroneService.find(channel.getIdLong());

        return drone.getOwnerId() == member.getIdLong() || member.hasPermission(Permission.MANAGE_CHANNEL);
    }

    public Modal checkConfigDrone(Guild guild, MessageChannelUnion channel, Member member, String customId) throws KeyNotFoundException, MissingPermissionsException {
        VoiceDrone voiceDrone;
        VoiceChannel voice;
        if(channel.getType() == ChannelType.VOICE) {
            voiceDrone = voiceDroneService.find(channel.getIdLong());
            voice      = channel.asVoiceChannel();
        }
        else {
            voiceDrone = voiceDroneService.findByChatId(channel.getIdLong());
            voice      = guild.getVoiceChannelById(voiceDrone.getChannelId());
        }

        if (voiceDrone.getOwnerId().equals(member.getIdLong()) || member.hasPermission(Permission.MANAGE_CHANNEL)) {
            Modal.Builder builder = Modal.create(customId, "Rename Room");
            builder.setTitle("Settings of " + voice.getName() + "!")
                    .addActionRow(
                            TextInput.create(renameDrone, "Room Name", TextInputStyle.SHORT)
                                    .setValue(voice.getName())
                                    .setMaxLength(256).build()
                    ).addActionRow(
                            TextInput.create(limitDrone, "Limit of Users (Number)", TextInputStyle.SHORT)
                                    .setValue(String.valueOf(voice.getUserLimit()))
                                    .setMaxLength(10).build()
                    ).addActionRow(
                            TextInput.create(connectDrone, "Private or Public", TextInputStyle.SHORT)
                                    .setValue(canConnect(voice) ? "Public" : "Private")
                                    .setMaxLength(10).build()
                    ).addActionRow(
                            TextInput.create(visibilityDrone, "Hidden or Visible", TextInputStyle.SHORT)
                                    .setValue(isVisible(voice) ? "Visible" : "Hidden")
                                    .setMaxLength(10).build()
                    );
            return builder.build();
        }
        throw new MissingPermissionsException();
    }

    public void updateDrone(ModalInteractionEvent event) throws InvalidInputException {
        String newName            = event.getValue(renameDrone).getAsString();
        int    newLimitDrone      = Integer.parseInt(event.getValue(limitDrone).getAsString()); // Throw NumberFormatException if not a number
        String newConnectDrone    = event.getValue(connectDrone).getAsString().toLowerCase();
        String newVisibilityDrone = event.getValue(visibilityDrone).getAsString().toLowerCase();

        // CHECK
        if (!newConnectDrone.matches("public|private")) {
            throw new InvalidInputException("public|private");
        }
        if (!newVisibilityDrone.matches("visible|hidden")) {
            throw new InvalidInputException("visible|hidden");
        }
        if(newLimitDrone > 99) {
            newLimitDrone = 99;
        } else if(newLimitDrone < 0) {
            newLimitDrone = 0;
        }

        MessageChannelUnion channel     = event.getChannel();
        VoiceChannel voiceChannel       = null;
        TextChannel  textChannel        = null;
        if(channel.getType() == ChannelType.VOICE) {
            voiceChannel = event.getChannel().asVoiceChannel();
            try {
                Long id = voiceDroneService.find(channel.getIdLong()).getChatId();
                textChannel = event.getGuild().getTextChannelById(id);
            } catch (KeyNotFoundException e) {
                // It's ok! The chat was deleted
            }
        } else {
            try {
                Long id = voiceDroneService.findByChatId(channel.getIdLong()).getChannelId();
                voiceChannel = event.getGuild().getVoiceChannelById(id);
                textChannel = channel.asTextChannel();
            } catch (KeyNotFoundException e) {
                // Impossible... but just in case
            }
        }

        Role publicRole = event.getGuild().getPublicRole();

        Collection<Permission> give = new ArrayList<>();
        Collection<Permission> deny = new ArrayList<>();

        if (newConnectDrone.equals("public")) give.add(Permission.VOICE_CONNECT);
        else deny.add(Permission.VOICE_CONNECT);

        if (newVisibilityDrone.equals("visible")) give.add(Permission.VIEW_CHANNEL);
        else deny.add(Permission.VIEW_CHANNEL);

        RestAction<Void> action = voiceChannel.getManager().setName(newName).setUserLimit(newLimitDrone)
                .and(voiceChannel.upsertPermissionOverride(publicRole).deny(deny).grant(give));
        if(textChannel != null) {
            action = action.and(textChannel.getManager().setName(newName));
        }
        action.submit();
    }

    public void checkToChangeChatAccess(VoiceChannel channel, Member member, boolean joined) {
        VoiceDrone voiceDrone = null;
        try {
            voiceDrone = voiceDroneService.find(channel.getIdLong());

            TextChannel text = channel.getGuild().getTextChannelById(voiceDrone.getChatId());

            if(text == null) return;

            if(joined) {
                text.upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL)
                        .and(channel.upsertPermissionOverride(member).grant(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL)).submit();
            } else if(voiceDrone.getOwnerId() != member.getIdLong()) {
                text.getManager().removePermissionOverride(member).submit();
            }
        } catch (KeyNotFoundException e) {
            // Ignoring...
        }
    }

    public void recreateControlPanel(TextChannel channel) {
        VoiceDrone voiceDrone = null;
        try {
            voiceDrone = voiceDroneService.findByChatId(channel.getIdLong());

            if(voiceDrone != null) {
                VoiceChannel voiceChannel = channel.getGuild().getVoiceChannelById(voiceDrone.getChannelId());
                if(voiceChannel != null) this.createControlPanel(voiceChannel, true);
            }
        } catch (KeyNotFoundException e) {
            // Ignoring...
        }
    }

    public void checkToDeleteTemporary(VoiceChannel channel, Member member, boolean wasDeleted) {
        long snowflakeId = channel.getIdLong();

        try {
            VoiceDrone drone = voiceDroneService.find(snowflakeId);

            // Rule 1: If permanent, the drone will not be deleted
            if (drone.isPermanent() && !wasDeleted) return;

            int online = channel.getMembers().size();
            TextChannel textChannel = channel.getGuild().getTextChannelById(drone.getChatId());
            if(online == 0) {
                voiceDroneService.destroy(snowflakeId);

                if(textChannel != null) {
                    textChannel.delete().and(channel.delete()).submit();
                } else {
                    channel.delete().submit();
                }
            } else if(member != null && member.getIdLong() == drone.getOwnerId()) {
                // Election Mode!
                MessageEmbed embed = claimChat();
                Button claim = Button.success(VoiceGroup.Dynamic.droneClaim, "Claim");

                Message claimMessage;
                if(textChannel != null) {
                    claimMessage = textChannel.sendMessageEmbeds(embed).setActionRows(ActionRow.of(claim)).complete();
                } else {
                    claimMessage = channel.sendMessageEmbeds(embed).setActionRows(ActionRow.of(claim)).complete();
                }

                drone.setClaimMessage(claimMessage.getIdLong());
                LOGGER.debug("Claim message: {}", claimMessage.getIdLong());
                voiceDroneService.update(drone);
            }
        } catch (KeyNotFoundException e) {
            // Do nothing
        }
    }

    private MessageEmbed claimChat() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("The chat does not have an active owner!");
        builder.setDescription("Click in the button to claim it to you!");
        builder.setColor(Color.ORANGE);
        return builder.build();
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
            Guild guild = voice.getGuild();
            text.upsertPermissionOverride(publicRole).deny(Permission.VIEW_CHANNEL)
                    .and(text.upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL))
                    .and(voice.upsertPermissionOverride(member).grant(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL))
                    .and(guild.moveVoiceMember(member, voice)).submit();

            // Add voice to drone db
            voiceDroneService.create(new VoiceDrone(voice.getIdLong(), text.getIdLong(), member.getIdLong(), null));
            LOGGER.debug("Creating New Channel!");
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

        net.dv8tion.jda.api.interactions.components.buttons.Button config     = net.dv8tion.jda.api.interactions.components.buttons.Button.primary(VoiceGroup.Dynamic.droneConfig           , "🔧 Settings");
        net.dv8tion.jda.api.interactions.components.buttons.Button visibility = net.dv8tion.jda.api.interactions.components.buttons.Button.secondary(VoiceGroup.Dynamic.droneHideShow       , isVisible(voiceChannel) ? "👻 Hide" : "👀 Visible");
        net.dv8tion.jda.api.interactions.components.buttons.Button connect    = net.dv8tion.jda.api.interactions.components.buttons.Button.secondary(VoiceGroup.Dynamic.dronePublicPrivate  , canConnect(voiceChannel) ? "📢 Public" : "🔒 Private");
        net.dv8tion.jda.api.interactions.components.buttons.Button permanent  = Button.danger(VoiceGroup.Dynamic.dronePermTemp          , drone.isPermanent()     ? "⏳ Temporary" : "✨ Permanent");

        ActionRow general = ActionRow.of(config, visibility, connect, permanent);
        // endregion

        // Send the message
        sendControlPanel(textChannel, drone, builder, java.util.List.of(general));
    }

    private void sendControlPanel(GuildMessageChannel channel, VoiceDrone drone, EmbedBuilder builder, java.util.List<ActionRow> actionRows) {
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
        String droneName = VoiceManager.configModal_idle;
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

    public void toggleDroneVisibility(Guild guild, MessageChannelUnion channel, Member member) throws MissingPermissionsException, KeyNotFoundException {
        VoiceChannel voiceChannel = getVoiceChannel(guild, channel, member);

        Role publicRole = guild.getPublicRole();

        if(!isVisible(voiceChannel)) voiceChannel.upsertPermissionOverride(publicRole).grant(Permission.VIEW_CHANNEL).submit();
        else                          voiceChannel.upsertPermissionOverride(publicRole).deny(Permission.VIEW_CHANNEL).submit();
    }

    public void toggleDronePublicPrivate(Guild guild, MessageChannelUnion channel, Member member) throws MissingPermissionsException, KeyNotFoundException {
        VoiceChannel voiceChannel = getVoiceChannel(guild, channel, member);

        Role publicRole = guild.getPublicRole();
        if(!canConnect(voiceChannel)) voiceChannel.upsertPermissionOverride(publicRole).grant(Permission.VOICE_CONNECT).submit();
        else                          voiceChannel.upsertPermissionOverride(publicRole).deny(Permission.VOICE_CONNECT).submit();
    }

    @Nullable
    private VoiceChannel getVoiceChannel(Guild guild, MessageChannelUnion channel, Member member) throws KeyNotFoundException, MissingPermissionsException {
        VoiceDrone drone;
        if(channel.getType() == ChannelType.VOICE) drone = voiceDroneService.find(channel.getIdLong());
        else                                       drone = voiceDroneService.findByChatId(channel.getIdLong());

        if(!drone.getOwnerId().equals(member.getIdLong()) && !member.hasPermission(Permission.MANAGE_CHANNEL)) {
            throw new MissingPermissionsException();
        }

        VoiceChannel voiceChannel = channel.getType() == ChannelType.VOICE ? channel.asVoiceChannel() : guild.getVoiceChannelById(drone.getChannelId());
        return voiceChannel;
    }

    public void toggleDronePermTemp(Guild guild, MessageChannelUnion channel, Member member) throws MissingPermissionsException, KeyNotFoundException {
        VoiceDrone drone;
        if(channel.getType() == ChannelType.VOICE) drone = voiceDroneService.find(channel.getIdLong());
        else                                       drone = voiceDroneService.findByChatId(channel.getIdLong());

        if(!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            throw new MissingPermissionsException();
        }
        drone.setPermanent(!drone.isPermanent());
        voiceDroneService.update(drone);
        createControlPanel(guild.getVoiceChannelById(drone.getChannelId()));
    }

    public void checkToRemoveClaimMessage(VoiceChannel joined, Member member) {
        try {
            VoiceDrone drone = voiceDroneService.find(joined.getIdLong());

            LOGGER.debug("Claim message: {}", drone.getClaimMessage());

            TextChannel text = joined.getGuild().getTextChannelById(drone.getChatId());

            if(member.getIdLong() == drone.getOwnerId()) {

                if(text != null) text.deleteMessageById(drone.getClaimMessage()).submit();
                else             joined.deleteMessageById(drone.getClaimMessage()).submit();
                drone.setClaimMessage(0L);
                voiceDroneService.update(drone);
            }
        } catch (KeyNotFoundException e) {
            // Ok!
        }
    }

    public void claimDrone(Guild guild, MessageChannelUnion channel, Member member) throws KeyNotFoundException, OwnerInTheChannelException, NotInTheDroneException {
        VoiceDrone drone;
        if(channel.getType() == ChannelType.VOICE) drone = voiceDroneService.find(channel.getIdLong());
        else                                       drone = voiceDroneService.findByChatId(channel.getIdLong());

        VoiceChannel voiceChannel = guild.getVoiceChannelById(drone.getChannelId());
        TextChannel  textChannel  = guild.getTextChannelById(drone.getChatId());

        if(voiceChannel.getMembers().stream().anyMatch(m -> m.getIdLong() == drone.getOwnerId())) {
            throw new OwnerInTheChannelException("You are already in the drone!");
        }

        if(!voiceChannel.getMembers().contains(member)) {
            throw new NotInTheDroneException("You are not in the drone!");
        }

        if(textChannel != null) textChannel.getManager().removePermissionOverride(drone.getOwnerId()).submit();

        drone.setOwnerId(member.getIdLong());
        voiceDroneService.update(drone);
        createControlPanel(voiceChannel);
    }
}
