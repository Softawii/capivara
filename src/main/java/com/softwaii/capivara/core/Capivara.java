package com.softwaii.capivara.core;

import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.exceptions.GuildNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class Capivara {

    private static Capivara instance = null;

    private Map<String, Guild> guilds;

    public Capivara() {
        guilds = new HashMap<>();
    }

    public Guild getGuild(String name) throws GuildNotFoundException {
        if(guilds.containsKey(name))  return guilds.get(name);
        // TODO: DB Query
        throw new GuildNotFoundException("Guild not found");
    }

    public Guild getOrCreateGuild(String name) {
        if (guilds.containsKey(name)) return guilds.get(name);
        // TODO: DB Query
        Guild guild = new Guild();
        guilds.put(name, guild);

        return guild;
    }

    public void addGuild(String name, Guild guild) {
        guilds.put(name, guild);
    }

    public Guild removeGuild(String name) {
        return guilds.remove(name);
    }
}
