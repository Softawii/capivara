package com.softawii.capivara.listeners;


import com.softawii.capivara.Main;
import com.softawii.capivara.core.EmbedManager;
import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.exceptions.KeyAlreadyInPackageException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.exceptions.UrlException;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.*;
import com.softawii.curupira.core.Curupira;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.sql.Select;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@IGroup(name = "echo", description = "Echo Group")
public class EchoGroup {

    // Row 1
    private static final String buttonSend          = "echo-send";
    private static final String buttonDeny          = "echo-deny";

    // Row 2
    private static final String buttonTitle         = "echo-title";
    private static final String modalTitle          = "echo-title-modal";
    private static final String buttonImage           = "echo-image";
    private static final String modalImage           = "echo-image-modal";

    // Row 3
    private static final String buttonNewField      = "echo-new-field";
    private static final String modalNewField       = "echo-new-field-modal";
    private static final String buttonRemoveField   = "echo-remove-field";
    private static final String menuRemoveField     = "echo-remove-field-menu";
    private static final String buttonEditField     = "echo-edit-field";
    private static final String menuEditField       = "echo-edit-field-menu";
    private static final String modalEditField      = "echo-edit-field-modal";

    private static final Logger LOGGER = LogManager.getLogger(EchoGroup.class);

    public static final String echoModal = "echo-modal";

    public static EmbedManager embedManager;

