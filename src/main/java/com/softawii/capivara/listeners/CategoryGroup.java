package com.softawii.capivara.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softawii.capivara.core.TemplateManager;
import com.softawii.capivara.entity.Template;
import com.softawii.capivara.exceptions.CategoryIsEmptyException;
import com.softawii.capivara.exceptions.TemplateAlreadyExistsException;
import com.softawii.capivara.exceptions.TemplateDoesNotExistException;
import com.softawii.capivara.utils.TemplateUtil;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IGroup(name = "Category", description = "Criar, aplicar e apagar templates de categorias", hidden = false)
public class CategoryGroup {

    @ISubGroup(name = "Template", description = "Criar, aplicar e apagar templates de categorias")
    public static class TemplateGroup {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        public static TemplateManager templateManager;

        public static final String confirmAction = "template-confirm-action";
        public static final String removeAction = "template-remove-action";
        public static final String cancelAction = "template-cancel-action";

        @ICommand(name = "create", description = "Criar um template a partir de uma categoria",
                permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "name", description = "O nome template a ser criado", type = OptionType.STRING, required = true)
        @IArgument(name = "category", description = "A categoria que será escaneada", type = OptionType.CHANNEL,
                required = true)
        public static void create(SlashCommandInteractionEvent event) {
            String name = event.getOption("name").getAsString();
            if (name.contains(":")) {
                event.replyEmbeds(Utils.nameContainsColon("O nome do template")).setEphemeral(true).queue();
                return;
            }

            GuildChannel channel = event.getOption("category").getAsChannel().asGuildMessageChannel();
            ChannelType channelType = channel.getType();
            if (channelType != ChannelType.CATEGORY) {
                event.replyEmbeds(TemplateUtil.channelIsNotCategory()).queue();
                return;
            }

            Category category = (Category) channel;
            Guild guild = event.getGuild();
            boolean exists = templateManager.existsById(guild.getIdLong(), name);
            if (exists) {
                MessageEmbed embed = Utils.simpleEmbed(
                        "Sinto muito",
                        "Já existe um template com esse nome", Color.ORANGE);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                Map<String, Integer> channelMap;
                try {
                    channelMap = TemplateUtil.scanCategory(category);
                } catch (CategoryIsEmptyException e) {
                    event.replyEmbeds(TemplateUtil.categoryIsEmpty()).queue();
                    return;
                }

                try {
                    String jsonString = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(channelMap);
                    Template template = templateManager.create(guild.getIdLong(), name, jsonString);
                    MessageEmbed embed = Utils.simpleEmbed("Supimpa", String.format("Template '%s' criado com sucesso", name), Color.GREEN);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
                } catch (TemplateAlreadyExistsException ex) {
                    MessageEmbed embed = Utils.simpleEmbed(
                            "Que coincidência, seria isso um sinal?",
                            "Parece que alguém acabou de criar um template com o mesmo nome.\n" +
                                    "Poderia tentar novamente com o mesmo nome ou nome diferente?", Color.ORANGE);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }
            }
        }

        @ICommand(name = "list", description = "Lista todos os templates do servidor",
                permissions = {Permission.ADMINISTRATOR})
        public static void list(SlashCommandInteractionEvent event) {
            Guild guild = event.getGuild();
            List<Template> templates = templateManager.findAllByGuildId(guild.getIdLong());

            if (templates.isEmpty()) {
                MessageEmbed embed = Utils.simpleEmbed(
                        "Não tenho nenhum template cadastrado",
                        "Cadastre um template e ele estará listado aqui.", Color.ORANGE);
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            List<MessageEmbed.Field> fields = new ArrayList<>(templates.size());
            templates.stream().sorted((t1, t2) -> {
                String name1 = t1.getTemplateKey().getName();
                String name2 = t2.getTemplateKey().getName();
                return name1.compareTo(name2);
            }).forEach(template -> {
                StringBuilder stringBuilder = new StringBuilder();
                String json = template.getJson();
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> map = OBJECT_MAPPER.readValue(json, Map.class);
                    map.forEach((channelName, channelType) -> {
                        stringBuilder.append(String.format("%s%n", channelName));
                    });

                    fields.add(new MessageEmbed.Field(template.getTemplateKey().getName(), stringBuilder.toString(), false));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
                    return;
                }
            });

            MessageEmbed embed = Utils.simpleEmbed("Templates", null, Color.GREEN, fields.toArray(new MessageEmbed.Field[]{}));
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }

