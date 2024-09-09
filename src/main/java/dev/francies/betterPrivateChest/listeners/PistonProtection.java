package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import dev.francies.betterPrivateChest.utils.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class PistonProtection implements Listener {
    private BetterPrivateChest plugin;
    private final DataFile dataFile;

    public PistonProtection(BetterPrivateChest plugin, DataFile dataFile) {
        this.plugin = plugin;
        this.dataFile = dataFile;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Block> pushedBlocks = event.getBlocks();
        for (Block block : pushedBlocks) {
            if (isProtectedBlock(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        List<Block> pulledBlocks = event.getBlocks();
        for (Block block : pulledBlocks) {
            if (isProtectedBlock(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isProtectedBlock(Block block) {
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            return isChestProtected(chest);
        }

        Block[] neighbors = {
                block.getRelative(1, 0, 0),
                block.getRelative(-1, 0, 0),
                block.getRelative(0, 0, 1),
                block.getRelative(0, 0, -1),
                block.getRelative(0, 1, 0),
                block.getRelative(0, -1, 0)
        };
        for (Block neighbor : neighbors) {
            if (neighbor.getState() instanceof Chest) {
                Chest chest = (Chest) neighbor.getState();
                if (isChestProtected(chest)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isChestProtected(Chest chest) {
        InventoryHolder holder = chest.getInventory().getHolder();

        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            Chest leftChest = (Chest) doubleChest.getLeftSide();
            Chest rightChest = (Chest) doubleChest.getRightSide();

            return isSignAttached(leftChest) || isSignAttached(rightChest);
        }

        return isSignAttached(chest);
    }


    private boolean isSignAttached(Chest chest) {
        Sign sign = findAttachedSign(chest);
        return sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")));
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
