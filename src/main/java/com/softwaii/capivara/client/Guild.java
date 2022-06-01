package com.softwaii.capivara.client;

import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.exceptions.RoleNotFoundException;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Guild {

    public static class Package {
        private final Map<String, Role> roles;

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

        public void removeRole(String name) throws RoleNotFoundException {
            if (!roles.containsKey(name)) throw new RoleNotFoundException("Role with name " + name + " does not exist");
            roles.remove(name);
        }

        public Role getRole(String name) {
            return roles.get(name);
        }
    }

    private Map<String, Package> packages;

    public Guild() {
        packages = new HashMap<>();
        // TODO: DB Query
    }


    public void addPackage(String name) throws PackageAlreadyExistsException {
        if(packages.containsKey(name)) throw new PackageAlreadyExistsException("Package with name " + name + " already exists");
        packages.put(name, new Package());
    }

    public void removePackage(String name) throws PackageDoesNotExistException {
        if(!packages.containsKey(name)) throw new PackageDoesNotExistException("Package with name " + name + " does not already exists");
        packages.remove(name);
    }

    public void addRole(String packageName, String roleName, Role role) throws PackageDoesNotExistException, RoleAlreadyAddedException {
        if(!packages.containsKey(packageName)) throw new PackageDoesNotExistException("Package with name " + packageName + " does not exist");
        packages.get(packageName).addRole(roleName, role);
    }

    public void removeRole(String packageName, String roleName) throws PackageDoesNotExistException, RoleNotFoundException {
        if(!packages.containsKey(packageName)) throw new PackageDoesNotExistException("Package with name " + packageName + " does not exist");
        packages.get(packageName).removeRole(roleName);
    }

    public List<String> getPackageNames() {
        return new ArrayList<>(packages.keySet());
    }

    public Map<String, Role> getRoles(String packageName) {
        return packages.get(packageName).roles;
    }

}
