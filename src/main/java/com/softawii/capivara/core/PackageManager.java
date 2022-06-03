package com.softawii.capivara.core;

import com.softawii.capivara.entity.Package;
import com.softawii.capivara.exceptions.*;
import com.softawii.capivara.services.PackageService;
import com.softawii.capivara.services.RoleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
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

    public void create(Long guildId, String packageName, boolean unique, String description, String emojiId, boolean emojiUnicode) throws PackageAlreadyExistsException {
        Package pkg = new Package(guildId, packageName, unique, description, emojiId, emojiUnicode);
        this.packageService.create(pkg);
    }

    public void destroy(Long guildId, String packageName) throws PackageDoesNotExistException {
        this.packageService.destroy(guildId, packageName);
    }

    public void addRole(Long guildId, String packageName, Role role, String name, String description, String emojiId, boolean emojiUnicode) throws PackageDoesNotExistException, RoleAlreadyAddedException, KeyAlreadyInPackageException {
        Package.PackageKey key = new Package.PackageKey(guildId, packageName);
        com.softawii.capivara.entity.Role.RoleKey roleKey = new com.softawii.capivara.entity.Role.RoleKey(key, name);
        if (roleService.exists(roleKey)) throw new RoleAlreadyAddedException();

        com.softawii.capivara.entity.Role roleEntity = roleService.create(new com.softawii.capivara.entity.Role(roleKey, description, role.getIdLong(), emojiId, emojiUnicode));
        Package pkg = this.packageService.findByPackageId(key);
        pkg.addRole(roleEntity);

        this.packageService.update(pkg);
    }

    public void removeRole(Long guildId, String packageName, String roleName) throws RoleDoesNotExistException, PackageDoesNotExistException, RoleNotFoundException {
        // roleService.remove(new Package.PackageKey(guildId, packageName), roleName);
        Package.PackageKey key = new Package.PackageKey(guildId, packageName);
        com.softawii.capivara.entity.Role.RoleKey roleKey = new com.softawii.capivara.entity.Role.RoleKey(key, roleName);

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
        StringBuilder failBuilder = new StringBuilder();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Packages");
        builder.setDescription("List of packages in this guild");
        builder.setColor(Color.GREEN);

        packages.forEach(pkg -> {
            StringBuilder sb = new StringBuilder();

            Package.PackageKey key = pkg.getPackageKey();
            String name = key.getName();
            String description = pkg.getDescription();

            if(description != null)  sb.append("*").append(description).append("*\n\n");

            pkg.getRoles().forEach(role -> {
                Long roleId = role.getRoleId();

                Optional<Role> optional = roles.stream().filter(r -> roleId == r.getIdLong()).findFirst();

                if (optional.isEmpty()) {
                    failed.set(true);
                    failBuilder.append("Role '").append(role.getRoleKey().getName())
                            .append("' from package '").append(name)
                            .append("' with RoleId '").append(role.getRoleId()).append("' not found in this guild\n");
                }

                if(optional.isPresent()) {
                    Role roleJda = optional.get();
                    String line = "**" + role.getRoleKey().getName() + "**" + ": " + roleJda.getAsMention() + "\n";

                    if(!role.getDescription().isBlank()) {
                        line += "*" + role.getDescription() + "*\n\n";
                    }

                    sb.append(line);
                }
            });

            builder.addField(name, sb.toString(), false);
        });


        if(failed.get()) {
            builder.addField("Failed to find roles", failBuilder.toString(), false);
        }

        return builder.build();
    }

    public SelectMenu getGuildPackagesMenu(Long guildId, List<String> packages_ids, String customId, List<Emote> emotes) {
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

            if(!pkg.getEmojiId().isBlank()) {
                if(pkg.isEmojiUnicode()) option = option.withEmoji(Emoji.fromUnicode(pkg.getEmojiId()));
                else {
                    Emote emote = emotes.stream().filter(
                            emote1 -> emote1.getAsMention().equals(pkg.getEmojiId())
                    ).findFirst().orElse(null);

                    if(emote != null) option = option.withEmoji(Emoji.fromEmote(emote));
                }
            }

            builder.addOptions(option);
        }

        return builder.build();
    }

    public SelectMenu getGuildPackageRoleMenu(Long guildId, String packageName, String customId, Member member, List<Long> guildRoles, List<Emote> emotes) throws PackageDoesNotExistException {
        Package pkg = packageService.findByPackageId(new Package.PackageKey(guildId, packageName));
        SelectMenu.Builder builder = SelectMenu.create(customId);

        // Select Just one Package
        if(pkg.isSingleChoice())  builder.setRequiredRange(0, 1);
        else                      builder.setRequiredRange(0, 25);

        builder.setPlaceholder("Select a role");

        // Getting user roles and checking if the role is in the package (comparing ids)
        List<Long> roleIds = member.getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toList());

        pkg.getRoles().forEach(role -> {
            // If the roleId isn't in the guild role ids, it means the role is not in the guild
            if(guildRoles.contains(role.getRoleId())) {

                SelectOption option = SelectOption.of(role.getRoleKey().getName(), role.getRoleId().toString());
                option = option.withDescription(role.getDescription());

                // This role has an emote????
                if(!role.getEmojiId().isBlank()) {

                    if(role.isEmojiUnicode()) {
                        option = option.withEmoji(Emoji.fromUnicode(role.getEmojiId()));
                    } else {
                        Emote emote = emotes.stream().filter(
                                emote1 -> emote1.getAsMention().equals(role.getEmojiId())
                        ).findFirst().orElse(null);

                        if(emote != null) option = option.withEmoji(Emoji.fromEmote(emote));
                    }

                }

                if (roleIds.contains(role.getRoleId())) option = option.withDefault(true);
                builder.addOptions(option);
            }
        });

        return builder.build();
    }
}