    @ICommand(name = "echo", description = "Quer enviar uma mensagem para o canal como se fosse o bot? Um anúncio? Pode me usar!", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name="target", description = "O canal que você quer enviar a mensagem", type = OptionType.CHANNEL, required = true)
    @IArgument(name="message", description = "Escreva uma mensagem ai pra ficar fora do Embed", type = OptionType.STRING, required = false)
    public static void echo(SlashCommandInteractionEvent event) {
        // Decode
        String message = event.getOption("message") != null ? event.getOption("message").getAsString() : null;
        OptionMapping targetOpt = event.getOption("target");

        List<ChannelType> textChannels = List.of(ChannelType.TEXT, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_NEWS_THREAD, ChannelType.NEWS);
        if( !textChannels.contains(targetOpt.getChannelType())) {
            MessageEmbed embed = Utils.simpleEmbed("Echo", "Este comando só funciona em canais de texto!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        if(message == null) message = "Vê se a mensagem está boa pra você!";

        // Generating Embed Model
        Map.Entry<String, EmbedManager.EmbedHandler> init = embedManager.init();
        init.getValue().setMessage(message);
        init.getValue().setTarget(event.getOption("target").getAsGuildChannel());

        event.reply(message)
                .addEmbeds(init.getValue().build())
                .addActionRows(EchoGroup.embedEditor(init.getKey()))
                .setEphemeral(true)
                .queue();
    }

    @IButton(id=buttonSend)
    public static void send(ButtonInteractionEvent event) {
        // Extracting ID
        /*
              Format: buttonSend:<id>
         */
        String id = event.getComponentId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        GuildChannel target = embedHandler.getTarget();

        MessageAction messageAction;
        if(target instanceof TextChannel textChannel) {
            messageAction = textChannel.sendMessage(embedHandler.getMessage()).setEmbeds(embedHandler.build());

        } else if(target instanceof NewsChannel newsChannel) {
            messageAction = newsChannel.sendMessage(embedHandler.getMessage()).setEmbeds(embedHandler.build());
        } else {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "O canal vinculado não é de um tipo suportado!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        if(embedHandler.getActiveRows() != null && !embedHandler.getActiveRows().isEmpty()) {
            messageAction.setActionRows(embedHandler.getActiveRows());
        }
        messageAction.queue();

        MessageEmbed embed = Utils.simpleEmbed("Embed enviado!", "Embed enviado com sucesso!", Color.GREEN);
        event.editMessage("Tudo ok!").setEmbeds(embed).setActionRows().queue();
    }

    @IButton(id=buttonDeny)
    public static void deny(ButtonInteractionEvent event) {
        // Extracting ID
        /*
              Format: buttonSend:<id>
         */
        String id = event.getComponentId().split(":")[1];
        embedManager.destroy(id);

        MessageEmbed embed = Utils.simpleEmbed("Embed cancelado!", "Embed cancelado com sucesso!", Color.GREEN);
        event.editMessage("Cancelei!!").setEmbeds(embed).setActionRows().queue();
    }

    //region Title / Description

    @IButton(id=buttonTitle)
    public static void title(ButtonInteractionEvent event) {
        // Extracting ID
        // Format: buttonSend:<id>
        String id = event.getComponentId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        // Generating Embed Model
        Curupira curupira = Main.context.getBean(Curupira.class);
        Modal.Builder modal = curupira.getModal(modalTitle);
        modal.setId(modal.getId() + ":" + id);

        // TODO: Fill Modal with current data

        event.replyModal(modal.build()).queue();
    }

    @IModal(id=modalTitle, title = "Vamos anunciar o que??", description = "Digite o que você quer anunciar", textInputs = {
            @IModal.ITextInput(id = "titulo", label = "Título do Anúncio!", style = TextInputStyle.SHORT, placeholder = "Escolha um nome impactante!", required = true, minLength = 1, maxLength = 256),
            @IModal.ITextInput(id = "mensagem", label = "Mensagem do Anúncio!", style = TextInputStyle.PARAGRAPH, placeholder = "Que mensagem importante... digita pro seu pai...", required = true, minLength = 1, maxLength = 4000)
    })
    public static void modalTitle(ModalInteractionEvent event) {
        String title = event.getValue("titulo").getAsString();
        String message = event.getValue("mensagem").getAsString();

        // Extracting ID
        // Format: echoModal:<id>
        String id = event.getModalId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        try {
            embedHandler.setTitle(title);
            embedHandler.setDescription(message);
        } catch (FieldLengthException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "O título ou a mensagem são muito longos!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        event.editMessageEmbeds(embedHandler.build()).setActionRows(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region Image

    @IButton(id=buttonImage)
    public static void image(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        // Generating Embed Model
        Curupira curupira = Main.context.getBean(Curupira.class);
        Modal.Builder modal = curupira.getModal(modalImage);
        modal.setId(modal.getId() + ":" + id);

        event.replyModal(modal.build()).queue();
    }

    @IModal(id=modalImage, title = "Fala sobre sua sessão ai!", description = "Tenho certeza que é muito interessante...", textInputs = {
            @IModal.ITextInput(id = "url", label = "Joga a url ai, fazendo favor!", style = TextInputStyle.SHORT, placeholder = "https://i.pinimg.com/564x/ab/21/d5/ab21d5b71ba7b3b74947c3e5656c6aee--room-to-room-velvet.jpg/", required = true, minLength = 1, maxLength = 256),
    })
    public static void modalImagem(ModalInteractionEvent event) {
        String url = event.getValue("url").getAsString();

        // Extracting ID
        // Format: modalNewField:<id>
        String id = event.getModalId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        try {
            embedHandler.setImage(url);
        } catch (UrlException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "A URL não é válida!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        event.editMessageEmbeds(embedHandler.build()).setActionRows(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region New Field

    @IButton(id=buttonNewField)
    public static void newField(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        // Generating Embed Model
        Curupira curupira = Main.context.getBean(Curupira.class);
        Modal.Builder modal = curupira.getModal(modalNewField);
        modal.setId(modal.getId() + ":" + id);

        // TODO: Fill Modal with current data

        event.replyModal(modal.build()).queue();
    }

    @IModal(id=modalNewField, title = "Fala sobre sua sessão ai!", description = "Tenho certeza que é muito interessante...", textInputs = {
            @IModal.ITextInput(id = "name", label = "Título do Sessão!", style = TextInputStyle.SHORT, placeholder = "Escolha um nome impactante!", required = true, minLength = 1, maxLength = 256),
            @IModal.ITextInput(id = "value", label = "Mensagem do Sessão!", style = TextInputStyle.PARAGRAPH, placeholder = "Que mensagem importante... digita pro seu pai...", required = true, minLength = 1, maxLength = 1024)
    })
    public static void modalNewField(ModalInteractionEvent event) {
        String name = event.getValue("name").getAsString();
        String value = event.getValue("value").getAsString();

        // Extracting ID
        // Format: modalNewField:<id>
        String id = event.getModalId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        try {
            embedHandler.addField(new MessageEmbed.Field(name, value, false));
        } catch (FieldLengthException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "O título ou a mensagem são muito longos!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        event.editMessageEmbeds(embedHandler.build()).setActionRows(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region Edit Field

    @IButton(id=buttonEditField)
    public static void editField(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        SelectMenu.Builder builder;
        try {
            builder = EchoGroup.embedField(id, menuEditField);
            builder.setRequiredRange(1, 1);
            builder.setPlaceholder("Escolha um campo para editar!");
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        event.editComponents(ActionRow.of(builder.build())).queue();
    }

    @IMenu(id=menuEditField)
    public static void editFieldMenu(SelectMenuInteractionEvent event) {
        SelectOption selectOption = event.getSelectedOptions().get(0);
        String       id           = event.getComponentId().split(":")[1];

        int index = Integer.parseInt(selectOption.getValue());

        if(index >= 0) {
            EmbedManager.EmbedHandler embedHandler;
            try {
                embedHandler = embedManager.get(id);
            } catch (KeyNotFoundException e) {
                MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            Curupira curupira = Main.context.getBean(Curupira.class);
            Modal.Builder modal = curupira.getModal(modalEditField);
            modal.setId(modal.getId() + ":" + id + ":" + index);

            event.replyModal(modal.build()).queue();
        } else {
            event.editComponents(EchoGroup.embedEditor(id)).queue();
        }
    }

    @IModal(id=modalEditField, title = "Fala sobre sua sessão ai!", description = "Tenho certeza que é muito interessante...", textInputs = {
            @IModal.ITextInput(id = "name", label = "Título do Sessão!", style = TextInputStyle.SHORT, placeholder = "Escolha um nome impactante!", required = true, minLength = 1, maxLength = 256),
            @IModal.ITextInput(id = "value", label = "Mensagem do Sessão!", style = TextInputStyle.PARAGRAPH, placeholder = "Que mensagem importante... digita pro seu pai...", required = true, minLength = 1, maxLength = 1024)
    })
    public static void modalEditField(ModalInteractionEvent event) {

        // Extracting ID
        // Format: modalNewField:<id>:<index>
        String id = event.getModalId().split(":")[1];
        int index = Integer.parseInt(event.getModalId().split(":")[2]);

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        String name = event.getValue("name").getAsString();
        String value = event.getValue("value").getAsString();
        MessageEmbed.Field editedField = new MessageEmbed.Field(name, value, false);
        embedHandler.setField(editedField, index);

        event.editMessageEmbeds(embedHandler.build()).setActionRows(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region Remove Field

    @IButton(id=buttonRemoveField)
    public static void removeFieldButton(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        SelectMenu.Builder builder;
        try {
            builder = EchoGroup.embedField(id, menuRemoveField);
            builder.setRequiredRange(1, 1);
            builder.setPlaceholder("Escolha um campo para remover!");
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        event.editComponents(ActionRow.of(builder.build())).queue();
    }

    @IMenu(id=menuRemoveField)
    public static void removeFieldMenu(SelectMenuInteractionEvent event) {
        SelectOption selectOption = event.getSelectedOptions().get(0);
        String       id           = event.getComponentId().split(":")[1];

        int index = Integer.parseInt(selectOption.getValue());

        if(index >= 0) {
            EmbedManager.EmbedHandler embedHandler;
            try {
                embedHandler = embedManager.get(id);
                embedHandler.removeField(index);
            } catch (KeyNotFoundException e) {
                MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }
            event.editMessageEmbeds(embedHandler.build()).setActionRows(EchoGroup.embedEditor(id)).queue();
        } else {
            event.editComponents(EchoGroup.embedEditor(id)).queue();
        }

    }

    //endregion

    //region Utils
    public static List<ActionRow> embedEditor(String id) {

        // Send / Cancel Row
        Button send =  Button.success(buttonSend + ":" + id, "Enviar Embed");
        Button deny =  Button.danger(buttonDeny + ":" + id,  "Cancelar Embed");

        // Edit Title / Description Row
        Button title        = Button.primary(buttonTitle + ":" + id,"Editar Título ou Descrição");
        Button imageField = Button.primary(buttonImage + ":" + id,"Nova Imagem");

        // Add Field / Remove Field / Edit Field
        Button newField     = Button.secondary(buttonNewField + ":" + id,    "Novo Campo");
        Button deleteField  = Button.secondary(buttonRemoveField + ":" + id, "Remover Campo");
        Button editField    = Button.secondary(buttonEditField + ":" + id,   "Editar Campo");

        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(title, imageField));
        actionRows.add(ActionRow.of(newField, deleteField, editField));
        actionRows.add(ActionRow.of(send, deny));

        return actionRows;
    }

    public static SelectMenu.Builder embedField(String id, String menuId) throws KeyNotFoundException {
        SelectMenu.Builder builder = SelectMenu.create(menuId + ":" + id);
        EmbedManager.EmbedHandler embedHandler = embedManager.get(id);

        List<String> options = embedHandler.getFieldNames().stream().map(name -> {
            if (name.length() > 100) return name.substring(0, 95) + "...";
            else return name;
        }).toList();

        builder.addOption("Nenhum", "-1");

        for (int i = 0; i < options.size(); i++) {
            builder.addOption(options.get(i), String.valueOf(i));
        }

        return builder;
    }

    //endregion
}
