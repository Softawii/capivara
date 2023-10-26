package com.softawii.capivara.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

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

    /**
     * @param text   Text to be parsed
     * @param length Max length of the text to be returned
     * @return Text with max length or less
     */
    public static String getProperString(String text, int length) {
        if (text.length() >= length) {
            return text.substring(0, length - 3) + "...";
        }
        return text;
    }

}
