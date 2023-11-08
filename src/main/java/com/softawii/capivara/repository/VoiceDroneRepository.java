package com.softawii.capivara.repository;

import com.softawii.capivara.entity.VoiceDrone;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface VoiceDroneRepository extends JpaRepository<VoiceDrone, Long> {

    VoiceDrone findByChatId(Long chatId);
}
