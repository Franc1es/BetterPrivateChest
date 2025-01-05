package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class ExplodeProtection implements Listener {
    private BetterPrivateChest plugin;

    public ExplodeProtection(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocksToProtect = new ArrayList<>();

        for (Block block : event.blockList()) {
            if (block.getState() instanceof Container) {
                Container container = (Container) block.getState();
                if (isContainerProtected(container)) {
                    blocksToProtect.add(block);

                    Container doubleContainer = findDoubleContainer(container);
                    if (doubleContainer != null) {
                        blocksToProtect.add(doubleContainer.getBlock());
                    }
                }
            } else if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (isSignProtected(sign)) {
                    blocksToProtect.add(block);
                }
            } else if (isValidDoor(block)) {
                if (isDoorProtected(block)) {
                    blocksToProtect.add(block);
                }
            }
        }

        event.blockList().removeAll(blocksToProtect);
    }

    private boolean isContainerProtected(Container container) {
        Sign sign = findAttachedSign(container.getBlock());
        return sign != null && sign.getLines()[0].equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
    }

    private boolean isSignProtected(Sign sign) {
        return sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
    }

    private boolean isDoorProtected(Block block) {
        Sign sign = findAttachedSign(block);
        return sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
    }

    private Container findDoubleContainer(Container container) {
        InventoryHolder holder = container.getInventory().getHolder();

        if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;

            Container leftContainer = (Container) doubleChest.getLeftSide();
            Container rightContainer = (Container) doubleChest.getRightSide();

            if (!leftContainer.getLocation().equals(container.getLocation())) {
                return leftContainer;
            } else {
                return rightContainer;
            }
        }

        return null;
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
