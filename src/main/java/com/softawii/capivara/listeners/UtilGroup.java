package com.softawii.capivara.listeners;

import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICopyableChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@IGroup(name = "util", description = "Util Group", hidden = false)
public class UtilGroup {

    @ICommand(name = "reset", description = "Quer redefinir todos os canais de uma categoria? Me use", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="target", description = "A categoria que será redefinida", type = OptionType.CHANNEL, required = true)
    public static void reset(SlashCommandInteractionEvent event) {
        GuildChannel target = event.getOption("target").getAsGuildChannel();
        if (!(target instanceof net.dv8tion.jda.api.entities.Category)) {
            event.reply("O canal selecionado não é uma categoria").setEphemeral(true).queue();
            return;
        }

        net.dv8tion.jda.api.entities.Category category = (net.dv8tion.jda.api.entities.Category) target;
        List<GuildChannel> categoryChannels = category.getChannels();
        List<RestAction<?>> actions = new ArrayList<>();

        categoryChannels.forEach(guildChannel -> {
            if (guildChannel instanceof ICopyableChannel copyableChannel) {
                actions.add(event.getGuild().createCopyOfChannel(copyableChannel));
                actions.add(guildChannel.delete());
            }
        });

        if (actions.isEmpty()) {
            MessageEmbed embed = Utils.simpleEmbed("Eita", "Parece que não posso redefinir os canais da categoria", Color.ORANGE);
            event.replyEmbeds(embed).queue();
        } else {
            RestAction.accumulate(actions, Collectors.toList()).queue(obj -> {
                MessageEmbed embed = Utils.simpleEmbed("Supimpa!", String.format("Consegui redefinir %d canais", actions.size()), Color.GREEN);
                event.replyEmbeds(embed).queue();
            });
        }
    }
}
