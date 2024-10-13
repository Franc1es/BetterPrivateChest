package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
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

    // Gestisce il piazzamento delle Respawn Anchors
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        Player player = event.getPlayer();

        // Raggio dal config
        int protectionRadius = plugin.getConfig().getInt("protection-radius");

        // Controlla se il blocco piazzato è un'Ancora della Rigenerazione
        if (placedBlock.getType() == Material.RESPAWN_ANCHOR) {
            // Cerca i bauli vicini protetti
            boolean protectedChestNearby = isProtectedChestNearby(placedBlock, protectionRadius);

            if (protectedChestNearby) {
                String message = ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("prefix-private") + " " +
                                plugin.getConfig().getString("private-chest.block-place-denied-message"))
                        .replace("%block%", placedBlock.getType().toString().toLowerCase().replace("_", " "));

                event.setCancelled(true);
                player.sendMessage(message);
            }
        }
    }

    // Gestisce il piazzamento degli End Crystals
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Controlla se l'azione è un click destro su un blocco con la mano principale
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.END_CRYSTAL) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null) {
                    // Raggio dal config
                    int protectionRadius = plugin.getConfig().getInt("protection-radius");

                    boolean protectedChestNearby = isProtectedChestNearby(clickedBlock, protectionRadius);

                    if (protectedChestNearby) {
                        String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " +
                                        plugin.getConfig().getString("private-chest.block-place-denied-message"))
                                .replace("%block%", item.getType().toString().toLowerCase().replace("_", " "));

                        event.setCancelled(true);
                        event.getPlayer().sendMessage(message);
                    }
                }
            }
        }
    }

    // Metodo per verificare se c'è un baule protetto vicino al blocco
    private boolean isProtectedChestNearby(Block block, int radius) {
        Vector blockPosition = block.getLocation().toVector();

        // Controlla i blocchi all'interno del raggio specificato
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block nearbyBlock = block.getRelative(x, y, z);

                    // Se il blocco vicino è un baule e protetto, ritorna true
                    if (nearbyBlock.getState() instanceof Chest) {
                        Chest chest = (Chest) nearbyBlock.getState();
                        if (isChestProtected(chest)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Metodo che verifica se il baule è protetto tramite un cartello
    private boolean isChestProtected(Chest chest) {
        Sign sign = findAttachedSign(chest);
        return sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
    }

    // Metodo che trova il cartello attaccato al baule
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
}
