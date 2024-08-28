package com.softawii.capivara.controller;

import com.softawii.capivara.core.DroneManager;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.exceptions.MissingPermissionsException;
import com.softawii.capivara.exceptions.NotInTheDroneException;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@DiscordController(value = "drone", description = "Voice Drone Controller")
public class VoiceDroneController {
    private final DroneManager droneManager;
    public static final long inviteDeadline = 1000L * 10L * 60L;

    public VoiceDroneController(DroneManager droneManager) {
        this.droneManager = droneManager;
    }

    private VoiceChannel validateMember(Member member) throws KeyNotFoundException, MissingPermissionsException, NotInTheDroneException {
        if(!member.getVoiceState().inAudioChannel()) throw new NotInTheDroneException();

        VoiceChannel channel = member.getVoiceState().getChannel().asVoiceChannel();

        if(!droneManager.canInteract(channel, member)) throw new MissingPermissionsException();

        return channel;
    }

    @DiscordCommand(name = "invite", description = "Invite a user to join in a channel", ephemeral = true)
    public TextLocaleResponse invite(@RequestInfo Member member, @DiscordParameter(name = "name", description = "User to invite") Member invited) throws NotInTheDroneException, MissingPermissionsException, KeyNotFoundException {
        VoiceChannel channel = validateMember(member);
        channel.createInvite().setUnique(true).deadline(System.currentTimeMillis() + inviteDeadline).queue(q -> {
            channel.getManager().putPermissionOverride(invited, List.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), Collections.emptyList()).queue();
            String name = member.getEffectiveName() + (member.getNickname() != null ? " (" + member.getNickname() + ")" : "");
            invited.getUser().openPrivateChannel().queue(chn -> chn.sendMessage(name + "invited you to join in a channel!\n" + q.getUrl()).queue());
        });

        return new TextLocaleResponse("drone.invite.response", invited.getEffectiveName());
    }

    @DiscordCommand(name = "kick", description = "Kick a user from a channel", ephemeral = true)
    public TextLocaleResponse kick(Guild guild, @RequestInfo Member member, @DiscordParameter(name = "name", description = "User to kick") Member kicked) throws NotInTheDroneException, MissingPermissionsException, KeyNotFoundException {
        VoiceChannel channel = validateMember(member);
        channel.getManager().putPermissionOverride(kicked, Collections.emptyList(), List.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT)).queue();

        if (kicked.getVoiceState().getChannel() != null) {
            AudioChannel to_kick_channel = kicked.getVoiceState().getChannel();
            if (to_kick_channel.getIdLong() == channel.getIdLong()) {
                guild.moveVoiceMember(kicked, null).queue();
                return new TextLocaleResponse("drone.kick.response", kicked.getEffectiveName());
            } else {
                return new TextLocaleResponse("drone.kick.response.error.not_in_channel", kicked.getEffectiveName());
            }
        } else {
            return new TextLocaleResponse("drone.kick.response.error.not_in_channel", kicked.getEffectiveName());
        }
    }
}
