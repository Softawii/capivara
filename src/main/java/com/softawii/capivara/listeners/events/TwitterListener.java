package com.softawii.capivara.listeners.events;

import com.softawii.capivara.services.TwitterTransformService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TwitterListener extends ListenerAdapter {
    private final Pattern twitterPattern;
    private final TwitterTransformService service;

    public TwitterListener(JDA jda, TwitterTransformService service) {
        this.service = service;
        this.twitterPattern = Pattern.compile("^https://(twitter|x)\\.com/(?<username>\\w+)/status/(?<postId>\\d+)$");
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!service.isEnabled(event.getGuild().getIdLong())) return;

        String rawMessage = event.getMessage().getContentRaw();
        User   author     = event.getAuthor();
        Optional<String> parsedMessage = parseMessage(rawMessage, author);

        parsedMessage.ifPresent(message -> {
            RestAction.allOf(
                    event.getMessage()
                            .delete(),
                    event.getChannel()
                            .sendMessage(message)
                            .setSuppressedNotifications(true)
            ).queue();
        });
    }

    private Optional<String> parseMessage(String twitterLink, User author) {
        Matcher matcher = this.twitterPattern.matcher(twitterLink);

        if (matcher.find()) {
            String twitterUsername = matcher.group("username");
            String twitterPostId = matcher.group("postId");

            String result = String.format(
                    """
                    Autor: %s
                    Link original: `%s`
                    [Postagem](https://fxtwitter.com/%s/status/%s)
                    """, author.getAsMention(), twitterLink, twitterUsername, twitterPostId);
            return Optional.of(result);
        }

        return Optional.empty();
    }
}
