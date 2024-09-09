package dev.francies.betterPrivateChest.utils;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class DataFile {
    private final BetterPrivateChest plugin;
    private FileConfiguration dataConfig = null;
    private File dataFile = null;

    public DataFile(BetterPrivateChest plugin) {
        this.plugin = plugin;
        saveDefaultDataFile();
    }


    public void reloadDataFile() {
        if (dataFile == null) {
            dataFile = new File(plugin.getDataFolder(), "data.yml");
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public FileConfiguration getDataConfig() {
        if (dataConfig == null) {
            reloadDataFile();
        }
        return dataConfig;
    }

    public void saveDataFile() {
        if (dataConfig == null || dataFile == null) {
            return;
        }
        try {
            getDataConfig().save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml to " + dataFile, ex);
        }
    }

    public void saveDefaultDataFile() {
        if (dataFile == null) {
            dataFile = new File(plugin.getDataFolder(), "data.yml");
        }
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
    }
}
