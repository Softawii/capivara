package com.softawii.capivara.repository;

import com.softawii.capivara.entity.SocialParserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialParserConfigRepository extends JpaRepository<SocialParserConfig, Long> {
}
