package com.softawii.capivara.repository;

import com.softawii.capivara.entity.DiscordMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordMessageRepository extends JpaRepository<DiscordMessage, Long> {


}
