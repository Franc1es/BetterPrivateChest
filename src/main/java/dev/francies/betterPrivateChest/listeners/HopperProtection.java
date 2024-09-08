package dev.francies.betterPrivateChest.listeners;


import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class HopperProtection implements Listener {
    private BetterPrivateChest plugin;

    public HopperProtection(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHopperMoveItem(InventoryMoveItemEvent event) {
        InventoryHolder destinationHolder = event.getDestination().getHolder();
        InventoryHolder sourceHolder = event.getSource().getHolder();

        // Controlla se la destinazione è una chest privata
        if (destinationHolder instanceof Chest) {
            Chest chest = (Chest) destinationHolder;
            Sign sign = findAttachedSign(chest);
            if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
                event.setCancelled(true);
                return;
            }
        }

        // Controlla se la sorgente è una chest privata
        if (sourceHolder instanceof Chest) {
            Chest chest = (Chest) sourceHolder;
            Sign sign = findAttachedSign(chest);
            if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
                event.setCancelled(true);
            }
        }
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
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
                    return sign;
                }
            }
        }
        return null;
    }
}
