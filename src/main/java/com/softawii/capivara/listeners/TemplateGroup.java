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
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IGroup(name = "Template", description = "Criar, aplicar e apagar templates de categorias", hidden = false)
public class TemplateGroup {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static TemplateManager templateManager;

    @ICommand(name = "create", description = "Criar um template a partir de uma categoria", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "O nome template a ser criado", type = OptionType.STRING, required = true)
    @IArgument(name = "category", description = "A categoria que será escaneada", type = OptionType.CHANNEL, required = true)
    public static void create(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();
        GuildChannel channel = event.getOption("category").getAsGuildChannel();
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
            event.replyEmbeds(embed).queue();
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
                event.replyEmbeds(embed).queue();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
            } catch (TemplateAlreadyExistsException ex) {
                MessageEmbed embed = Utils.simpleEmbed(
                        "Que coincidência, seria isso um sinal?",
                        "Parece que alguém acabou de criar um template com o mesmo nome.\n" +
                                "Poderia tentar novamente com o mesmo nome ou nome diferente?", Color.ORANGE);
                event.replyEmbeds(embed).queue();
            }
        }
    }

    @ICommand(name = "list", description = "Lista todos os templates do servidor", permissions = {Permission.ADMINISTRATOR})
    public static void list(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        List<Template> templates = templateManager.findAllByGuildId(guild.getIdLong());

        if (templates.isEmpty()) {
            MessageEmbed embed = Utils.simpleEmbed(
                    "Não tenho nenhum template cadastrado",
                    "Cadastre um template e ele estará listado aqui.", Color.ORANGE);
            event.replyEmbeds(embed).queue();
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
        event.replyEmbeds(embed).queue();
    }

    @ICommand(name = "update", description = "Atualiza um template a partir de uma categoria", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "O nome template que será atualizado", type = OptionType.STRING, required = true)
    @IArgument(name = "category", description = "A categoria que será escaneada", type = OptionType.CHANNEL, required = true)
    public static void update(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();
        GuildChannel channel = event.getOption("channel").getAsGuildChannel();
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
                event.replyEmbeds(embed).queue();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
            } catch (TemplateDoesNotExistException e) {
                e.printStackTrace();
                MessageEmbed embed = Utils.simpleEmbed(
                        "É sério???",
                        "É possível que alguém possa ter apagado o template enquanto o update ocorria", Color.ORANGE);
                event.replyEmbeds(embed).queue();
            }
        } catch (TemplateDoesNotExistException e) {
            e.printStackTrace();
            MessageEmbed embed = Utils.simpleEmbed(
                    "Não conheço esse indivíduo",
                    "Não encontrei nenhum template com esse nome para que possa ser atualizado", Color.ORANGE);
            event.replyEmbeds(embed).queue();
        }
    }

    @ICommand(name = "destroy", description = "Apaga um template", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "O nome template que será destruído", type = OptionType.STRING, required = true)
    public static void destroy(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();
        Guild guild = event.getGuild();

        try {
            templateManager.destroy(guild.getIdLong(), name);
            MessageEmbed embed = Utils.simpleEmbed("Tchau tchau", String.format("O template '%s' foi destruído com sucesso", name), Color.GREEN);
            event.replyEmbeds(embed).queue();
        } catch (TemplateDoesNotExistException e) {
            MessageEmbed embed = Utils.simpleEmbed(
                    "Não conheço esse indivíduo",
                    "Não encontrei nenhum template com esse nome para que possa ser destruído", Color.ORANGE);
            event.replyEmbeds(embed).queue();
        }
    }

    @ICommand(name = "apply", description = "Cria uma categoria a partir de um template", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "O nome template que utilizado", type = OptionType.STRING, required = true)
    public static void apply(SlashCommandInteractionEvent event) {
        String name = event.getOption("name").getAsString();
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
                    event.replyEmbeds(embed).queue();
                });
            });
        } catch (TemplateDoesNotExistException e) {
            MessageEmbed embed = Utils.simpleEmbed(
                    "Não conheço esse indivíduo",
                    "Não encontrei nenhum template com esse nome para que possa ser aplicado", Color.ORANGE);
            event.replyEmbeds(embed).queue();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            event.replyEmbeds(Utils.parseObjectToJsonError()).queue();
        }

    }
}
