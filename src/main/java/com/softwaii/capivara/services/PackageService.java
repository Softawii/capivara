package com.softwaii.capivara.services;

import com.softwaii.capivara.entity.Package;
import com.softwaii.capivara.exceptions.PackageAlreadyExistsException;
import com.softwaii.capivara.exceptions.PackageDoesNotExistException;
import com.softwaii.capivara.repository.PackageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PackageService {

    private PackageRepository repository;

    public PackageService(PackageRepository repository) {
        this.repository = repository;
    }

    public void create(Package pkg) throws PackageAlreadyExistsException {
        if(repository.existsById(pkg.getPackageKey())) throw new PackageAlreadyExistsException("Package already exists");
        repository.save(pkg);
    }

    public void destroy(Long guildId, String packageKey) throws PackageDoesNotExistException {
        Package.PackageKey key = new Package.PackageKey(guildId, packageKey);
        if(!repository.existsById(key)) throw new PackageDoesNotExistException("Package does not exist");
        repository.deleteById(key);
    }

    public void update(Package pkg) throws PackageDoesNotExistException {
        if(!repository.existsById(pkg.getPackageKey())) throw new PackageDoesNotExistException("Package does not exist");
        repository.save(pkg);
    }

    public List<Package> findAllByGuildId(Long guildId) {
        return repository.findAllByGuildId(guildId);
    }

    public Package findByPackageId(Package.PackageKey key) throws PackageDoesNotExistException {
        Optional<Package> optional = repository.findById(key);

        if(optional.isEmpty()) throw new PackageDoesNotExistException("Package does not exist");
        return optional.get();
    }
}
