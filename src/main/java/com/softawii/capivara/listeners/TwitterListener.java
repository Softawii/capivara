package com.softawii.capivara.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TwitterListener extends ListenerAdapter {
    private final String patternStrTwitter;
    private final String patternStrX;
    public TwitterListener(JDA jda) {
        this.patternStrTwitter = "https://twitter.com/(\\w+)/status/(\\d+)";
        this.patternStrX = "https://x.com/(\\w+)/status/(\\d+)";
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        if (message.startsWith("https://twitter.com/")){
            message = fixEmbedTwitter(message, patternStrTwitter);
            if (message == null) return;
            event.getMessage().reply(message).mentionRepliedUser(false).queue();
        }

        if (message.startsWith("https://x.com/")){
            message = fixEmbedTwitter(message, patternStrX);
            if (message == null) return;
            event.getMessage().reply(message).mentionRepliedUser(false).queue();
        }

    }
    private static String fixEmbedTwitter(String url, String patternStr) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String userName = matcher.group(1);
            String remainingUrl = matcher.group(2);

            return String.format("https://fxtwitter.com/%s/status/%s", userName, remainingUrl);
        }
        return null;
    }
}
