package com.softawii.capivara.repository;

import com.softawii.capivara.entity.VoiceDrone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoiceDroneRepository extends JpaRepository<VoiceDrone, Long> {

    VoiceDrone findByChatId(Long chatId);
}
