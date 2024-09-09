package dev.francies.betterPrivateChest;

import dev.francies.betterPrivateChest.handlers.SignCreationHandler;
import dev.francies.betterPrivateChest.handlers.SignProtectionHandler;
import dev.francies.betterPrivateChest.listeners.ExplodeProtection;
import dev.francies.betterPrivateChest.listeners.HopperProtection;
import dev.francies.betterPrivateChest.listeners.PistonProtection;
import dev.francies.betterPrivateChest.listeners.PrivateChestProtection;
import dev.francies.betterPrivateChest.utils.DataFile;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BetterPrivateChest extends JavaPlugin {
    private DataFile dataFile;
    public static Economy econ = null;

    @Override
    public void onEnable() {
        long t = System.currentTimeMillis();
        if (!setupEconomy()) {
            getLogger().severe("YOU DON'T HAVE VAULT PLUGIN!");
        }

        logAsciiArt();
        saveDefaultConfig();
        this.dataFile = new DataFile(this);
        dataFile.saveDefaultDataFile();
        this.getLogger().log(Level.INFO, "");
        this.getLogger().log(Level.INFO, "AUTHOR: " + this.getDescription().getAuthors().get(0));
        this.getLogger().log(Level.INFO, "");
        getServer().getPluginManager().registerEvents(new PrivateChestProtection(this, dataFile), this);
        getServer().getPluginManager().registerEvents(new HopperProtection(this), this);
        getServer().getPluginManager().registerEvents(new PistonProtection(this, dataFile), this);
        getServer().getPluginManager().registerEvents(new ExplodeProtection(this), this);
        getServer().getPluginManager().registerEvents(new SignProtectionHandler(this), this);
        getServer().getPluginManager().registerEvents(new SignCreationHandler(this, dataFile), this);

        this.getLogger().log(Level.INFO, "_________________________");
        this.getLogger().log(Level.INFO, "Loaded in " + (System.currentTimeMillis() - t) + "ms");
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
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public Economy getEconomy(){
        return econ;
    }

    public String printImage() {
        String image = """
               \s
  ____       _   _            ____       _            _        ____ _              _   
 | __ )  ___| |_| |_ ___ _ __|  _ \\ _ __(___   ____ _| |_ ___ / ___| |__   ___ ___| |_ 
 |  _ \\ / _ | __| __/ _ | '__| |_) | '__| \\ \\ / / _` | __/ _ | |   | '_ \\ / _ / __| __|
 | |_) |  __| |_| ||  __| |  |  __/| |  | |\\ V | (_| | ||  __| |___| | | |  __\\__ | |_ 
 |____/ \\___|\\__|\\__\\___|_|  |_|   |_|  |_| \\_/ \\__,_|\\__\\___|\\____|_| |_|\\___|___/\\__|
                                                                                       
               \s""";


        image +="\nVERSION " + this.getDescription().getVersion();

        return image;
    }


    public DataFile getDataFile() {
        return dataFile;
    }

    public void logAsciiArt() {
        String[] lines = printImage().split("\n");
        for (String line : lines) {
            this.getLogger().info(line);
        }
    }
}
