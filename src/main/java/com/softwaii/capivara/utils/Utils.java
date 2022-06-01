package com.softwaii.capivara.utils;

import com.softwaii.capivara.client.Guild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {

    public static SelectMenu createPackageMenu(List<String> packages, String id) {
        SelectMenu.Builder builder = SelectMenu.create(id)
                .setPlaceholder("Select your package")
                .setRequiredRange(1, 1);
        List<SelectOption> optionList = new ArrayList<>();

        for(String pkg : packages) {
            builder.addOption(pkg, pkg);
        }

        return builder.build();
    }

    public static MessageEmbed createEmbed(String title, String description, String color) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(title)
            .setDescription(description)
            .setColor(Integer.parseInt(color, 16));

        return builder.build();
    }
}
