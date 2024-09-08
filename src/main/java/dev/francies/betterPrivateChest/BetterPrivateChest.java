package dev.francies.betterPrivateChest;

import dev.francies.betterPrivateChest.handlers.SignCreationHandler;
import dev.francies.betterPrivateChest.handlers.SignProtectionHandler;
import dev.francies.betterPrivateChest.listeners.ExplodeProtection;
import dev.francies.betterPrivateChest.listeners.HopperProtection;
import dev.francies.betterPrivateChest.listeners.PrivateChestProtection;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BetterPrivateChest extends JavaPlugin {

    @Override
    public void onEnable() {
        long t = System.currentTimeMillis();
        logAsciiArt();
        saveDefaultConfig();
        this.getLogger().log(Level.INFO, "");
        this.getLogger().log(Level.INFO, "AUTHOR: " + this.getDescription().getAuthors().get(0));
        this.getLogger().log(Level.INFO, "");
        getServer().getPluginManager().registerEvents(new PrivateChestProtection(this), this);
        getServer().getPluginManager().registerEvents(new HopperProtection(this), this);
        getServer().getPluginManager().registerEvents(new ExplodeProtection(this), this);
        getServer().getPluginManager().registerEvents(new SignProtectionHandler(this), this);
        getServer().getPluginManager().registerEvents(new SignCreationHandler(this), this);

        this.getLogger().log(Level.INFO, "_________________________");
        this.getLogger().log(Level.INFO, "Caricato in " + (System.currentTimeMillis() - t) + "ms");
        this.getLogger().log(Level.INFO, "_________________________");
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "_________________________");
        this.getLogger().log(Level.INFO, "BetterPrivateChest v" + this.getDescription().getVersion());
        this.getLogger().log(Level.INFO, "Author: " + this.getDescription().getAuthors().get(0));
        this.getLogger().log(Level.INFO, "BYE BYE");
        this.getLogger().log(Level.INFO, "_________________________");
        this.getLogger().log(Level.INFO, "");
    }
    public String printImage() {
        String image = ChatColor.GOLD + """
         .--------.
        / .------. \\
       / /        \\ \\
       | |        | |
      _| |________| |_
    .' |_|        |_| '.
    '._____ ____ _____.'  
    |     .'____'.     |
    '.__.'.'    '.'.__.'
    '.__  |      |  __.'
    |   '.'.____.'.'   |
    '.____'.____.'____.'
    '.________________.'
    """;

        // Aggiungi la versione sotto l'immagine
        image += ChatColor.GREEN + "\nVERSION " + this.getDescription().getVersion();

        return image;
    }



    public void logAsciiArt() {
        String[] lines = printImage().split("\n");
        for (String line : lines) {
            this.getLogger().info(line);
        }
    }
}
