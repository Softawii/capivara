package com.softawii.capivara.repository;

import com.softawii.capivara.entity.VoiceHive;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface VoiceHiveRepository extends JpaRepository<VoiceHive, Long> {

    boolean existsByHiveKey_CategoryId(Long aLong);

    boolean existsByHiveKey(VoiceHive.HiveKey hiveKey);

    Optional<VoiceHive> findByHiveKey_CategoryId(@NotNull Long SnowflakeId);

    List<VoiceHive> findAllByHiveKey_GuildId(@NotNull Long SnowflakeId);

    @Transactional
    void deleteByHiveKey_CategoryId(Long aLong);
}
