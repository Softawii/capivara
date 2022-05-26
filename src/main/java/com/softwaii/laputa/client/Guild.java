package com.softwaii.laputa.client;

import com.softwaii.laputa.exceptions.PackageAlreadyExistsException;
import com.softwaii.laputa.exceptions.RoleAlreadyAddedException;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Guild {

    public static class Package {
        private Map<String, Role> roles;

        public Package() {
            this.roles = new HashMap<>();
        }

        public Package(Map<String, Role> roles) {
            this.roles = roles;
        }

        public void addRole(String name, Role role) throws RoleAlreadyAddedException {
            if (roles.containsKey(name)) throw new RoleAlreadyAddedException("Role with name " + name + " already exists");
            roles.put(name, role);
        }

        public Role getRole(String name) {
            return roles.get(name);
        }
    }

    private Map<String, Package> packages;

    public Guild() {
        packages = new HashMap<>();
    }

    public Guild(Map<String, Package> packages) {
        this.packages = packages;
    }

    public void addPackage(String name) throws PackageAlreadyExistsException {
        if(packages.containsKey(name)) throw new PackageAlreadyExistsException("Package with name " + name + " already exists");
        packages.put(name, new Package());
    }

    public void addRole(String packageName, String roleName, Role role) throws RoleAlreadyAddedException {
        if(!packages.containsKey(packageName)) throw new RoleAlreadyAddedException("Package with name " + packageName + " does not exist");
        packages.get(packageName).addRole(roleName, role);
    }

    public List<String> getPackageNames() {
        return new ArrayList<>(packages.keySet());
    }

    public Map<String, Role> getRoles(String packageName) {
        return packages.get(packageName).roles;
    }

}
