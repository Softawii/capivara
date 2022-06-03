package com.softwaii.capivara.services;

import com.softwaii.capivara.entity.Package;
import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.repository.PackageRepository;
import com.softwaii.capivara.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PackageService {

    private PackageRepository packageRepository;
    private RoleRepository roleRepository;

    public PackageService(PackageRepository packageRepository, RoleRepository roleRepository) {
        this.packageRepository = packageRepository;
        this.roleRepository = roleRepository;
    }

    public Package create(Package pkg) throws PackageAlreadyExistsException {
        if(packageRepository.existsById(pkg.getPackageKey())) throw new PackageAlreadyExistsException("Package already exists");
        return packageRepository.save(pkg);
    }

    public void destroy(Long guildId, String packageKey) throws PackageDoesNotExistException {
        Package.PackageKey key = new Package.PackageKey(guildId, packageKey);
        if(!packageRepository.existsById(key)) throw new PackageDoesNotExistException("Package does not exist");
        packageRepository.deleteById(key);
    }

    public void update(Package pkg) throws PackageDoesNotExistException {
        if(!packageRepository.existsById(pkg.getPackageKey())) throw new PackageDoesNotExistException("Package does not exist");
        packageRepository.save(pkg);
    }

    public List<Package> findAllByGuildId(Long guildId) {
        return packageRepository.findAllByPackageKey_GuildId(guildId);
    }

    public Package findByPackageId(Package.PackageKey key) throws PackageDoesNotExistException {
        Optional<Package> optional = packageRepository.findById(key);

        if(optional.isEmpty()) throw new PackageDoesNotExistException("Package does not exist");
        return optional.get();
    }
}
