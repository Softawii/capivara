package com.softawii.capivara.listeners;

import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.IButton;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICopyableChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@IGroup(name = "util", description = "Util Group", hidden = false)
public class UtilGroup {

    // Confirm or Deny Action
    public static final String confirmAction = "util-confirm-action";
    public static final String cancelAction  = "util-cancel-action";

    // Specific Action
    public static final String resetCategoryAction = "util-reset-category";

    @ICommand(name = "reset-category", description = "Quer redefinir todos os canais de uma categoria? Me use", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="target", description = "A categoria que será redefinida", type = OptionType.CHANNEL, required = true)
    public static void resetCategory(SlashCommandInteractionEvent event) {
        GuildChannelUnion target = event.getOption("target").getAsChannel();
        Category category;
        try {
            category = target.asCategory();
        } catch (IllegalStateException e) {
            event.reply("O canal selecionado não é uma categoria").setEphemeral(true).queue();
            return;
        }

        String confirmId = String.format("%s:%s:%s", confirmAction, resetCategoryAction, category.getId());
        String cancelId  = cancelAction;

        MessageEmbed messageEmbed = Utils.simpleEmbed("Você tem certeza disso?",
                "Você realmente quer resetar a categoria " + category.getAsMention() + "?",
                Color.ORANGE);

        Button successButton = Button.success(confirmId, "Sim! Pode continuar!");
        Button cancelButton  = Button.danger(cancelId, "Não, Deus me livre!!");

        event.replyEmbeds(messageEmbed).addActionRow(successButton, cancelButton).setEphemeral(true).queue();
    }

    @IButton(id = confirmAction)
    public static void confirm(ButtonInteractionEvent event) {
        System.out.println("Received confirm: " + event.getComponentId());
        /*
        Expected Args:
        - buttonId
        - actionId
        - ...
         */
        String[] args = event.getComponentId().split(":");

        if (args.length < 2) {
            event.reply("Argumentos inválidos -> " + event.getComponentId()).setEphemeral(true).queue();
            return;
        }

        String actionId = args[1];
        if (actionId.equals(resetCategoryAction)) {
            /*
            Expected Args:
            - buttonId
            - actionId
            - categoryId
             */
            String categoryId = args[2];
            Category category = event.getGuild().getCategoryById(categoryId);
            if (category == null) {
                MessageEmbed embed = Utils.simpleEmbed("Acharam a categoria antes de mim, temos um outro gerente na cidade",
                        "Verifica ai se deletaram a categoria de id'" + categoryId + "' ok??", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }

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
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                RestAction.accumulate(actions, Collectors.toList()).queue(obj -> {
                    MessageEmbed embed = Utils.simpleEmbed("Supimpa!", String.format("Consegui redefinir %d canais", actions.size() / 2), Color.GREEN);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }, throwable -> {
                    MessageEmbed embed = Utils.simpleEmbed("Poxa", "Algo de errado aconteceu, mas é possível que o processamento ainda esteja ocorrendo. Aguarde alguns instantes", Color.YELLOW);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                });
            }
        }
    }

    @IButton(id = cancelAction)
    public static void cancel(ButtonInteractionEvent event) {
        MessageEmbed embed = Utils.simpleEmbed("Cancelado", "Cancelado com sucesso!! Não tente isso de novo heinn...", Color.RED);
        event.editMessageEmbeds(embed).setActionRows().queue();
    }
}
