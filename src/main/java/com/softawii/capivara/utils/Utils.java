package com.softawii.capivara.utils;

import com.softawii.capivara.exceptions.MultipleEmojiMessageException;
import com.vdurmont.emoji.EmojiParser;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static MessageEmbed simpleEmbed(String title, String description, Color color) {
        return simpleEmbed(title, description, color, null);
    }

    public static MessageEmbed simpleEmbed(String title, String description, Color color, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color);

        if(fields != null) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder.build();
    }



    public static List<String> extractEmojis(String text) {
        List<String> emojis = EmojiParser.extractEmojis(text);

        Pattern discordEmojiPattern = Pattern.compile("<:\\S*:\\d*>");
        Matcher matcher = discordEmojiPattern.matcher(text);

        matcher.results().map(MatchResult::group).forEach(emojis::add);

        return emojis;
    }

    public static Pair<String, Boolean> getEmoji(String raw) throws MultipleEmojiMessageException {
        String emoji = "";
        boolean isUnicode = true;

        if(!raw.isBlank()) {
            List<String> emojis = Utils.extractEmojis(raw);
            if(emojis.size() > 1) {
                throw new MultipleEmojiMessageException();
            }
            emoji = emojis.get(0);
            isUnicode = !EmojiParser.extractEmojis(emoji).isEmpty();
        }

        return new Pair<>(emoji, isUnicode);
    }
}
