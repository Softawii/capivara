package com.softwaii.capivara.core;

import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.entity.Package;
import com.softwaii.capivara.exceptions.*;
import com.softwaii.capivara.services.PackageService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PackageManager {

    PackageService packageService;

    public PackageManager(PackageService packageService) {
        this.packageService = packageService;
    }

    public void create(Long guildId, String packageName, boolean unique) throws PackageAlreadyExistsException {
        Package pkg = new Package(guildId, packageName, unique);
        this.packageService.create(pkg);
    }

    public void destroy(Long guildId, String packageName) throws PackageDoesNotExistException {
        this.packageService.destroy(guildId, packageName);
    }

    public void addRole(Long guildId, String packageName, Role role, String name, String description) throws PackageDoesNotExistException, RoleAlreadyAddedException {

        Package.PackageKey key = new Package.PackageKey(guildId, packageName);
        com.softwaii.capivara.entity.Role entity = new com.softwaii.capivara.entity.Role(key, name, description, role.getIdLong());

        Package pkg = this.packageService.findByPackageId(key);
        pkg.addRole(entity);

        this.packageService.update(pkg);
    }
}
