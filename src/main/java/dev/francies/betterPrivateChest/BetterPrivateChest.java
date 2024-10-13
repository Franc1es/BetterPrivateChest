package dev.francies.betterPrivateChest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.francies.betterPrivateChest.handlers.SignCreationHandler;
import dev.francies.betterPrivateChest.handlers.SignProtectionHandler;
import dev.francies.betterPrivateChest.listeners.*;
import dev.francies.betterPrivateChest.utils.DataFile;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public final class BetterPrivateChest extends JavaPlugin {
    private DataFile dataFile;
    public static Economy econ = null;
    private final String versionUrl = "https://www.francescoferrara.it/api/betterprivatechest.json";
    @Override
    public void onEnable() {
        int pluginId = 23325;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));
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
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new CrystalAnchorPlaceEvent(this), this);
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



    public void logAsciiArt() {
        String[] lines = printImage().split("\n");
        for (String line : lines) {
            this.getLogger().info(line);
        }
    }
    public void checkForUpdates(Player player) {

            try {

                URL url = new URL(versionUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                connection.disconnect();


                JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();


                String latestVersion = json.get("version").getAsString();
                String downloadUrl1 = json.get("downloadUrl1").getAsString();


                String currentVersion = this.getDescription().getVersion();

                if (!currentVersion.equals(latestVersion)) {
                    if (player.hasPermission("btpchest.admin")) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix-private") +" &eA newer version is available: &f" + latestVersion));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix-private") +"&bDownload link 1: &f" + downloadUrl1));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

}
