package com.softwaii.capivara.core;

import com.softwaii.capivara.client.Guild;

import java.util.HashMap;
import java.util.Map;

public class Capivara {

    private static Capivara instance = null;
    private Map<String, Guild> guilds;

    private Capivara() {
        guilds = new HashMap<>();
    }

    public static Capivara getInstance() {
        if(instance == null) {
            instance = new Capivara();
        }
        return instance;
    }

    public Guild getGuild(String name) {
        return guilds.get(name);
    }

    public void addGuild(String name, Guild guild) {
        guilds.put(name, guild);
    }

    public Guild removeGuild(String name) {
        return guilds.remove(name);
    }
}
