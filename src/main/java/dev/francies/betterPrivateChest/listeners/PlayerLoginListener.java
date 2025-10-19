package dev.francies.betterPrivateChest.listeners;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class PlayerLoginListener implements Listener {

    private final BetterPrivateChest plugin;

    public PlayerLoginListener(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("btpchest.admin")) {

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              //  plugin.checkForUpdates(player);
            }, 60L);
        }
    }
}
