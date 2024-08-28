package com.softawii.capivara.controller;

import com.softawii.capivara.core.DroneManager;
import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.*;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.commands.DiscordParameter;
import com.softawii.curupira.v2.annotations.interactions.DiscordButton;
import com.softawii.curupira.v2.annotations.interactions.DiscordModal;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
@DiscordController(parent = "voice", value = "dynamic", description = "Voice Controller", permissions = Permission.ADMINISTRATOR)
public class VoiceDynamicController {

    public static final String configModal         = "voice-dynamic-config-modal";
    public static final String generateConfigModal = "voice-dynamic-generate-config-modal";
    public static final String droneConfigButton   = "voice-dynamic-drone-config-button";
    public static final String droneConfigModal    = "voice-dynamic-drone-config-modal";
    public static final String droneHideShow       = "voice-dynamic-drone-hide-show";
    public static final String dronePublicPrivate  = "voice-dynamic-drone-public-private";
    public static final String dronePermTemp       = "voice-dynamic-drone-permanent-temporary";
    public static final String droneClaim          = "voice-dynamic-drone-claim";

    private final VoiceManager voiceManager;
    private final DroneManager droneManager;

    public VoiceDynamicController(VoiceManager voiceManager, DroneManager droneManager) {
        this.voiceManager = voiceManager;
        this.droneManager = droneManager;
    }

    @DiscordCommand(name = "set", description = "set a category for dynamic voice channels")
    public void set(
            SlashCommandInteractionEvent event, Guild guild, LocalizationManager localization,
            @LocaleType DiscordLocale locale, @DiscordParameter(name = "category", description = "Category to add dynamic channel") GuildChannelUnion channel) throws ExistingDynamicCategoryException, InvalidChannelTypeException {

        if(channel.getType() != ChannelType.CATEGORY) {
            throw new InvalidChannelTypeException();
        }

        Category category = channel.asCategory();
        VoiceHive hive = this.voiceManager.setDynamicCategory(category);
        VoiceChannel hiveChannel = guild.getVoiceChannelById(hive.getVoiceId());

        // Getting callback texts
        String title = localization.getLocalizedString("voice.dynamic.set.success", locale);
        String description = localization.getLocalizedString("voice.dynamic.set.success.description", locale, hiveChannel.getAsMention());

        MessageEmbed embed = Utils.simpleEmbed(title, description, Color.GREEN);

        event.replyEmbeds(embed)
                .addActionRow(Button.primary(generateConfigModal + ":" + hiveChannel.getParentCategoryIdLong(), "Open Settings"))
                .setEphemeral(true)
                .queue();
    }

    @DiscordCommand(name = "unset", description = "unset the category for dynamic voice channels", ephemeral = true)
    public TextLocaleResponse unset(
            Guild guild,
            @DiscordParameter(name = "category", description = "Category to add dynamic channel") GuildChannelUnion channel) throws KeyNotFoundException, InvalidChannelTypeException {

        if(channel.getType() != ChannelType.CATEGORY) {
            throw new InvalidChannelTypeException();
        }

        Category category = channel.asCategory();
        this.voiceManager.unsetDynamicCategory(category);

        return new TextLocaleResponse("voice.dynamic.unset.success");
    }

    @DiscordCommand(name = "config", description = "config a dynamic voice channel", ephemeral = true)
    public Modal config(
            SlashCommandInteractionEvent event, Guild guild, LocalizationManager localization,
            @LocaleType DiscordLocale locale, @DiscordParameter(name = "channel", description = "Channel to config") GuildChannelUnion channel) throws InvalidChannelTypeException, KeyNotFoundException {

        if(channel.getType() != ChannelType.VOICE) {
            throw new InvalidChannelTypeException();
        }

        Category category = channel.asCategory();
        Modal configModal = voiceManager.getConfigModal(category, VoiceDynamicController.configModal);

        if (configModal == null) {
            throw new KeyNotFoundException();
        }

        return configModal;
    }

    @DiscordCommand(name = "list", description = "list dynamic voice channels", ephemeral = true)
    public MessageEmbed list(Guild guild, LocalizationManager localization, @LocaleType DiscordLocale locale) {
        List<VoiceHive> hives = voiceManager.findAllByGuildId(guild.getIdLong());

        StringBuilder sb = new StringBuilder();
        for (VoiceHive hive : hives) {
            VoiceChannel hiveChannel = guild.getVoiceChannelById(hive.getVoiceId());
            Category     parent      = hiveChannel.getParentCategory();

            if (parent == null) {
                continue;
            }

            sb.append(hiveChannel.getParentCategory().getAsMention())
                    .append(" : ")
                    .append(hiveChannel.getAsMention())
                    .append("\n");
        }
        String message = sb.toString();

        String title = localization.getLocalizedString("voice.dynamic.list.title", locale);
        return Utils.simpleEmbed(title, message, Color.GREEN);
    }

