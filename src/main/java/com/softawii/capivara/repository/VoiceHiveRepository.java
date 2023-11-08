package com.softawii.capivara.repository;

import com.softawii.capivara.entity.VoiceHive;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Repository
public interface VoiceHiveRepository extends JpaRepository<VoiceHive, Long> {
    List<VoiceHive> findAllByGuildId(@NotNull Long SnowflakeId);

    boolean existsByCategoryId(@NotNull Long SnowflakeId);
}
