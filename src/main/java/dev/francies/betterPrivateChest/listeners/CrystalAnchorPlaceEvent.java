package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CrystalAnchorPlaceEvent implements Listener {
    private BetterPrivateChest plugin;

    public CrystalAnchorPlaceEvent(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        Player player = event.getPlayer();

        int protectionRadius = plugin.getConfig().getInt("protection-radius");

        if (placedBlock.getType() == Material.RESPAWN_ANCHOR) {
            boolean protectedNearby = isProtectedNearby(placedBlock, protectionRadius);

            if (protectedNearby) {
                String message = ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("prefix-private") + " " +
                                        plugin.getConfig().getString("private-chest.block-place-denied-message"))
                        .replace("%block%", placedBlock.getType().toString().toLowerCase().replace("_", " "));

                event.setCancelled(true);
                player.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.END_CRYSTAL) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null) {

                    int protectionRadius = plugin.getConfig().getInt("protection-radius");

                    boolean protectedNearby = isProtectedNearby(clickedBlock, protectionRadius);

                    if (protectedNearby) {
                        String message = ChatColor.translateAlternateColorCodes('&',
                                        plugin.getConfig().getString("prefix-private") + " " +
                                                plugin.getConfig().getString("private-chest.block-place-denied-message"))
                                .replace("%block%", item.getType().toString().toLowerCase().replace("_", " "));

                        event.setCancelled(true);
                        event.getPlayer().sendMessage(message);
                    }
                }
            }
        }
    }


    private boolean isProtectedNearby(Block block, int radius) {
        Vector blockPosition = block.getLocation().toVector();


        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block nearbyBlock = block.getRelative(x, y, z);


                    if (nearbyBlock.getState() instanceof Container) {
                        Container container = (Container) nearbyBlock.getState();
                        if (isContainerProtected(container)) {
                            return true;
                        }
                    }


                    if (isValidDoor(nearbyBlock) && isDoorProtected(nearbyBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean isContainerProtected(Container container) {
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
                return (Sign) neighbor.getState();
            }
        }
        return null;
    }


    private boolean isValidDoor(Block block) {
        return block.getBlockData() instanceof Door;
    }
}
