package com.softawii.capivara.repository;

import com.softawii.capivara.entity.VoiceHive;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface VoiceHiveRepository extends JpaRepository<VoiceHive, Long> {
    List<VoiceHive> findAllByGuildId(@NotNull Long SnowflakeId);

    boolean existsByCategoryId(@NotNull Long SnowflakeId);
}
