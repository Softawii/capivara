package com.softwaii.laputa.utils;

import com.softwaii.laputa.client.Guild;
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

    public static SelectMenu createRoleMenu(Member member, Guild guild, String pkg_name, String id) {
        // Getting the roles
        Map<String, Role> roles = guild.getRoles(pkg_name);

        SelectMenu.Builder builder = SelectMenu.create(id)
                .setPlaceholder("Select your roles")
                .setRequiredRange(0, 25);
        List<SelectOption> optionList = new ArrayList<>();
        for(Map.Entry<String, Role> entry : roles.entrySet()) {
            SelectOption option = SelectOption.of(entry.getKey(), entry.getKey()).withDefault(member.getRoles().contains(entry.getValue()));
            builder.addOptions(option);
        };

        return builder.build();
    }

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
