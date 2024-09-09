package dev.francies.betterPrivateChest.handlers;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import dev.francies.betterPrivateChest.utils.DataFile;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;

public class SignCreationHandler implements Listener {
    private BetterPrivateChest plugin;
    private final DataFile dataFile;

    public SignCreationHandler(BetterPrivateChest plugin, DataFile dataFile) {
        this.plugin = plugin;
        this.dataFile = dataFile;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        Block block = event.getBlock();


        if (!player.hasPermission("btpchest.use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.cannotCreate")));
            return;
        }

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) block.getState();


        if (lines[0].equalsIgnoreCase("[privata]") || lines[0].equalsIgnoreCase("[privato]") || lines[0].equalsIgnoreCase("[private]") || lines[0].equalsIgnoreCase("[priv]")) {
            Block attachedBlock = block.getRelative(((org.bukkit.block.data.type.WallSign) sign.getBlockData()).getFacing().getOppositeFace());

            if (!(attachedBlock.getState() instanceof Chest)) {
                return;
            }


            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("enable-payment")) {
                double chestPrice = config.getDouble("chest-price");


                if (!plugin.getEconomy().has(player, chestPrice)) {
                    String insufficientFundsMessage = ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(config.getString("private-chest.insufficient-funds")));
                    player.sendMessage(insufficientFundsMessage);
                    return;
                } else {
                    String successMessage = ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(config.getString("private-chest.chest-purchased")).replace("%amount%", String.valueOf(chestPrice)));
                    plugin.getEconomy().withdrawPlayer(player, chestPrice);
                    player.sendMessage(successMessage);
                }

            }


            if (ChatColor.stripColor(lines[1]).isEmpty()) {
                String ownerName = player.getName();
                saveChestCreation(player, sign.getBlock());
                if(plugin.getConfig().getString( "private-chest-id").isEmpty()){
                    plugin.getLogger().log(Level.SEVERE, "ATTENTION! CHECK 'private-chest-id' in the config.yml, it may be empty");
                    return;
                }
                event.setLine(0, ChatColor.translateAlternateColorCodes('&',  plugin.getConfig().getString( "private-chest-id")));
                event.setLine(1, ChatColor.GREEN + ChatColor.BOLD.toString() + ownerName);
                if (!lines[2].isEmpty()) {
                    event.setLine(2, ChatColor.AQUA + ChatColor.stripColor(lines[2]));
                }
                if (!lines[3].isEmpty()) {
                    event.setLine(3, ChatColor.AQUA + ChatColor.stripColor(lines[3]));
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.success")));
            }


            if (ChatColor.stripColor(lines[2]).isEmpty() && ChatColor.stripColor(lines[3]).isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.twoMorePlayers")));
            } else if (ChatColor.stripColor(lines[2]).isEmpty() || ChatColor.stripColor(lines[3]).isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.oneMorePlayer")));
            }
        }
    }

    private void saveChestCreation(Player player, Block chestBlock) {
        FileConfiguration dataConfig = dataFile.getDataConfig();

        int chestID = dataConfig.getInt("chestCounter", 0) + 1;
        dataConfig.set("chestCounter", chestID);

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        String chestLocation = chestBlock.getLocation().toString();
        String chestPath = "chests.chest_" + chestID;
        dataConfig.set(chestPath + ".location", chestLocation);
        dataConfig.set(chestPath + ".owner", player.getName());
        dataConfig.set(chestPath + ".created", formattedDate);

        dataFile.saveDataFile();
    }
}