    @DiscordButton(name = generateConfigModal, ephemeral = true)
    public Modal generateConfigModal(
            ButtonInteractionEvent event,
            LocalizationManager localization, @LocaleType DiscordLocale locale,
            @DiscordParameter(name = "category", description = "Category to add dynamic channel") GuildChannelUnion channel) throws KeyNotFoundException, InvalidChannelTypeException {

        if(channel.getType() != ChannelType.CATEGORY) {
            throw new InvalidChannelTypeException();
        }

        Category category = channel.asCategory();
        Modal configModal = voiceManager.getConfigModal(category, VoiceDynamicController.generateConfigModal);

        if (configModal == null) {
            throw new KeyNotFoundException();
        }

        return configModal;
    }

    @DiscordModal(name = configModal, ephemeral = true)
    public MessageEmbed configModal(
            Guild guild,
            LocalizationManager localization, @LocaleType DiscordLocale locale,
            ModalInteractionEvent event) throws KeyNotFoundException {

        String strCategoryId = event.getModalId().split(":")[1];
        long categoryId = Long.parseLong(strCategoryId);
        Category category = event.getGuild().getCategoryById(categoryId);

        if (category == null) {
            throw new KeyNotFoundException();
        }

        VoiceHive hive = voiceManager.setConfigModal(event, category);
        return hive.show(guild);
    }

    @DiscordButton(name = droneConfigButton, ephemeral = true)
    public Modal droneConfig(
            ButtonInteractionEvent event, Guild guild, @RequestInfo Member member) throws KeyNotFoundException, InvalidChannelTypeException, MissingPermissionsException {

        MessageChannelUnion channel = event.getChannel();

        if(channel.getType() != ChannelType.VOICE) {
            throw new InvalidChannelTypeException();
        }

        Modal configModal = droneManager.checkConfigDrone(guild, channel, member, droneConfigModal);

        if (configModal == null) {
            throw new KeyNotFoundException();
        }

        return configModal;
    }

    @DiscordModal(name = droneConfigModal, ephemeral = true)
    public TextLocaleResponse droneConfigModal(
            ModalInteractionEvent event) throws InvalidInputException {

        droneManager.updateDrone(event);
        return new TextLocaleResponse("voice.dynamic.drone.config.success");
    }

    @DiscordButton(name = droneHideShow, ephemeral = true)
    public TextLocaleResponse droneHideShow(
            ButtonInteractionEvent event, Guild guild, @RequestInfo Member member) throws KeyNotFoundException, MissingPermissionsException {

        MessageChannelUnion channel = event.getChannel();
        droneManager.toggleDroneVisibility(guild, channel, member);
        return new TextLocaleResponse("voice.dynamic.drone.hide-show.success");
    }

    @DiscordButton(name = dronePublicPrivate, ephemeral = true)
    public TextLocaleResponse dronePublicPrivate(
            ButtonInteractionEvent event, Guild guild, @RequestInfo Member member) throws KeyNotFoundException, MissingPermissionsException {

        MessageChannelUnion channel = event.getChannel();
        droneManager.toggleDronePublicPrivate(guild, channel, member);
        return new TextLocaleResponse("voice.dynamic.drone.public-private.success");
    }

    @DiscordButton(name = dronePermTemp, ephemeral = true)
    public TextLocaleResponse dronePermTemp(
            ButtonInteractionEvent event, Guild guild, @RequestInfo Member member) throws KeyNotFoundException, MissingPermissionsException {

        MessageChannelUnion channel = event.getChannel();
        droneManager.toggleDronePermTemp(guild, channel, member);
        return new TextLocaleResponse("voice.dynamic.drone.perm-temp.success");
    }

    @DiscordButton(name = droneClaim, ephemeral = true)
    public TextLocaleResponse droneClaim(
            ButtonInteractionEvent event, Guild guild, @RequestInfo Member member) throws KeyNotFoundException, NotInTheDroneException, OwnerInTheChannelException {

        MessageChannelUnion channel = event.getChannel();
        droneManager.claimDrone(guild, channel, member);
        return new TextLocaleResponse("voice.dynamic.drone.claim.success");
    }
}
