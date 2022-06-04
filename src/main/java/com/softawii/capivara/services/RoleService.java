package com.softawii.capivara.services;

import com.softawii.capivara.entity.Package;
import com.softawii.capivara.entity.Role;
import com.softawii.capivara.exceptions.RoleDoesNotExistException;
import com.softawii.capivara.repository.PackageRepository;
import com.softawii.capivara.repository.RoleRepository;
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

    public void update(Role role) throws RoleDoesNotExistException {
        Role.RoleKey key = role.getRoleKey();
        if(!roleRepository.existsById(key)) throw new RoleDoesNotExistException("Role does not exist");
        roleRepository.save(role);
    }
}
