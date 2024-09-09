package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
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
            if (block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                if (isChestProtected(chest)) {
                    blocksToProtect.add(block);


                    Chest doubleChest = findDoubleChest(chest);
                    if (doubleChest != null) {
                        blocksToProtect.add(doubleChest.getBlock());
                    }
                }
            } else if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (isSignProtected(sign)) {
                    blocksToProtect.add(block);
                }
            }
        }

        event.blockList().removeAll(blocksToProtect);
    }


    private boolean isChestProtected(Chest chest) {
        Sign sign = findAttachedSign(chest);
        return sign != null && sign.getLines()[0].equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")));
    }

    private boolean isSignProtected(Sign sign) {
        return sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")));
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
                return (Sign) neighbor.getState();
            }
        }
        return null;
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


}
