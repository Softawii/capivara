package com.softawii.capivara.events;

import com.softawii.capivara.controller.SocialGenericController;
import com.softawii.capivara.metrics.SocialMetrics;
import com.softawii.capivara.services.SocialParserConfigService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@SuppressWarnings("unused")
public class SocialListener extends ListenerAdapter {
    private final SocialMetrics metrics;
    private final Pattern twitterPattern;
    private final Pattern bskyPattern;

    private final SocialParserConfigService service;
    private static final char invisibleChar = '⠀'; // https://www.compart.com/en/unicode/U+2800

    public SocialListener(JDA jda, SocialParserConfigService service, SocialMetrics metrics) {
        this.twitterPattern = Pattern.compile("^https://(twitter|x)\\.com/(?<username>\\w+)/status/(?<postId>\\d+)([-a-zA-Z0-9()@:%_+.~#?&/=]*)$"); // https://stackoverflow.com/a/17773849
        this.bskyPattern = Pattern.compile("^https://bsky\\.app/profile/(?<username>[a-zA-Z0-9.]+)/post/(?<postId>[a-zA-Z0-9]+)$");
        this.service = service;
        this.metrics = metrics;
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        verifyMessageContent(event.getMessage(), event.getAuthor(), event.getGuild());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;
        verifyMessageContent(event.getMessage(), event.getAuthor(), event.getGuild());
    }

    private void createReplaceMessage(String replacementMessage, Message originalMessage) {
        MessageChannelUnion channel = originalMessage.getChannel();

        RestAction.allOf(
                originalMessage.delete(),
                channel.sendMessage(replacementMessage)
                        .addActionRow(SocialGenericController.generateDeleteButton(originalMessage.getAuthor().getIdLong()))
                        .setSuppressedNotifications(true)
        ).queue();
    }

    private void verifyMessageContent(Message message, User author, Guild guild) {
        String link = message.getContentRaw();
        Matcher twitterMatcher = this.twitterPattern.matcher(link);

        if (twitterMatcher.find() && this.service.isTwitterEnabled(guild.getIdLong())) {
            String twitterUsername = twitterMatcher.group("username");
            String twitterPostId = twitterMatcher.group("postId");
            String replacement = String.format(
                    """
                    Autor: %s | [Link original](<%s>) [%c](https://fxtwitter.com/%s/status/%s)
                    """, author.getAsMention(), link, invisibleChar, twitterUsername, twitterPostId);

            this.metrics.newTwitterParse();
            createReplaceMessage(replacement, message);
            return;
        }

        Matcher bskyMatcher = this.bskyPattern.matcher(link);

        if (bskyMatcher.find() && this.service.isBskyEnabled(guild.getIdLong())) {
            String bskyUsername = bskyMatcher.group("username");
            String bskyPostId = bskyMatcher.group("postId");
            String replacement = String.format(
                    """
                    Autor: %s | [Link original](<%s>) [%c](https://bskye.app/profile/%s/post/%s)
                    """, author.getAsMention(), link, invisibleChar, bskyUsername, bskyPostId);
            this.metrics.newBskyParse();
            createReplaceMessage(replacement, message);
        }
    }
}
