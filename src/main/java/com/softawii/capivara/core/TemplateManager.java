package com.softawii.capivara.core;

import com.softawii.capivara.entity.Template;
import com.softawii.capivara.entity.Template.TemplateKey;
import com.softawii.capivara.exceptions.TemplateAlreadyExistsException;
import com.softawii.capivara.exceptions.TemplateDoesNotExistException;
import com.softawii.capivara.services.TemplateService;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateManager {

    private final TemplateService templateService;

    public TemplateManager(TemplateService templateService) {
        this.templateService = templateService;
    }

    public Template create(Long guildId, String name, String json) throws TemplateAlreadyExistsException {
        return templateService.create(new Template(new TemplateKey(guildId, name), json));
    }

    public Template update(Long guildId, String name, String json) throws TemplateDoesNotExistException {
        Template template = templateService.findById(new TemplateKey(guildId, name));
        template.setJson(json);
        return templateService.update(template);
    }

    public void destroy(Long guildId, String name) throws TemplateDoesNotExistException {
        templateService.destroy(guildId, name);
    }

    public List<Template> findAllByGuildId(Long guildId) {
        return templateService.findAllByGuildId(guildId);
    }

    public Template findById(Long guildId, String name) throws TemplateDoesNotExistException {
        return templateService.findById(new TemplateKey(guildId, name));
    }

    public boolean existsById(Long guildId, String name)  {
        return templateService.existsById(new TemplateKey(guildId, name));
    }

    public List<Command.Choice> autoCompleteTemplateName(Long guildId, String templateName) {
        return templateService.findAllByGuildId(guildId).stream()
                .map(template -> template.getTemplateKey().getName())
                .filter(c -> c.startsWith(templateName))
                .map(c -> new Command.Choice(c, c))
                .toList();
    }
}
