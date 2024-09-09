package dev.francies.betterPrivateChest.listeners;


import dev.francies.betterPrivateChest.BetterPrivateChest;
import dev.francies.betterPrivateChest.utils.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrivateChestProtection implements Listener {
    private BetterPrivateChest plugin;
    private final DataFile dataFile;

    public PrivateChestProtection(BetterPrivateChest plugin, DataFile dataFile) {
        this.plugin = plugin;
        this.dataFile = dataFile;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Chest)) {
            return;
        }

        Chest chest = (Chest) event.getClickedBlock().getState();
        Sign sign = findAttachedSign(chest);


        Chest doubleChest = findDoubleChest(chest);
        if (sign == null && doubleChest != null) {
            sign = findAttachedSign(doubleChest);
        }

        if (sign == null || !sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
            return;
        }

        String ownerName = ChatColor.stripColor(sign.getLine(1));
        String allowedPlayer1 = ChatColor.stripColor(sign.getLine(2));
        String allowedPlayer2 = ChatColor.stripColor(sign.getLine(3));

        Player player = event.getPlayer();
        String playerName = player.getName();

        if (!playerName.equalsIgnoreCase(ownerName) &&
                !playerName.equalsIgnoreCase(allowedPlayer1) &&
                !playerName.equalsIgnoreCase(allowedPlayer2) &&
                !player.hasPermission("btpchest.admin")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.noPermission").replace("%owner%", ownerName)));
        }
    }


    private Chest findDoubleChest(Chest chest) {

        InventoryHolder holder = chest.getInventory().getHolder();


        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;


            Chest leftChest = (Chest) doubleChest.getLeftSide();
            Chest rightChest = (Chest) doubleChest.getRightSide();


            if (!leftChest.getLocation().equals(chest.getLocation())) {
                return leftChest;
            } else {
                return rightChest;
            }
        }


        return null;
    }




    private void saveChestDestruction(Player player, Block block, String type) {
        FileConfiguration dataConfig = dataFile.getDataConfig();


        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());


        int destructionID = dataConfig.getInt("destructionCounter", 0) + 1;
        dataConfig.set("destructionCounter", destructionID);


        String blockLocation = block.getLocation().toString();
        String destructionPath = "destructions.destruction_" + destructionID;
        dataConfig.set(destructionPath + ".location", blockLocation);
        dataConfig.set(destructionPath + ".type", type);
        dataConfig.set(destructionPath + ".destroyed_by", player.getName());
        dataConfig.set(destructionPath + ".destroyed", formattedDate);

        dataFile.saveDataFile();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();


        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Sign sign = findAttachedSign(chest);


            Chest doubleChest = findDoubleChest(chest);
            if (sign == null && doubleChest != null) {
                sign = findAttachedSign(doubleChest);
            }


            if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
                String ownerName = ChatColor.stripColor(sign.getLine(1));
                if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("btpchest.admin")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                    return;
                }

                saveChestDestruction(player, chest.getBlock(), "chest");
            }
        }


        if (isBlockUnderChest(block)) {
            Block blockAbove = block.getRelative(0, 1, 0);
            if (blockAbove.getState() instanceof Chest) {
                Chest chestAbove = (Chest) blockAbove.getState();
                Sign sign = findAttachedSign(chestAbove);


                Chest doubleChest = findDoubleChest(chestAbove);
                if (sign == null && doubleChest != null) {
                    sign = findAttachedSign(doubleChest);
                }

                if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
                    String ownerName = ChatColor.stripColor(sign.getLine(1));
                    if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("btpchest.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                    }
                }
            }
        }

        // Protect the sign
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
                String ownerName = ChatColor.stripColor(sign.getLine(1));
                if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("btpchest.admin")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                }
            }
        }
    }

    private boolean isBlockUnderChest(Block block) {
        Block blockAbove = block.getRelative(0, 1, 0);
        if (blockAbove.getState() instanceof Chest) {
            return true;
        }

        Block leftBlock = blockAbove.getRelative(-1, 0, 0);
        Block rightBlock = blockAbove.getRelative(1, 0, 0);

        return leftBlock.getState() instanceof Chest || rightBlock.getState() instanceof Chest;
    }

    private Sign findAttachedSign(Chest chest) {
        Block[] neighbors = {
                chest.getBlock().getRelative(1, 0, 0),
                chest.getBlock().getRelative(-1, 0, 0),
                chest.getBlock().getRelative(0, 0, 1),
                chest.getBlock().getRelative(0, 0, -1)
        };

        for (Block neighbor : neighbors) {
            if (neighbor.getState() instanceof Sign) {
                Sign sign = (Sign) neighbor.getState();
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
                    return sign;
                }
            }
        }
        return null;
    }
}
