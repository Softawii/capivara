package com.softwaii.capivara.client;

import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.exceptions.RoleNotFoundException;
import net.dv8tion.jda.api.entities.Role;

import java.util.HashMap;
import java.util.Map;

public class Package {
    private final Map<String, Role> roles;
    private boolean                 unique;

    public Package(boolean unique) {
        this.roles = new HashMap<>();
        this.unique = unique;
    }

    public void addRole(String name, Role role) throws RoleAlreadyAddedException {
        if (roles.containsKey(name)) throw new RoleAlreadyAddedException("Role with name " + name + " already exists");
        roles.put(name, role);

        // TODO: DB save
    }

    public void removeRole(String name) throws RoleNotFoundException {
        if (!roles.containsKey(name)) throw new RoleNotFoundException("Role with name " + name + " does not exist");
        roles.remove(name);

        // TODO: DB save
    }

    public Map<String, Role> getRoles() {
        return roles;
    }
}
