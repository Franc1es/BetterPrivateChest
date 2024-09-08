package dev.francies.betterPrivateChest.handlers;

import dev.francies.betterPrivateChest.BetterPrivateChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignCreationHandler implements Listener {
    private BetterPrivateChest plugin;

    public SignCreationHandler(BetterPrivateChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        Block block = event.getBlock();

        // Verifica che il cartello sia di tipo "WallSign" o "Sign"
        if (!(block.getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) block.getState();

        // Se il cartello è "[privata]" o "[privato]", trattalo come un cartello di chest privata
        if (lines[0].equalsIgnoreCase("[privata]") || lines[0].equalsIgnoreCase("[privato]")) {
            Block attachedBlock = block.getRelative(((org.bukkit.block.data.type.WallSign) sign.getBlockData()).getFacing().getOppositeFace());

            // Assicurati che il blocco collegato sia una chest
            if (!(attachedBlock.getState() instanceof Chest)) {
                return;
            }

            // Se il cartello è nuovo, imposta il proprietario
            if (ChatColor.stripColor(lines[1]).isEmpty()) {
                String ownerName = player.getName();
                event.setLine(0, ChatColor.RED + "ʙᴀᴜʟᴇ ᴘʀɪᴠᴀᴛᴏ");
                event.setLine(1, ChatColor.GREEN + ChatColor.BOLD.toString() + ownerName);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix-private") + " " + plugin.getConfig().getString("private-chest.success")));
            }
        }
    }
}
