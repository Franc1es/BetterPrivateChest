package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import dev.francies.betterPrivateChest.utils.DataFile;
import org.bukkit.ChatColor;
import org.bukkit.block.*;
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
    private final DataFile dataFile;

    public PrivateChestProtection(BetterPrivateChest plugin, DataFile dataFile) {
        this.plugin = plugin;
        this.dataFile = dataFile;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Container)) {
            return;
        }

        Container container = (Container) event.getClickedBlock().getState();
        Sign sign = findAttachedSign(container);

        Container doubleContainer = findDoubleContainer(container);
        if (sign == null && doubleContainer != null) {
            sign = findAttachedSign(doubleContainer);
        }

        if (sign == null || !sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")))) {
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            Sign sign = findAttachedSign(container);

            Container doubleContainer = findDoubleContainer(container);
            if (sign == null && doubleContainer != null) {
                sign = findAttachedSign(doubleContainer);
            }

            if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")))) {
                String ownerName = ChatColor.stripColor(sign.getLine(1));
                if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("btpchest.admin")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                    return;
                }

                saveContainerDestruction(player, container.getBlock(), "container");
            }
        }

        if (isBlockUnderContainer(block)) {
            Block blockAbove = block.getRelative(0, 1, 0);
            if (blockAbove.getState() instanceof Container) {
                Container containerAbove = (Container) blockAbove.getState();
                Sign sign = findAttachedSign(containerAbove);

                Container doubleContainer = findDoubleContainer(containerAbove);
                if (sign == null && doubleContainer != null) {
                    sign = findAttachedSign(doubleContainer);
                }

                if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")))) {
                    String ownerName = ChatColor.stripColor(sign.getLine(1));
                    if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("btpchest.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                    }
                }
            }
        }

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")))) {
                String ownerName = ChatColor.stripColor(sign.getLine(1));
                if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("btpchest.admin")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                }
            }
        }
    }

    private void saveContainerDestruction(Player player, Block block, String type) {
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

    private boolean isBlockUnderContainer(Block block) {
        Block blockAbove = block.getRelative(0, 1, 0);

        // Verifica se il blocco sopra è un contenitore
        if (blockAbove.getState() instanceof Container) {
            Container containerAbove = (Container) blockAbove.getState();

            // Verifica se il contenitore è protetto da un cartello
            Sign sign = findAttachedSign(containerAbove);

            // Gestione per chest doppi
            Container doubleContainer = findDoubleContainer(containerAbove);
            if (sign == null && doubleContainer != null) {
                sign = findAttachedSign(doubleContainer);
            }

            // Se il cartello esiste ed è valido, considera il contenitore protetto
            return sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("private-chest-id")));
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

    private Sign findAttachedSign(Container container) {
        Block[] neighbors = {
                container.getBlock().getRelative(1, 0, 0),
                container.getBlock().getRelative(-1, 0, 0),
                container.getBlock().getRelative(0, 0, 1),
                container.getBlock().getRelative(0, 0, -1)
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
}
