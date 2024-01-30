package com.softawii.capivara.repository;

import com.softawii.capivara.entity.TwitterParserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwitterParserConfigRepository extends JpaRepository<TwitterParserConfig, Long> {
}
