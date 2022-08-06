package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.exceptions.InvalidInputException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.services.VoiceDroneService;
import com.softawii.curupira.exceptions.MissingPermissionsException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Component
public class DroneManager {

    private final VoiceDroneService voiceDroneService;
    private final VoiceManager voiceManager;

    private final String renameDrone     = "drone-manager-rename";
    private final String limitDrone      = "drone-manager-limit";
    private final String connectDrone    = "drone-manager-connect";
    private final String visibilityDrone = "drone-manager-visibility";

    public DroneManager(VoiceDroneService voiceDroneService, VoiceManager voiceManager) {
        this.voiceDroneService = voiceDroneService;
        this.voiceManager = voiceManager;
    }

    private boolean canConnect(VoiceChannel channel) {
        return channel.getGuild().getPublicRole().getPermissions(channel).contains(Permission.VOICE_CONNECT);
    }

    private boolean isVisible(VoiceChannel channel) {
        return channel.getGuild().getPublicRole().getPermissions(channel).contains(Permission.VIEW_CHANNEL);
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

        voiceChannel.getManager().setName(newName).setUserLimit(newLimitDrone).queue();
        voiceChannel.upsertPermissionOverride(publicRole).deny(deny).grant(give).queue();
        if(textChannel != null) {
            textChannel.getManager().setName(newName).queue();
        }
    }

    public void checkToChangeChatAccess(VoiceChannel channel, Member member, boolean joined) {
        VoiceDrone voiceDrone = null;
        try {
            voiceDrone = voiceDroneService.find(channel.getIdLong());

            TextChannel text = channel.getGuild().getTextChannelById(voiceDrone.getChatId());

            if(text == null) return;

            if(joined) {
                text.upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL).queue();
            } else if(voiceDrone.getOwnerId() != member.getIdLong()) {
                text.getManager().removePermissionOverride(member).queue();
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
                if(voiceChannel != null) voiceManager.createControlPanel(voiceChannel, true);
            }
        } catch (KeyNotFoundException e) {
            // Ignoring...
        }
    }
}