        @ICommand(name = "update", description = "Atualiza um template a partir de uma categoria",
                permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "name", description = "O nome template que será atualizado", type = OptionType.STRING,
                required = true, hasAutoComplete = true)
        @IArgument(name = "category", description = "A categoria que será escaneada", type = OptionType.CHANNEL,
                required = true)
        public static void update(SlashCommandInteractionEvent event) {
            String name = event.getOption("name").getAsString();
            if (name.contains(":")) {
                event.replyEmbeds(Utils.nameContainsColon("O nome do template")).setEphemeral(true).queue();
                return;
            }

            GuildChannel channel = event.getOption("category").getAsChannel().asGuildMessageChannel();
            ChannelType channelType = channel.getType();
            if (channelType != ChannelType.CATEGORY) {
                event.replyEmbeds(TemplateUtil.channelIsNotCategory()).queue();
                return;
            }

            Category category = (Category) channel;
            Guild guild = event.getGuild();
            try {
                Template manager = templateManager.findById(guild.getIdLong(), name);
                Map<String, Integer> channelMap;

                try {
                    channelMap = TemplateUtil.scanCategory(category);
                } catch (CategoryIsEmptyException e) {
                    event.replyEmbeds(TemplateUtil.categoryIsEmpty()).queue();
                    return;
                }

                try {
                    String jsonString = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(channelMap);
                    Template template = templateManager.update(guild.getIdLong(), name, jsonString);
                    MessageEmbed embed = Utils.simpleEmbed("Magnífico", String.format("Template '%s' atualizado com sucesso", name), Color.GREEN);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
                } catch (TemplateDoesNotExistException e) {
                    e.printStackTrace();
                    MessageEmbed embed = Utils.simpleEmbed(
                            "É sério???",
                            "É possível que alguém possa ter apagado o template enquanto o update ocorria", Color.ORANGE);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }
            } catch (TemplateDoesNotExistException e) {
                e.printStackTrace();
                MessageEmbed embed = Utils.simpleEmbed(
                        "Não conheço esse indivíduo",
                        "Não encontrei nenhum template com esse nome para que possa ser atualizado", Color.ORANGE);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
        }

        @ICommand(name = "destroy", description = "Apaga um template", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "name", description = "O nome template que será destruído", type = OptionType.STRING,
                required = true, hasAutoComplete = true)
        public static void destroy(SlashCommandInteractionEvent event) {
            String name = event.getOption("name").getAsString();
            if (name.contains(":")) {
                event.replyEmbeds(Utils.nameContainsColon("O nome do template")).setEphemeral(true).queue();
                return;
            }

            String confirmId = String.format("%s:%s:%s", confirmAction, removeAction, name);
            String cancelId = cancelAction;

            MessageEmbed embed = Utils.simpleEmbed("Você tem certeza disso?",
                    "Você realmente quer deletar o template '" + name + "'?",
                    Color.ORANGE);

            Button successButton = Button.success(confirmId, "Sim! Pode continuar!");
            Button cancelButton = Button.danger(cancelId, "Não, Deus me livre!!");

            event.replyEmbeds(embed).addActionRow(successButton, cancelButton).setEphemeral(true).queue();
        }

        @ICommand(name = "apply", description = "Cria uma categoria a partir de um template",
                permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "name", description = "O nome template que utilizado", type = OptionType.STRING, required = true,
                hasAutoComplete = true)
        public static void apply(SlashCommandInteractionEvent event) {
            String name = event.getOption("name").getAsString();
            if (name.contains(":")) {
                event.replyEmbeds(Utils.nameContainsColon("O nome do template")).setEphemeral(true).queue();
                return;
            }

            Guild guild = event.getGuild();

            try {
                Template template = templateManager.findById(guild.getIdLong(), name);
                @SuppressWarnings("unchecked")
                Map<String, Integer> map = OBJECT_MAPPER.readValue(template.getJson(), Map.class);
                guild.createCategory(name).queue(category -> {
//                category.getManager().setPosition()
                    List<RestAction<? extends GuildChannel>> createChannelAction = new ArrayList<>();
                    map.forEach((channelName, channelType) -> {
                        if (ChannelType.fromId(channelType) == ChannelType.VOICE) {
                            createChannelAction.add(category.createVoiceChannel(channelName));
                        } else if (ChannelType.fromId(channelType) == ChannelType.TEXT) {
                            createChannelAction.add(category.createTextChannel(channelName));
                        } else if (ChannelType.fromId(channelType) == ChannelType.STAGE) {
                            createChannelAction.add(category.createStageChannel(channelName));
                        }
                    });

                    RestAction.accumulate(createChannelAction, Collectors.toList()).queue(voids -> {
                        MessageEmbed embed = Utils.simpleEmbed("Tá na mão", "A categoria deve estar por aí", Color.GREEN);
                        event.replyEmbeds(embed).setEphemeral(true).queue();
                    });
                });
            } catch (TemplateDoesNotExistException e) {
                MessageEmbed embed = Utils.simpleEmbed(
                        "Não conheço esse indivíduo",
                        "Não encontrei nenhum template com esse nome para que possa ser aplicado", Color.ORANGE);
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
            }
        }

        // SECTION BUTTON
        @IButton(id = confirmAction)
        public static void confirm(ButtonInteractionEvent event) {
            System.out.println("Received confirm: " + event.getComponentId());

        /*
        Expected Args:
        - buttonId
        - actionId
        - templateName
         */
            String[] args = event.getComponentId().split(":");

            if (args.length != 3) {
                event.reply("Argumentos inválidos -> " + event.getComponentId()).setEphemeral(true).queue();
                return;
            }

            String actionId = args[1];
            String templateName = args[2];
            Guild guild = event.getGuild();

            if (actionId.equals(removeAction)) {
                try {
                    templateManager.destroy(guild.getIdLong(), templateName);
                    MessageEmbed embed = Utils.simpleEmbed("Tchau tchau", String.format("O template '%s' foi destruído com sucesso", templateName), Color.GREEN);
                    event.editMessageEmbeds(embed).setActionRows().queue();
                } catch (TemplateDoesNotExistException e) {
                    MessageEmbed embed = Utils.simpleEmbed(
                            "Não conheço esse indivíduo",
                            "Não encontrei nenhum template com esse nome para que possa ser destruído", Color.ORANGE);
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }
            }
        }

        @IButton(id = cancelAction)
        public static void cancel(ButtonInteractionEvent event) {
            MessageEmbed embed = Utils.simpleEmbed("Cancelado", "Cancelado com sucesso!! Não tente isso de novo heinn...", Color.RED);

            event.editMessageEmbeds(embed).setActionRows().queue();
        }

        public static class AutoCompleter extends ListenerAdapter {
            @Override
            public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
                if (event.getGuild() == null) {
                    return;
                }

                String eventPath = event.getCommandPath();

                System.out.println(String.format("AutoCompleter: %s : %s", eventPath, event.getFocusedOption().getName()));

                if (eventPath.equals("template/apply") || eventPath.equals("template/destroy") || eventPath.equals("template/update")) {
                    String focusedKey = event.getFocusedOption().getName();
                    String focusedValue = event.getFocusedOption().getValue();

                    if (focusedKey.equals("name")) {
                        event.replyChoices(templateManager.autoCompleteTemplateName(event.getGuild().getIdLong(), focusedValue)).queue();
                    }
                }
            }
        }
    }

