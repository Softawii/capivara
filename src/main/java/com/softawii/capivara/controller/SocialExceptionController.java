package com.softawii.capivara.controller;

import com.softawii.capivara.exceptions.MissingPermissionsException;
import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import com.softawii.curupira.v2.annotations.LocaleType;
import com.softawii.curupira.v2.localization.LocalizationManager;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.stereotype.Component;

@Component
@DiscordExceptions(classes = SocialTwitterGroup.class)
public class SocialExceptionController {

    private final MainExceptionController mainExceptionController;

    public SocialExceptionController(MainExceptionController mainExceptionController) {
        this.mainExceptionController = mainExceptionController;
    }

    @DiscordException(MissingPermissionsException.class)
    public void missingPermissions(Throwable throwable, Interaction interaction, LocalizationManager localization, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localization.getLocalizedString("social.error.missing_permissions", locale)).setEphemeral(true).queue();
        }
    }

    @DiscordException(Throwable.class)
    public void generic(Throwable throwable, Interaction interaction, LocalizationManager localization, @LocaleType DiscordLocale locale) {
        if (interaction instanceof IReplyCallback callback) {
            callback.reply(localization.getLocalizedString("social.error.generic", locale)).setEphemeral(true).queue();
        }

        this.mainExceptionController.handle(throwable, interaction);
    }
}
