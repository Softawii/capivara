package com.softawii.capivara.entity;

import com.softawii.capivara.exceptions.KeyAlreadyInPackageException;
import com.softawii.capivara.exceptions.RoleAlreadyAddedException;
import com.softawii.capivara.exceptions.RoleNotFoundException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
public class Package implements Serializable {

    @EmbeddedId
    PackageKey packageKey;

    @Embeddable
    public static class PackageKey implements Serializable {
        @Column
        Long guildId;

        @Column(name = "package_name")
        String name;

        public PackageKey() {
        }

        public PackageKey(Long guildId, String name) {
            this.guildId = guildId;
            this.name = name;
        }

        public Long getGuildId() {
            return guildId;
        }

        public void setGuildId(Long guildId) {
            this.guildId = guildId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PackageKey that = (PackageKey) o;
            return Objects.equals(guildId, that.guildId) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(guildId, name);
        }
    }

    @Column
    boolean singleChoice;

    @Column
    String description;

    @Column(nullable = false, columnDefinition = "varchar(255) default ''")
    String emojiId;

    @Column(nullable = false, columnDefinition = "boolean default false")
    boolean emojiUnicode;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<Role> roles;

    public Package() {
    }

    public Package(Long guildId, String name, boolean singleChoice, String description, String emojiId, boolean emojiUnicode) {
        this.packageKey = new PackageKey(guildId, name);
        this.singleChoice = singleChoice;
        this.description = description;
        this.emojiId = emojiId;
        this.emojiUnicode = emojiUnicode;
    }

    public PackageKey getPackageKey() {
        return packageKey;
    }

    public void setPackageKey(PackageKey packageKey) {
        this.packageKey = packageKey;
    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmojiId() {
        return emojiId;
    }

    public void setEmojiId(String emojiId) {
        this.emojiId = emojiId;
    }

    public boolean isEmojiUnicode() {
        return emojiUnicode;
    }

    public void setEmojiUnicode(boolean emojiUnicode) {
        this.emojiUnicode = emojiUnicode;
    }

    public void addRole(Role role) throws RoleAlreadyAddedException, KeyAlreadyInPackageException {
        if (roles.stream().anyMatch(r -> r.getRoleKey().equals(role.getRoleKey())))
            throw new KeyAlreadyInPackageException();

        if (roles.stream().anyMatch(r -> r.getRoleId().equals(role.getRoleId())))
            throw new RoleAlreadyAddedException();

        roles.add(role);
    }

    public boolean contains(Long roleId) {
        return roles.stream().anyMatch(r -> Objects.equals(r.getRoleId(), roleId));
    }

    public void removeRole(Role.RoleKey key) throws RoleNotFoundException {
        for (Role role : roles) {
            if (role.getRoleKey().equals(key)) {
                roles.remove(role);
                return;
            }
        }

        throw new RoleNotFoundException();
    }
}
