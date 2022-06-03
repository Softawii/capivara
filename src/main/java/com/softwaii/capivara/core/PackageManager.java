package com.softwaii.capivara.core;

import com.softwaii.capivara.entity.Package;
import com.softwaii.capivara.exceptions.*;
import com.softwaii.capivara.services.PackageService;
import com.softwaii.capivara.services.RoleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class PackageManager {

    private PackageService packageService;
    private RoleService roleService;

    public PackageManager(PackageService packageService, RoleService roleService) {
        this.packageService = packageService;
        this.roleService = roleService;
    }

    public void create(Long guildId, String packageName, boolean unique, String description) throws PackageAlreadyExistsException {
        Package pkg = new Package(guildId, packageName, unique, description);
        this.packageService.create(pkg);
    }

    public void destroy(Long guildId, String packageName) throws PackageDoesNotExistException {
        this.packageService.destroy(guildId, packageName);
    }

    public void addRole(Long guildId, String packageName, Role role, String name, String description) throws PackageDoesNotExistException, RoleAlreadyAddedException {
        Package.PackageKey key = new Package.PackageKey(guildId, packageName);
        com.softwaii.capivara.entity.Role.RoleKey roleKey = new com.softwaii.capivara.entity.Role.RoleKey(key, name);
        if (roleService.exists(roleKey)) throw new RoleAlreadyAddedException();

        com.softwaii.capivara.entity.Role roleEntity = roleService.create(new com.softwaii.capivara.entity.Role(roleKey, description, role.getIdLong()));
        Package pkg = this.packageService.findByPackageId(key);
        pkg.addRole(roleEntity);

        this.packageService.update(pkg);
    }

    public void removeRole(Long guildId, String packageName, String roleName) throws RoleDoesNotExistException, PackageDoesNotExistException, RoleNotFoundException {
        // roleService.remove(new Package.PackageKey(guildId, packageName), roleName);
        Package.PackageKey key = new Package.PackageKey(guildId, packageName);
        com.softwaii.capivara.entity.Role.RoleKey roleKey = new com.softwaii.capivara.entity.Role.RoleKey(key, roleName);

        if (!roleService.exists(roleKey)) throw new RoleDoesNotExistException();

        Package pkg = this.packageService.findByPackageId(key);
        pkg.removeRole(roleKey);

        this.packageService.update(pkg);

        if(roleService.exists(roleKey)) {
            roleService.remove(roleKey.getPackageKey(), roleKey.getName());
        }
    }

    public MessageEmbed getGuildPackages(Long guildId, List<Role> roles) {
        List<Package> packages = packageService.findAllByGuildId(guildId);
        AtomicBoolean failed = new AtomicBoolean(false);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Packages");
        builder.setDescription("List of packages in this guild");
        builder.setColor(Color.GREEN);

        packages.forEach(pkg -> {
            StringBuilder sb = new StringBuilder();

            Package.PackageKey key = pkg.getPackageKey();
            String name = key.getName();
            String description = pkg.getDescription();

            if(description != null)  sb.append("*").append(description).append("*\n");

            pkg.getRoles().forEach(role -> {
                Long roleId = role.getRoleId();

                Optional<Role> optional = roles.stream().filter(r -> roleId == r.getIdLong()).findFirst();

                if (optional.isEmpty()) failed.set(true);

                if(optional.isPresent()) {
                    Role roleJda = optional.get();
                    String line = role.getRoleKey().getName() + ": " + roleJda.getAsMention() + "\n";

                    if(!role.getDescription().isBlank()) {
                        line += "*" + role.getDescription() + "*\n";
                    }

                    sb.append(line);
                }
            });

            builder.addField(name, sb.toString(), false);
        });

        return builder.build();
    }

    public SelectMenu getGuildPackagesMenu(Long guildId, List<String> packages_ids, String customId) {
        List<Package> packages = packageService.findAllByGuildId(guildId);
        // TODO: If packages != null, check if package is in packages_ids
        SelectMenu.Builder builder = SelectMenu.create(customId);

        // Select Just one Package
        builder.setRequiredRange(1, 1);
        // TODO: Customize the message
        builder.setPlaceholder("Select a package");

        for(Package pkg : packages) {
            SelectOption option = SelectOption.of(pkg.getPackageKey().getName(), pkg.getPackageKey().getName());
            option = option.withDescription(pkg.getDescription());
            builder.addOptions(option);
        }

        return builder.build();
    }

    public SelectMenu getGuildPackageRoleMenu(Long guildId, String packageName, String customId, Member member, List<Long> guildRoles) throws PackageDoesNotExistException {
        Package pkg = packageService.findByPackageId(new Package.PackageKey(guildId, packageName));
        SelectMenu.Builder builder = SelectMenu.create(customId);

        // Select Just one Package
        if(pkg.isSingleChoice())  builder.setRequiredRange(1, 1);
        else                      builder.setRequiredRange(0, pkg.getRoles().size());

        // TODO: Check if all ids in package are in guildRoles

        // Getting user roles and checking if the role is in the package (comparing ids)
        List<Long> roleIds = member.getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toList());

        pkg.getRoles().forEach(role -> {
            SelectOption option = SelectOption.of(role.getRoleKey().getName(), role.getRoleId().toString());
            option = option.withDescription(role.getDescription());

            if(roleIds.contains(role.getRoleId())) option = option.withDefault(true);
            builder.addOptions(option);
        });

        return builder.build();
    }
}
