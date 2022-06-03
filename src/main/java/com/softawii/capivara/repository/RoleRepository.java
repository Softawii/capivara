package com.softawii.capivara.repository;

import com.softawii.capivara.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Role.RoleKey> {
}
