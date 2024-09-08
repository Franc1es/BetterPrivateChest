package dev.francies.betterPrivateChest.listeners;


import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;

public class PrivateChestProtection implements Listener {
    private BetterPrivateChest plugin;

    public PrivateChestProtection(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Chest)) {
            return;
        }

        Chest chest = (Chest) event.getClickedBlock().getState();
        Sign sign = findAttachedSign(chest);


        if (sign == null || !sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.noPermission")));
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Se il giocatore ha il permesso di bypassare, consenti l'azione
        if (player.hasPermission("private.bypass")) {
            return;
        }

        // Controlla se il blocco rotto è una chest protetta
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Sign sign = findAttachedSign(chest);
            if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                return;
            }
        }

        // Controlla se il blocco rotto è un cartello collegato a una chest protetta
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
                String ownerName = ChatColor.stripColor(sign.getLine(1));
                if (!player.getName().equalsIgnoreCase(ownerName) && !player.hasPermission("private.bypass")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
                    return;
                }
            }
        }

        // Controlla se il blocco rotto è sotto una chest protetta
        Block blockAbove = block.getRelative(org.bukkit.block.BlockFace.UP);
        if (blockAbove.getState() instanceof Chest) {
            Chest chestAbove = (Chest) blockAbove.getState();
            Sign sign = findAttachedSign(chestAbove);
            if (sign != null && sign.getLine(0).equalsIgnoreCase(ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.break")));
            }
        }
    }

    // Funzione per trovare il cartello collegato a una chest
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
