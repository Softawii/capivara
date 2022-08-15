package com.softawii.capivara.utils;

import com.softawii.capivara.exceptions.MultipleEmojiMessageException;
import com.vdurmont.emoji.EmojiParser;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static MessageEmbed simpleEmbed(String title, String description, Color color) {
        return simpleEmbed(title, description, color, new MessageEmbed.Field[0]);
    }

    public static MessageEmbed simpleEmbed(String title, String description, Color color, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color);

        if (fields.length != 0) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder.build();
    }


    public static List<String> extractEmojis(String text) {
        List<String> emojis = EmojiParser.extractEmojis(text);

        Pattern discordEmojiPattern = Pattern.compile("<:\\S*:\\d*>");
        Matcher matcher             = discordEmojiPattern.matcher(text);

        matcher.results().map(MatchResult::group).forEach(emojis::add);

        return emojis;
    }

    public static Pair<String, Boolean> getEmoji(String raw) throws MultipleEmojiMessageException {
        String  emoji     = "";
        boolean isUnicode = true;

        if (!raw.isBlank()) {
            List<String> emojis = Utils.extractEmojis(raw);
            if (emojis.size() > 1) {
                throw new MultipleEmojiMessageException();
            }
            emoji = emojis.get(0);
            isUnicode = !EmojiParser.extractEmojis(emoji).isEmpty();
        }

        return new Pair<>(emoji, isUnicode);
    }

    public static Color getRandomColor() {
        RandomGenerator random = RandomGenerator.getDefault();
        return new Color(random.nextInt(0, 255), random.nextInt(0, 255), random.nextInt(0, 255));
    }

    public static MessageEmbed nameContainsColon(String thisArgument) {
        return Utils.simpleEmbed("Nome muito feio!", thisArgument + " não pode conter o caracter ':', foi mal, problemas internos aqui!", Color.RED);
    }

    public static MessageEmbed multipleEmoji() {
        return Utils.simpleEmbed("Emoji muito feio!", "O emoji não pode conter mais de um emoji, tu ta doidinho!", Color.RED);
    }

    public static MessageEmbed parseObjectToJsonError() {
        return Utils.simpleEmbed(
                "Pane no sistema, alguém me desconfigurou",
                "Acho que algo de errado no processamento dos dados.\n" +
                        "Poderia tentar novamente?", Color.ORANGE);
    }

    /**
     * @param text Text to be parsed
     * @param length Max length of the text to be returned
     *
     * @return Text with max length or less
     */
    public static String getProperString(String text, int length) {
        if (text.length() >= length) {
            return text.substring(0, length - 3) + "...";
        }
        return text;
    }

}