    // Confirm or Deny Action
    public static final String confirmAction = "util-confirm-action";
    public static final String cancelAction = "util-cancel-action";

    // Specific Action
    public static final String resetCategoryAction = "util-reset-category";

    @ICommand(name = "reset", description = "Quer redefinir todos os canais de uma categoria?", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "target", description = "A categoria que será redefinida", type = OptionType.CHANNEL, required = true)
    public static void reset(SlashCommandInteractionEvent event) {
        GuildChannelUnion target = event.getOption("target").getAsChannel();
        Category category;
        try {
            category = target.asCategory();
        } catch (IllegalStateException e) {
            event.reply("O canal selecionado não é uma categoria").setEphemeral(true).queue();
            return;
        }

        String confirmId = String.format("%s:%s:%s", confirmAction, resetCategoryAction, category.getId());
        String cancelId = cancelAction;

        MessageEmbed messageEmbed = Utils.simpleEmbed("Você tem certeza disso?",
                "Você realmente quer resetar a categoria " + category.getAsMention() + "?",
                Color.ORANGE);

        Button successButton = Button.success(confirmId, "Sim! Pode continuar!");
        Button cancelButton = Button.danger(cancelId, "Não, Deus me livre!!");

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
            event.reply("Ocorreu um erro e a nossa equipe já está investigando").setEphemeral(true).queue();
            throw new RuntimeException("Argumentos inválidos -> " + event.getComponentId());
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