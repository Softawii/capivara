package com.softawii.capivara.listeners;


import com.softawii.capivara.core.EmbedManager;
import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.exceptions.UrlException;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.*;
import com.softawii.curupira.core.Curupira;
import io.micronaut.context.annotation.Context;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IGroup(name = "echo", description = "Echo Group")
@Context
public class EchoGroup {

    public static final String buttonEditMessage = "echo-edit-message";
    public static final String modalEditMessage  = "echo-edit-message-modal";
    //endregion
    public static final String buttonRemoveMessage = "echo-remove-message";
    //region Send/Cancel Constants
    private static final String buttonSend = "echo-send";
    private static final String buttonDeny = "echo-deny";
    //region Title/Description Constants
    private static final String buttonTitle = "echo-title";

    //endregion
    private static final String modalTitle  = "echo-title-modal";
    private static final String buttonImage = "echo-image";
    private static final String modalImage  = "echo-image-modal";
    //region Fields Constants
    private static final String buttonNewField    = "echo-new-field";
    private static final String modalNewField     = "echo-new-field-modal";
    private static final String buttonRemoveField = "echo-remove-field";
    private static final String menuRemoveField   = "echo-remove-field-menu";
    //endregion Constants

    //region Message Constants
    private static final String buttonEditField   = "echo-edit-field";
    private static final String menuEditField     = "echo-edit-field-menu";
    private static final String modalEditField    = "echo-edit-field-modal";

    //endregion
    private static final Logger LOGGER = LogManager.getLogger(EchoGroup.class);

    private static EmbedManager embedManager;
    private static Curupira     curupira;

    public EchoGroup(Curupira curupira, EmbedManager embedManager) {
        EchoGroup.curupira = curupira;
        EchoGroup.embedManager = embedManager;
    }

    @ICommand(name = "echo",
              description = "Quer enviar uma mensagem para o canal como se fosse o bot? Um anúncio? Pode me usar!",
              permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "target", description = "O canal que você quer enviar a mensagem", type = OptionType.CHANNEL,
               required = true)
    @IArgument(name = "message", description = "Escreva uma mensagem ai pra ficar fora do Embed",
               type = OptionType.STRING, required = false)
    public static void echo(SlashCommandInteractionEvent event) {
        // Decode
        String        message   = event.getOption("message") != null ? event.getOption("message").getAsString() : null;
        OptionMapping targetOpt = event.getOption("target");

        List<ChannelType> textChannels = List.of(ChannelType.TEXT, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_NEWS_THREAD, ChannelType.NEWS);
        if (!textChannels.contains(targetOpt.getChannelType())) {
            MessageEmbed embed = Utils.simpleEmbed("Echo", "Este comando só funciona em canais de texto!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        // Generating Embed Model
        Map.Entry<String, EmbedManager.EmbedHandler> init = embedManager.init();
        init.getValue().setMessage(message);
        init.getValue().setTarget(event.getOption("target").getAsChannel().asGuildMessageChannel());

        if (message != null) {
            event.reply(message)
                    .addEmbeds(init.getValue().build())
                    .setComponents(EchoGroup.embedEditor(init.getKey()))
                    .setEphemeral(true)
                    .queue();
        } else {
            event.replyEmbeds(init.getValue().build())
                    .setComponents(EchoGroup.embedEditor(init.getKey()))
                    .setEphemeral(true)
                    .queue();
        }
    }

    @IButton(id = buttonSend)
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

        MessageCreateAction messageAction;
        if (target instanceof TextChannel textChannel) {
            if (embedHandler.getMessage() != null)
                messageAction = textChannel.sendMessage(embedHandler.getMessage()).setEmbeds(embedHandler.build());
            else messageAction = textChannel.sendMessageEmbeds(embedHandler.build());
        } else if (target instanceof NewsChannel newsChannel) {
            if (embedHandler.getMessage() != null)
                messageAction = newsChannel.sendMessage(embedHandler.getMessage()).setEmbeds(embedHandler.build());
            else messageAction = newsChannel.sendMessageEmbeds(embedHandler.build());
        } else {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "O canal vinculado não é de um tipo suportado!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        if (embedHandler.getActiveRows() != null && !embedHandler.getActiveRows().isEmpty()) {
            messageAction.setComponents(embedHandler.getActiveRows());
        }
        messageAction.queue();

        MessageEmbed embed = Utils.simpleEmbed("Embed enviado!", "Embed enviado com sucesso!", Color.GREEN);
        event.editMessage("Tudo ok!").setEmbeds(embed).setComponents().queue();
    }

    @IButton(id = buttonDeny)
    public static void deny(ButtonInteractionEvent event) {
        // Extracting ID
        /*
              Format: buttonSend:<id>
         */
        String id = event.getComponentId().split(":")[1];
        embedManager.destroy(id);

        MessageEmbed embed = Utils.simpleEmbed("Embed cancelado!", "Embed cancelado com sucesso!", Color.GREEN);
        event.editMessage("Cancelei!!").setEmbeds(embed).setComponents().queue();
    }

    //region Title / Description

    @IButton(id = buttonTitle)
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
        Modal.Builder modal    = curupira.getModal(modalTitle);
        modal.setId(modal.getId() + ":" + id);

        // TODO: Fill Modal with current data

        event.replyModal(modal.build()).queue();
    }

