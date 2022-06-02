package com.softwaii.capivara.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Role {

    @Id
    @Column(name = "package_id")
    private Package.PackageKey packageKey;

    @Id
    @Column(name = "name")
    private String name;
    private String description;

    private Long roleId;

    public Role() {
    }

    public Role(Package.PackageKey packageKey, String name, String description, Long roleId) {
        this.packageKey = packageKey;
        this.name = name;
        this.description = description;
        this.roleId = roleId;
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
}
