package com.softawii.capivara.services;

import com.softawii.capivara.entity.Template;
import com.softawii.capivara.entity.Template.TemplateKey;
import com.softawii.capivara.exceptions.TemplateAlreadyExistsException;
import com.softawii.capivara.exceptions.TemplateDoesNotExistException;
import com.softawii.capivara.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Template create(Template template) throws TemplateAlreadyExistsException {
        if (templateRepository.existsById(template.getTemplateKey())) throw new TemplateAlreadyExistsException();
        return templateRepository.save(template);
    }

    public void destroy(Long guildId, String name) throws TemplateDoesNotExistException {
        TemplateKey key = new TemplateKey(guildId, name);
        if (!templateRepository.existsById(key)) throw new TemplateDoesNotExistException();
        templateRepository.deleteById(key);
    }

    public Template update(Template template) throws TemplateDoesNotExistException {
        if (!templateRepository.existsById(template.getTemplateKey())) throw new TemplateDoesNotExistException();
        return templateRepository.save(template);
    }

    public List<Template> findAllByGuildId(Long guildId) {
        return templateRepository.findAllByTemplateKey_GuildIdOrderByTemplateKey_Name(guildId);
    }

    public Template findById(TemplateKey templateKey) throws TemplateDoesNotExistException {
        Optional<Template> optional = templateRepository.findById(templateKey);

        if (optional.isEmpty()) throw new TemplateDoesNotExistException();
        return optional.get();
    }

    public boolean existsById(TemplateKey templateKey) {
        return templateRepository.existsById(templateKey);
    }
}
