package com.softwaii.capivara.services;

import com.softwaii.capivara.entity.Package;
import com.softwaii.capivara.entity.Role;
import com.softwaii.capivara.exceptions.RoleDoesNotExistException;
import com.softwaii.capivara.repository.PackageRepository;
import com.softwaii.capivara.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleService {

    private PackageRepository packageRepository;
    private RoleRepository roleRepository;

    public RoleService(PackageRepository packageRepository, RoleRepository roleRepository) {
        this.packageRepository = packageRepository;
        this.roleRepository = roleRepository;
    }

    public Role create(Role role) {
        return roleRepository.save(role);
    }

    public Role findById(Role.RoleKey roleKey) throws RoleDoesNotExistException {
        Optional<Role> optionalRole = roleRepository.findById(roleKey);
        if (optionalRole.isEmpty()) throw new RoleDoesNotExistException("Role does not exists");
        return optionalRole.get();
    }

    public boolean exists(Role.RoleKey roleKey) {
        return roleRepository.existsById(roleKey);
    }

    public void remove(Package.PackageKey packageKey, String name) throws RoleDoesNotExistException {
        Role.RoleKey key = new Role.RoleKey(packageKey, name);
        if(!roleRepository.existsById(key)) throw new RoleDoesNotExistException("Role does not exist");
        roleRepository.deleteById(key);
    }
}
