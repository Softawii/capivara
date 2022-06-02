package com.softwaii.capivara.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Entity
public class Package implements Serializable {

    @EmbeddedId
    PackageKey packageKey;

    @Embeddable
    public class PackageKey implements Serializable {
        @Column
        Long guildId;

        @Column
        String name;

        public PackageKey() {
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

    @ElementCollection
    @MapKeyColumn(name = "role_key")
    @Column(name = "role_id")
    Map<String, String> roles;

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

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }
}
