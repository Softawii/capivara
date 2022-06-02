package com.softwaii.capivara.core;

import com.softwaii.capivara.entity.Package;
import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.services.PackageService;
import com.softwaii.capivara.services.RoleService;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;

@Component
public class PackageManager {

    private PackageService packageService;
    private RoleService roleService;

    public PackageManager(PackageService packageService, RoleService roleService) {
        this.packageService = packageService;
        this.roleService = roleService;
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
        com.softwaii.capivara.entity.Role.RoleKey roleKey = new com.softwaii.capivara.entity.Role.RoleKey(key, name);
        if (roleService.exists(roleKey)) throw new RoleAlreadyAddedException();

        com.softwaii.capivara.entity.Role roleEntity = roleService.create(new com.softwaii.capivara.entity.Role(roleKey, description, role.getIdLong()));
        Package pkg = this.packageService.findByPackageId(key);
        pkg.addRole(roleEntity);

        this.packageService.update(pkg);
    }
}
