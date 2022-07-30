package com.softawii.capivara.core;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.services.VoiceDroneService;
import com.softawii.curupira.exceptions.MissingPermissionsException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.springframework.stereotype.Component;

@Component
public class DroneManager {

    private final VoiceDroneService voiceDroneService;

    private final String renameDrone = "drone-manager-rename";

    public DroneManager(VoiceDroneService voiceDroneService) {
        this.voiceDroneService = voiceDroneService;
    }

    public Modal checkRenameDrone(Guild guild, MessageChannelUnion channel, Member member, String customId) throws KeyNotFoundException, MissingPermissionsException {
        VoiceDrone voiceDrone = voiceDroneService.find(channel.getIdLong());

        if(voiceDrone.getOwnerId().equals(member.getIdLong()) || member.hasPermission(Permission.MANAGE_CHANNEL)) {
            Modal.Builder builder = Modal.create(customId, "Rename Room");
            builder.setTitle("Rename your Room!")
                    .addActionRow(
                            TextInput.create(renameDrone, "Room Name", TextInputStyle.SHORT).setValue(channel.getName()).setMaxLength(256).build()
                    );
            return builder.build();
        }
        throw new MissingPermissionsException();
    }

    public String renameDrone(ModalInteractionEvent event) {
        String newName = event.getValue(renameDrone).getAsString();
        event.getGuildChannel().getManager().setName(newName).complete();
        return newName;
    }
}
