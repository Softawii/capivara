package com.softawii.capivara.repository;

import com.softawii.capivara.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Package.PackageKey> {

    List<Package> findAllByPackageKey(Package.PackageKey packageKey);

    List<Package> findAllByPackageKey_GuildId(Long guildId);
}
