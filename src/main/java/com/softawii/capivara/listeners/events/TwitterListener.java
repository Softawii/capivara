package com.softawii.capivara.listeners.events;

import com.softawii.capivara.listeners.TwitterGroup;
import com.softawii.capivara.services.TwitterParserConfigService;
import net.dv8tion.jda.api.JDA;
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
public class TwitterListener extends ListenerAdapter {
    private final Pattern                    twitterPattern;
    private final TwitterParserConfigService service;
    private static final char invisibleChar = '⠀'; // https://www.compart.com/en/unicode/U+2800

    public TwitterListener(JDA jda, TwitterParserConfigService service) {
        this.service = service;
        this.twitterPattern = Pattern.compile("^https://(twitter|x)\\.com/(?<username>\\w+)/status/(?<postId>\\d+)([-a-zA-Z0-9()@:%_+.~#?&/=]*)$"); // https://stackoverflow.com/a/17773849
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String rawMessage = event.getMessage().getContentRaw();
        User   author     = event.getAuthor();
        Optional<String> parsedMessage = parseMessage(rawMessage, author);

        parsedMessage.ifPresent(message -> createTweetMessage(event.getGuild().getIdLong(), message, event.getMessage()));
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;

        String rawMessage = event.getMessage().getContentRaw();
        User   author     = event.getAuthor();
        Optional<String> parsedMessage = parseMessage(rawMessage, author);

        parsedMessage.ifPresent(message -> createTweetMessage(event.getGuild().getIdLong(), message, event.getMessage()));
    }

    private void createTweetMessage(Long guildId, String replacementMessage, Message originalMessage) {
        if (!service.isEnabled(guildId)) return;

        MessageChannelUnion channel = originalMessage.getChannel();

        RestAction.allOf(
                originalMessage.delete(),
                channel.sendMessage(replacementMessage)
                        .addActionRow(TwitterGroup.generateDeleteButton(originalMessage.getAuthor().getIdLong()))
                        .setSuppressedNotifications(true)
        ).queue();
    }

    private Optional<String> parseMessage(String twitterLink, User author) {
        Matcher matcher = this.twitterPattern.matcher(twitterLink);

        if (matcher.find()) {
            String twitterUsername = matcher.group("username");
            String twitterPostId = matcher.group("postId");
            String result = String.format(
                    """
                    Autor: %s | [Link original](<%s>) [%c](https://fxtwitter.com/%s/status/%s)
                    """, author.getAsMention(), twitterLink, invisibleChar, twitterUsername, twitterPostId);
            return Optional.of(result);
        }

        return Optional.empty();
    }
}
