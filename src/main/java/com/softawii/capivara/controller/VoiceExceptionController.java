package com.softawii.capivara.controller;

import com.softawii.capivara.exceptions.*;
import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.api.exception.CommandNotFoundException;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@DiscordExceptions(classes = {VoiceAgentController.class, VoiceMasterController.class})
public class VoiceExceptionController {

    private final MainExceptionController mainExceptionController;

    public VoiceExceptionController(MainExceptionController mainExceptionController) {
        this.mainExceptionController = mainExceptionController;
    }

    @DiscordException(MissingPermissionsException.class)
    public void missingPermissionsException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if(interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.missing_permissions", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(CommandNotFoundException.class)
    public void commandNotFoundException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.command_not_found", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(ExistingDynamicCategoryException.class)
    public void existingDynamicCategoryException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.existing_dynamic_category", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(InvalidChannelTypeException.class)
    public void invalidChannelTypeException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.invalid_channel_type", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(FieldLengthException.class)
    public void fieldLengthException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.field_length", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(InvalidInputException.class)
    public void invalidInputException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.invalid_input", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(KeyNotFoundException.class)
    public void keyNotFoundException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.key_not_found", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(NotInTheDroneException.class)
    public void notInTheDroneException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.not_in_the_drone", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(OwnerInTheChannelException.class)
    public void ownerInTheChannelException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.owner_in_the_channel", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(UrlException.class)
    public void urlException(Throwable exception, Interaction interaction, LocalizationManager localizationManager, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localizationManager.getLocalizedString("voice.error.url_not_found", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(Throwable.class)
    public void throwable(Throwable throwable, Interaction interaction) {
        mainExceptionController.handle(throwable, interaction);
    }

}
