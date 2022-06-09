package com.softawii.capivara.core;

import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.exceptions.KeyAlreadyInPackageException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.exceptions.UrlException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EmbedManager {

    public static class EmbedHandler {
        private EmbedBuilder builder;
        private String message;
        private List<MessageEmbed.Field> fields;
        private GuildChannel target;
        private List<ActionRow> activeRows;

        public EmbedHandler() {
            this.builder = new EmbedBuilder().setTitle("Titulo muito legal!").setDescription("Descrição sensacional");
            this.fields = new ArrayList<>();
        }
        public void setMessage(String message) {
            this.message = message;
        }

        public void setTitle(String title) throws FieldLengthException {
            try {
                builder.setTitle(title);
            } catch(IllegalArgumentException e) {
                throw new FieldLengthException(e.getMessage());
            }
        }

        public void setDescription(String description) throws FieldLengthException {
            try {
                builder.setDescription(description);
            } catch(IllegalArgumentException e) {
                throw new FieldLengthException(e.getMessage());
            }
        }

        public void setImage(String url) throws UrlException {
            try {
                builder.setImage(url);
            } catch(IllegalArgumentException e) {
                throw new UrlException(e.getMessage());
            }
        }

        public void addField(MessageEmbed.Field field) throws FieldLengthException {
            if(this.fields.size() > 25) throw new FieldLengthException();
            this.fields.add(field);
        }

        public void setField(MessageEmbed.Field field, int index) {
            this.fields.set(index, field);
        }

        public void removeField(int index) throws IndexOutOfBoundsException {
            if(index < 0 || index > this.fields.size()) throw new IndexOutOfBoundsException();
            this.fields.remove(index);
        }

        public List<String> getFieldNames() {
            return this.fields.stream().map(MessageEmbed.Field::getName).collect(Collectors.toList());
        }

        public GuildChannel getTarget() {
            return target;
        }

        public void setTarget(GuildChannel target) {
            this.target = target;
        }

        public List<ActionRow> getActiveRows() {
            return activeRows;
        }

        public void setActiveRows(List<ActionRow> activeRows) {
            this.activeRows = activeRows;
        }

        public String getMessage() {
            return this.message;
        }

        public MessageEmbed build() {
            EmbedBuilder builder = new EmbedBuilder(this.builder);

            for(MessageEmbed.Field field : this.fields) {
                builder.addField(field);
            }

            return builder.build();
        }
    }

    private Map<String, EmbedHandler> embeds;

    public EmbedManager() {
        this.embeds = new HashMap<>();
    }

    public Map.Entry<String, EmbedHandler> init() {
        EmbedHandler handler = new EmbedHandler();
        String       uuid    = UUID.randomUUID().toString();

        // It's in the map? Reset that shit bro
        while(this.embeds.containsKey(uuid)) uuid = UUID.randomUUID().toString();

        this.embeds.put(uuid, handler);
        return Map.entry(uuid, handler);
    }

    public EmbedHandler get(String key) throws KeyNotFoundException {
        if(!this.embeds.containsKey(key)) throw new KeyNotFoundException();
        return this.embeds.get(key);
    }

    public void destroy(String key) {
        if(this.embeds.containsKey(key)) this.embeds.remove(key);
    }
}
