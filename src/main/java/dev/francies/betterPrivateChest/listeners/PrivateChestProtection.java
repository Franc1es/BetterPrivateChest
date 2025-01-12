package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrivateChestProtection implements Listener {
    private BetterPrivateChest plugin;


    public PrivateChestProtection(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        // Gestione delle porte
        if (isValidDoor(block)) {
            Sign sign = findAttachedSign(block);

            // Verifica se entrambe le metà della porta sono protette
            Block otherDoorHalf = getOtherDoorHalf(block);
            Sign otherSign = findAttachedSign(otherDoorHalf);

            if (isInteractionBlocked(player, sign) || isInteractionBlocked(player, otherSign)) {
                event.setCancelled(true);
                return;
            }
        }

        // Gestione dei contenitori
        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            Sign sign = findAttachedSign(container.getBlock());

            Container doubleContainer = findDoubleContainer(container);
            if (sign == null && doubleContainer != null) {
                sign = findAttachedSign(doubleContainer.getBlock());
            }

            if (isInteractionBlocked(player, sign)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Blocchi sotto casse o barili
        if (isBlockUnderProtectedContainer(block)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("prefix-private") + " " +
                            plugin.getConfig().getString("private-chest.break")));
            return;
        }

        // Gestione delle porte
        if (isValidDoor(block)) {
            Sign sign = findAttachedSign(block);

            // Verifica se entrambe le metà della porta sono protette
            Block otherDoorHalf = getOtherDoorHalf(block);
            Sign otherSign = findAttachedSign(otherDoorHalf);

            if (isBreakBlocked(player, sign) || isBreakBlocked(player, otherSign)) {
                event.setCancelled(true);
                return;
            }

            // saveContainerDestruction(player, block, "door");
        }

        // Gestione dei cartelli
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            // Verifichiamo se il cartello è protetto
            if (isBreakBlocked(player, sign)) {
                event.setCancelled(true);
                return;
            }
            // Se è consentito distruggerlo, salviamo l’evento di distruzione
          //  saveContainerDestruction(player, block, "sign");
        }

        // Gestione dei contenitori (casse, barili, ecc.)
        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            Sign sign = findAttachedSign(container.getBlock());

            Container doubleContainer = findDoubleContainer(container);
            if (sign == null && doubleContainer != null) {
                sign = findAttachedSign(doubleContainer.getBlock());
            }

            if (isBreakBlocked(player, sign)) {
                event.setCancelled(true);
                return;
            }

           // saveContainerDestruction(player, container.getBlock(), "container");
        }
    }

    private boolean isInteractionBlocked(Player player, Sign sign) {
        if (sign == null || !sign.getLine(0).equalsIgnoreCase(
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("private-chest-id")))) {
            return false;
        }

        String ownerName = ChatColor.stripColor(sign.getLine(1));
        String allowedPlayer1 = ChatColor.stripColor(sign.getLine(2));
        String allowedPlayer2 = ChatColor.stripColor(sign.getLine(3));
        String playerName = player.getName();

        if (!playerName.equalsIgnoreCase(ownerName) &&
                !playerName.equalsIgnoreCase(allowedPlayer1) &&
                !playerName.equalsIgnoreCase(allowedPlayer2) &&
                !player.hasPermission("btpchest.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("prefix-private") + " " +
                            plugin.getConfig().getString("private-chest.noPermission")
                                    .replace("%owner%", ownerName)));
            return true;
        }
        return false;
    }

    private boolean isBreakBlocked(Player player, Sign sign) {
        if (sign == null || !sign.getLine(0).equalsIgnoreCase(
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("private-chest-id")))) {
            return false;
        }

        String ownerName = ChatColor.stripColor(sign.getLine(1));
        if (!player.getName().equalsIgnoreCase(ownerName)
                && !player.hasPermission("btpchest.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("prefix-private") + " " +
                            plugin.getConfig().getString("private-chest.break")));
            return true;
        }
        return false;
    }

    private boolean isBlockUnderProtectedContainer(Block block) {
        Block blockAbove = block.getRelative(0, 1, 0);

        // Verifica se il blocco sopra è un contenitore protetto
        if (blockAbove.getState() instanceof Container) {
            Container container = (Container) blockAbove.getState();
            Sign sign = findAttachedSign(container.getBlock());

            Container doubleContainer = findDoubleContainer(container);
            if (sign == null && doubleContainer != null) {
                sign = findAttachedSign(doubleContainer.getBlock());
            }

            return sign != null && sign.getLine(0).equalsIgnoreCase(
                    ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("private-chest-id")));
        }
        return false;
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
                block.getRelative(0, 0, -1)
        };

        for (Block neighbor : neighbors) {
            if (neighbor.getState() instanceof Sign) {
                Sign sign = (Sign) neighbor.getState();
                if (sign.getLine(0).equalsIgnoreCase(
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("private-chest-id")))) {
                    return sign;
                }
            }
        }
        return null;
    }

    private boolean isValidDoor(Block block) {
        return block.getBlockData() instanceof Door;
    }

    private Block getOtherDoorHalf(Block block) {
        if (block.getBlockData() instanceof Door) {
            Door door = (Door) block.getBlockData();
            return block.getRelative(door.getHinge() == Door.Hinge.LEFT ? -1 : 1, 0, 0);
        }
        return block;
    }
}
