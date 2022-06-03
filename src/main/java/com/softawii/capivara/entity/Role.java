package com.softawii.capivara.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class Role {

    @EmbeddedId
    RoleKey roleKey;

    @Embeddable
    public static class RoleKey implements Serializable {
        @Column
        private Package.PackageKey packageKey;

        @Column(name = "role_name")
        private String name;

        public RoleKey() {
        }

        public RoleKey(Package.PackageKey packageKey, String name) {
            this.packageKey = packageKey;
            this.name = name;
        }

        public Package.PackageKey getPackageKey() {
            return packageKey;
        }

        public void setPackageKey(Package.PackageKey packageKey) {
            this.packageKey = packageKey;
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
            RoleKey roleKey = (RoleKey) o;
            return Objects.equals(packageKey, roleKey.packageKey) && Objects.equals(name, roleKey.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageKey, name);
        }
    }

    @Column
    private String description;

    @Column
    private Long roleId;

    @Column(nullable = false, columnDefinition = "varchar(255) default ''")
    private String emojiId;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean emojiUnicode;

    // TODO perigoso
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Package pkg;

    public Role() {
    }

    public Role(RoleKey roleKey, String description, Long roleId, String emojiId, boolean emojiUnicode) {
        this.roleKey = roleKey;
        this.description = description;
        this.roleId = roleId;
        this.emojiId = emojiId;
        this.emojiUnicode = emojiUnicode;
    }

    public RoleKey getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(RoleKey roleKey) {
        this.roleKey = roleKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Package getPkg() {
        return pkg;
    }

    public void setPkg(Package pkg) {
        this.pkg = pkg;
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

    @Override
    public String toString() {
        return "Role{" +
                "roleKey=" + roleKey +
                ", description='" + description + '\'' +
                ", roleId=" + roleId +
                ", pkg=" + pkg +
                '}';
    }
}