    @IModal(id = modalTitle, title = "Vamos anunciar o que??", description = "Digite o que você quer anunciar",
            textInputs = {
                    @IModal.ITextInput(id = "titulo", label = "Título do Anúncio!", style = TextInputStyle.SHORT,
                                       placeholder = "Escolha um nome impactante!", required = true, minLength = 1,
                                       maxLength = 256),
                    @IModal.ITextInput(id = "mensagem", label = "Mensagem do Anúncio!",
                                       style = TextInputStyle.PARAGRAPH,
                                       placeholder = "Que mensagem importante... digita pro seu pai...",
                                       required = true, minLength = 1, maxLength = 4000)
            })
    public static void modalTitle(ModalInteractionEvent event) {
        String title   = event.getValue("titulo").getAsString();
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

        event.editMessageEmbeds(embedHandler.build()).setComponents(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region Image

    @IButton(id = buttonImage)
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
        Modal.Builder modal    = curupira.getModal(modalImage);
        modal.setId(modal.getId() + ":" + id);

        event.replyModal(modal.build()).queue();
    }

    @IModal(id = modalImage, title = "Fala sobre sua sessão ai!",
            description = "Tenho certeza que é muito interessante...", textInputs = {
            @IModal.ITextInput(id = "url", label = "Joga a url ai, fazendo favor!", style = TextInputStyle.SHORT,
                               placeholder = "https://i.pinimg.com/564x/ab/21/d5/ab21d5b71ba7b3b74947c3e5656c6aee--room-to-room-velvet.jpg/",
                               required = true, minLength = 1, maxLength = 256),
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
        event.editMessageEmbeds(embedHandler.build()).setComponents(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region New Field

    @IButton(id = buttonNewField)
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
        Modal.Builder modal    = curupira.getModal(modalNewField);
        modal.setId(modal.getId() + ":" + id);

        // TODO: Fill Modal with current data

        event.replyModal(modal.build()).queue();
    }

    @IModal(id = modalNewField, title = "Fala sobre sua sessão ai!",
            description = "Tenho certeza que é muito interessante...", textInputs = {
            @IModal.ITextInput(id = "name", label = "Título do Sessão!", style = TextInputStyle.SHORT,
                               placeholder = "Escolha um nome impactante!", required = true, minLength = 1,
                               maxLength = 256),
            @IModal.ITextInput(id = "value", label = "Mensagem do Sessão!", style = TextInputStyle.PARAGRAPH,
                               placeholder = "Que mensagem importante... digita pro seu pai...", required = true,
                               minLength = 1, maxLength = 1024)
    })
    public static void modalNewField(ModalInteractionEvent event) {
        String name  = event.getValue("name").getAsString();
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
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Vcê está tentando botar muitos campos!!!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }
        event.editMessageEmbeds(embedHandler.build()).setComponents(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region Edit Field

    @IButton(id = buttonEditField)
    public static void editField(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        StringSelectMenu.Builder builder;
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

    @IMenu(id = menuEditField)
    public static void editFieldMenu(StringSelectInteractionEvent event) {
        SelectOption selectOption = event.getSelectedOptions().get(0);
        String       id           = event.getComponentId().split(":")[1];

        int index = Integer.parseInt(selectOption.getValue());

        if (index >= 0) {
            EmbedManager.EmbedHandler embedHandler;
            try {
                embedHandler = embedManager.get(id);
            } catch (KeyNotFoundException e) {
                MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            Modal.Builder modal    = curupira.getModal(modalEditField);
            modal.setId(modal.getId() + ":" + id + ":" + index);

            event.replyModal(modal.build()).queue();
        } else {
            event.editComponents(EchoGroup.embedEditor(id)).queue();
        }
    }

    @IModal(id = modalEditField, title = "Fala sobre sua sessão ai!",
            description = "Tenho certeza que é muito interessante...", textInputs = {
            @IModal.ITextInput(id = "name", label = "Título do Sessão!", style = TextInputStyle.SHORT,
                               placeholder = "Escolha um nome impactante!", required = true, minLength = 1,
                               maxLength = 256),
            @IModal.ITextInput(id = "value", label = "Mensagem do Sessão!", style = TextInputStyle.PARAGRAPH,
                               placeholder = "Que mensagem importante... digita pro seu pai...", required = true,
                               minLength = 1, maxLength = 1024)
    })
    public static void modalEditField(ModalInteractionEvent event) {

        // Extracting ID
        // Format: modalNewField:<id>:<index>
        String id    = event.getModalId().split(":")[1];
        int    index = Integer.parseInt(event.getModalId().split(":")[2]);

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        String             name        = event.getValue("name").getAsString();
        String             value       = event.getValue("value").getAsString();
        MessageEmbed.Field editedField = new MessageEmbed.Field(name, value, false);
        embedHandler.setField(editedField, index);

        event.editMessageEmbeds(embedHandler.build()).setComponents(EchoGroup.embedEditor(id)).queue();
    }

    //endregion

    //region Remove Field

    @IButton(id = buttonRemoveField)
    public static void removeFieldButton(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        StringSelectMenu.Builder builder;
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

    @IMenu(id = menuRemoveField)
    public static void removeFieldMenu(StringSelectInteractionEvent event) {
        SelectOption selectOption = event.getSelectedOptions().get(0);
        String       id           = event.getComponentId().split(":")[1];

        int index = Integer.parseInt(selectOption.getValue());

        if (index >= 0) {
            EmbedManager.EmbedHandler embedHandler;
            try {
                embedHandler = embedManager.get(id);
                embedHandler.removeField(index);
            } catch (KeyNotFoundException e) {
                MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }
            event.editMessageEmbeds(embedHandler.build()).setComponents(EchoGroup.embedEditor(id)).queue();
        } else {
            event.editComponents(EchoGroup.embedEditor(id)).queue();
        }

    }

    //endregion

    //region Message

    @IButton(id = buttonEditMessage)
    public static void editMessage(ButtonInteractionEvent event) {
        String        id       = event.getComponentId().split(":")[1];
        Modal.Builder builder  = curupira.getModal(modalEditMessage).setId(modalEditMessage + ":" + id);
        event.replyModal(builder.build()).queue();
    }

    @IModal(id = modalEditMessage, title = "Escreve sua mensagem ai!", description = "Escreve ai, bora!", textInputs = {
            @IModal.ITextInput(id = "message", label = "Mensagem", style = TextInputStyle.SHORT,
                               placeholder = "FrasesDeEfeito.com.br", required = true, minLength = 1, maxLength = 256),
    })
    public static void modalMessage(ModalInteractionEvent event) {
        String message = event.getValue("message").getAsString();

        // Extracting ID
        // Format: modalEditMessage:<id>
        String id = event.getModalId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        embedHandler.setMessage(message);
        event.editMessage(message).queue();
    }


    @IButton(id = buttonRemoveMessage)
    public static void removeMessage(ButtonInteractionEvent event) {
        String id = event.getComponentId().split(":")[1];

        EmbedManager.EmbedHandler embedHandler = null;
        try {
            embedHandler = embedManager.get(id);
        } catch (KeyNotFoundException e) {
            MessageEmbed embed = Utils.simpleEmbed("Algo errado aqui! Mil perdões", "Embed não encontrado no nosso sistema, vai ter que fazer de novo!", Color.RED);
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        embedHandler.setMessage(null);

        event.editMessage("Você removeu sua mensagem! (Se quiser adicionar outra só clicar no botão de editar)").queue();
    }

    //endregion

    //region Utils
    public static List<ActionRow> embedEditor(String id) {

        // Send / Cancel Row
        Button send = Button.success(buttonSend + ":" + id, "Enviar Embed");
        Button deny = Button.danger(buttonDeny + ":" + id, "Cancelar Embed");

        // Edit Title / Description Row
        Button title      = Button.primary(buttonTitle + ":" + id, "Editar Título ou Descrição");
        Button imageField = Button.primary(buttonImage + ":" + id, "Nova Imagem");

        // Add Field / Remove Field / Edit Field
        Button newField    = Button.secondary(buttonNewField + ":" + id, "Novo Campo");
        Button deleteField = Button.secondary(buttonRemoveField + ":" + id, "Remover Campo");
        Button editField   = Button.secondary(buttonEditField + ":" + id, "Editar Campo");

        // Edit / Remove Message
        Button editMessage   = Button.secondary(buttonEditMessage + ":" + id, "Editar Mensagem");
        Button removeMessage = Button.secondary(buttonRemoveMessage + ":" + id, "Remover Mensagem");

        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(title, imageField));
        actionRows.add(ActionRow.of(newField, deleteField, editField));
        actionRows.add(ActionRow.of(editMessage, removeMessage));
        actionRows.add(ActionRow.of(send, deny));

        return actionRows;
    }

    public static StringSelectMenu.Builder embedField(String id, String menuId) throws KeyNotFoundException {
        StringSelectMenu.Builder  builder      = StringSelectMenu.create(menuId + ":" + id);
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
