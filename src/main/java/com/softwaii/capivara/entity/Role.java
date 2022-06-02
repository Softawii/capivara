package com.softwaii.capivara.entity;

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

    private String description;

    private Long roleId;

    @ManyToOne(fetch = FetchType.EAGER)
    Package pkg;

    public Role() {
    }

    public Role(RoleKey roleKey, String description, Long roleId) {
        this.roleKey = roleKey;
        this.description = description;
        this.roleId = roleId;
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
