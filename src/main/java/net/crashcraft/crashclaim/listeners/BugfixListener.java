package net.crashcraft.crashclaim.listeners;

import dev.whip.crashutils.menusystem.CrashGuiHolder;
import dev.whip.crashutils.menusystem.GUI;
import net.crashcraft.crashclaim.CrashClaim;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BugfixListener implements Listener {
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof CrashGuiHolder) {
            CrashGuiHolder holder = (CrashGuiHolder)e.getInventory().getHolder();
            if (!holder.getPlugin().equals(CrashClaim.getPlugin())) {
                return;
            }

            GUI gui = holder.getManager();
            if (gui.isLockGUI() && e.getClick() == ClickType.NUMBER_KEY) {
                e.setCancelled(true);
            }
        }
    }
}
