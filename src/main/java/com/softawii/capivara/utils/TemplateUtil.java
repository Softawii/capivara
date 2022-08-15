package com.softawii.capivara.utils;

import com.softawii.capivara.exceptions.CategoryIsEmptyException;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateUtil {
    public static Map<String, Integer> scanCategory(Category category) throws CategoryIsEmptyException {
        Map<String, Integer> channelMap = new HashMap<>();
        List<GuildChannel>   channels   = category.getChannels();

        if (channels.isEmpty()) throw new CategoryIsEmptyException();

        channels.forEach(c -> {
            channelMap.put(c.getName(), c.getType().ordinal());
        });

        return channelMap;
    }

    public static MessageEmbed categoryIsEmpty() {
        return Utils.simpleEmbed(
                "Tá de brincadeira?",
                "Essa categoria não tem canal algum", Color.ORANGE);
    }

    public static MessageEmbed channelIsNotCategory() {
        return Utils.simpleEmbed(
                "Calma lá",
                "Me passa uma categoria que continuamos o processo", Color.RED);
    }
}
