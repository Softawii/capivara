package com.softawii.capivara.repository;

import com.softawii.capivara.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Template.TemplateKey> {

    List<Template> findAllByTemplateKey_GuildId(Long guildId);


    List<Template> findAllByTemplateKey_GuildIdOrderByTemplateKey_Name(Long guildId);

}
