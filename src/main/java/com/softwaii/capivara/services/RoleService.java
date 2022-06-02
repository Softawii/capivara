package com.softwaii.capivara.services;

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
}
