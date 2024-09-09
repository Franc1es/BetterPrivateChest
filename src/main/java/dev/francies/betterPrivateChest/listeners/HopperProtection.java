package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;

public class HopperProtection implements Listener {
    private BetterPrivateChest plugin;

    public HopperProtection(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType() == Material.HOPPER) {
            Block blockAbove = event.getBlock().getRelative(0, 1, 0);


            if (blockAbove.getState() instanceof Chest) {
                Chest chest = (Chest) blockAbove.getState();
                Sign sign = findAttachedSign(chest);

                Chest doubleChest = findDoubleChest(chest);
                if (sign == null && doubleChest != null) {
                    sign = findAttachedSign(doubleChest);
                }


                if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.noHoppers")));
                }
            }
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
