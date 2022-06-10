package com.softawii.capivara.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class Template {

    @EmbeddedId
    private TemplateKey templateKey;

    @Embeddable
    public static class TemplateKey implements Serializable {
        @Column
        private Long guildId;

        @Column
        private String name;

        public TemplateKey() {
        }

        public TemplateKey(Long guildId, String name) {
            this.guildId = guildId;
            this.name = name;
        }

        public Long getGuildId() {
            return guildId;
        }

        public void setGuildId(Long guildId) {
            this.guildId = guildId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TemplateKey that = (TemplateKey) o;
            return guildId.equals(that.guildId) && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(guildId, name);
        }
    }

    @Column
    private String json;

    public Template() {
    }

    public Template(TemplateKey templateKey, String json) {
        this.templateKey = templateKey;
        this.json = json;
    }

    public TemplateKey getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(TemplateKey templateKey) {
        this.templateKey = templateKey;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
