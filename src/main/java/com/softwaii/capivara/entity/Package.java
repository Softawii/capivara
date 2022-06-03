package com.softwaii.capivara.entity;

import com.softwaii.capivara.exceptions.KeyAlreadyInPackageException;
import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.exceptions.RoleNotFoundException;

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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<Role> roles;

    public Package() {
    }

    public Package(Long guildId, String name, boolean singleChoice, String description) {
        this.packageKey = new PackageKey(guildId, name);
        this.singleChoice = singleChoice;
        this.description = description;
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

    public void addRole(Role role) throws RoleAlreadyAddedException, KeyAlreadyInPackageException {
        if(roles.stream().anyMatch(r -> r.getRoleKey().equals(role.getRoleKey())))
            throw new KeyAlreadyInPackageException();

        if(roles.stream().anyMatch(r -> r.getRoleId().equals(role.getRoleId())))
            throw new RoleAlreadyAddedException();

        roles.add(role);
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
