package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class PistonProtection implements Listener {
    private BetterPrivateChest plugin;

    public PistonProtection(BetterPrivateChest plugin) {
        this.plugin = plugin;
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
        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            return isContainerProtected(container);
        }

        if (isValidDoor(block)) {
            return isDoorProtected(block);
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
            if (neighbor.getState() instanceof Container) {
                Container container = (Container) neighbor.getState();
                if (isContainerProtected(container)) {
                    return true;
                }
            }
            if (isValidDoor(neighbor) && isDoorProtected(neighbor)) {
                return true;
            }
        }

        return false;
    }

    private boolean isContainerProtected(Container container) {
        InventoryHolder holder = container.getInventory().getHolder();

        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            Container leftContainer = (Container) doubleChest.getLeftSide();
            Container rightContainer = (Container) doubleChest.getRightSide();

            return isSignAttached(leftContainer) || isSignAttached(rightContainer);
        }

        return isSignAttached(container);
    }

    private boolean isSignAttached(Container container) {
        Sign sign = findAttachedSign(container.getBlock());
        return sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
    }

    private boolean isDoorProtected(Block block) {
        Sign sign = findAttachedSign(block);
        return sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
    }

    private Sign findAttachedSign(Block block) {
        Block[] neighbors = {
                block.getRelative(1, 0, 0),
                block.getRelative(-1, 0, 0),
                block.getRelative(0, 0, 1),
                block.getRelative(0, 0, -1),
                block.getRelative(0, 1, 0),
                block.getRelative(0, -1, 0)
        };

        for (Block neighbor : neighbors) {
            if (neighbor.getState() instanceof Sign) {
                Sign sign = (Sign) neighbor.getState();
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")))) {
                    return sign;
                }
            }
        }
        return null;
    }

    private boolean isValidDoor(Block block) {
        return block.getBlockData() instanceof Door;
    }
}
