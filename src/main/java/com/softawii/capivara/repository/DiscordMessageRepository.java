package com.softawii.capivara.repository;

import com.softawii.capivara.entity.DiscordMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.Tuple;

public interface DiscordMessageRepository extends JpaRepository<DiscordMessage, Long> {

    @Query(value = """
    select
        GUILDID,
        count(checked) as "MESSAGECOUNT",
        sum(CASE WHEN HATE or HATEOPENAI THEN 1 ELSE 0 END) as "HATECOUNT",
        (cast(COALESCE(sum(CASE WHEN HATE or HATEOPENAI THEN 1 ELSE 0 END)) as double precision) / cast(count(CHECKED) as double precision)) * 100 as hate
    from DISCORDMESSAGE
    where CHECKED = true and GUILDID = :guildId
    group by GUILDID
   """, nativeQuery = true)
    public Tuple getHateStatsByGuildId(@Param("guildId") Long guildId);
}
