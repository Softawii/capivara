package com.softawii.capivara.listeners;

import com.softawii.capivara.services.TwitterParserConfigService;
import com.softawii.curupira.annotations.IButton;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@IGroup(name = "twitter", description = "Transformação de links do Twitter automática", hidden = false)
@Component
public class TwitterGroup {

    public static final String                     deleteBotTwitterMessage = "twitter-bot-message-delete";
    private static      TwitterParserConfigService service;

    @Autowired
    @SuppressWarnings("unused")
    public void setCurupira(TwitterParserConfigService service) {
        TwitterGroup.service = service;
    }

    @ICommand(name = "enable", description = "Ativa o serviço de transformação de links do Twitter automática",
              permissions = {Permission.ADMINISTRATOR})
    @SuppressWarnings("unused")
    public static void enable(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (service.isEnabled(guildId)) {
            event.reply("O serviço já está ativado").setEphemeral(true).queue();
            return;
        }

        service.enable(guildId);
        event.reply("O serviço foi está ativado").setEphemeral(true).queue();
    }

    @ICommand(name = "disable", description = "Desativa o serviço de transformação de links do Twitter automática",
              permissions = {Permission.ADMINISTRATOR})
    @SuppressWarnings("unused")
    public static void disable(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (!service.isEnabled(guildId)) {
            event.reply("O serviço não está ativado").setEphemeral(true).queue();
            return;
        }

        service.disable(guildId);
        event.reply("O serviço foi está ativado").setEphemeral(true).queue();
    }

    @IButton(id = deleteBotTwitterMessage)
    @SuppressWarnings("unused")
    public static void delete(ButtonInteractionEvent event) {
        // Format: ButtonID:Owner
        String ownerId      = event.getComponentId().split(":")[1];
        String messageOwner = event.getMember().getId();

        MessageChannelUnion channel = event.getChannel();

        if (!messageOwner.equals(ownerId)) {
            event.reply("Você não pode apagar essa mensagem").setEphemeral(true).queue();
            return;
        }

        event.reply("Mensagem apagada").setEphemeral(true).queue();
        channel.deleteMessageById(event.getMessageId()).queue();
    }

    public static Button generateDeleteButton(long authorId) {
        return Button.danger(String.format("%s:%s", deleteBotTwitterMessage, authorId), "Apagar");
    }
}
