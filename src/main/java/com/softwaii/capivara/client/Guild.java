package com.softwaii.capivara.client;

import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.exceptions.RoleAlreadyAddedException;
import com.softwaii.capivara.exceptions.RoleNotFoundException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Guild {

    private Map<String, Package> packages;

    public Guild() {
        packages = new HashMap<>();
        // TODO: DB Query
    }


    public void addPackage(String name, boolean unique) throws PackageAlreadyExistsException {
        if(packages.containsKey(name)) throw new PackageAlreadyExistsException("Package with name " + name + " already exists");
        packages.put(name, new Package(unique));

        // TODO: DB save
    }

    public void removePackage(String name) throws PackageDoesNotExistException {
        if(!packages.containsKey(name)) throw new PackageDoesNotExistException("Package with name " + name + " does not already exists");
        packages.remove(name);

        // TODO: DB save
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
        return packages.get(packageName).getRoles();
    }

    public SelectMenu createRoleMenu(Member member, String pkg_name, String id) {
        // Getting the roles
        Map<String, Role> roles = getRoles(pkg_name);

        SelectMenu.Builder builder = SelectMenu.create(id)
                .setPlaceholder("Select your roles")
                .setRequiredRange(0, 25);
        List<SelectOption> optionList = new ArrayList<>();
        for(Map.Entry<String, Role> entry : roles.entrySet()) {
            SelectOption option = SelectOption.of(entry.getKey(), entry.getKey()).withDefault(member.getRoles().contains(entry.getValue())).withDescription("Cornos");
            builder.addOptions(option);
        };

        return builder.build();
    }
}
