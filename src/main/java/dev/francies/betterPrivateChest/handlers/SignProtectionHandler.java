package dev.francies.betterPrivateChest.handlers;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignProtectionHandler implements Listener {
    private BetterPrivateChest plugin;

    public SignProtectionHandler(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Sign sign = (Sign) event.getBlock().getState();
        String[] originalLines = sign.getLines();
        String[] newLines = event.getLines();

        if (!ChatColor.translateAlternateColorCodes('&', originalLines[0]).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString( "private-chest-id")))) {
            return;
        }

        if (player.getName().equalsIgnoreCase(ChatColor.stripColor(originalLines[1]))) {
            boolean isLine0Modified = !newLines[0].equals(originalLines[0]);
            boolean isLine1Modified = !newLines[1].equals(originalLines[1]);

            if (isLine0Modified || isLine1Modified) {
                event.setLine(0, originalLines[0]);
                event.setLine(1, originalLines[1]);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.cannotChange")));
            }

            String line2 = ChatColor.stripColor(newLines[2]);
            String line3 = ChatColor.stripColor(newLines[3]);

            if (line2.isEmpty() && line3.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.twoMorePlayers")));
            } else {
                if (line2.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.oneMorePlayer")));
                } else {
                    event.setLine(2, ChatColor.AQUA + line2);
                }

                if (line3.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.oneMorePlayer")));
                } else {

                    if (line2.equalsIgnoreCase(line3)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.sameName")));
                        event.setLine(3, ChatColor.AQUA + line3);
                        event.setLine(2, ChatColor.AQUA + line2);
                    } else {
                        event.setLine(3, ChatColor.AQUA + line3);
                        event.setLine(2, ChatColor.AQUA + line2);
                    }
                }
            }
        } else {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.cannotModify").replace("%owner%", ChatColor.stripColor(originalLines[1]))));
        }
    }

}
