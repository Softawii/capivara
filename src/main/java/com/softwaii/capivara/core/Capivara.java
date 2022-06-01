package com.softwaii.capivara.core;

import com.softwaii.capivara.client.Guild;
import com.softwaii.capivara.exceptions.GuildNotFoundException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.HashMap;
import java.util.Map;

public class Capivara {

    private static Capivara instance = null;

    private Map<String, Guild> guilds;
    private PackageManager packageManager;

    private Capivara() {
        guilds = new HashMap<>();
    }

    private void init() {
        packageManager = new PackageManager();
    }

    public static Capivara getInstance() {
        if(instance == null) {
            instance = new Capivara();
            instance.init();
        }
        return instance;
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

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public void addGuild(String name, Guild guild) {
        guilds.put(name, guild);
    }

    public Guild removeGuild(String name) {
        return guilds.remove(name);
    }
}
