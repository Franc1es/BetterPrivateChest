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
        String[] lines = event.getLines();

        // Verifica se è il cartello "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ"
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ")) {
            return; // Esci se non è un cartello privato
        }

        // Solo il proprietario può modificare le righe 2 e 3
        if (player.getName().equalsIgnoreCase(ChatColor.stripColor(sign.getLine(1)))) {
            // Mantieni inalterate le righe 0 e 1
            event.setLine(0, sign.getLine(0)); // Mantieni la riga 0 (ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ)
            event.setLine(1, sign.getLine(1)); // Mantieni la riga 1 (nome del proprietario)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.cannotChange")));
            // Permetti al proprietario di modificare solo le righe 2 e 3
            if (!lines[2].isEmpty()) {
                event.setLine(2, ChatColor.AQUA + ChatColor.stripColor(lines[2])); // Colora sempre la riga 2 in Aqua
            }

            if (!lines[3].isEmpty()) {
                event.setLine(3, ChatColor.AQUA + ChatColor.stripColor(lines[3])); // Colora sempre la riga 3 in Aqua
            }
        } else {
            // Blocca le modifiche a tutte le righe se non sei il proprietario
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.cannotChange")));
        }
    }
}
