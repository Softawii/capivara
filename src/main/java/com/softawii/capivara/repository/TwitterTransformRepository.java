package com.softawii.capivara.repository;

import com.softawii.capivara.entity.TwitterTransform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwitterTransformRepository extends JpaRepository<TwitterTransform, Long> {
}
