package net.crashcraft.crashclaim.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.papermc.lib.PaperLib;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("claimstuck|stuck|unstuck")
@CommandPermission("crashclaim.user.claimstuck")
public class StuckCommand extends BaseCommand {
    private final ClaimDataManager manager;
    private final Map<UUID, Instant> stuckFirst = new HashMap<>();
    private final Map<UUID, Instant> lastUnstuck = new HashMap<>();

    public StuckCommand(ClaimDataManager manager){
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@players @nothing")
    public void onDefault(Player player){
        Location location = player.getLocation();
        Claim claim = manager.getClaim(location.getBlockX(), location.getBlockZ(), location.getWorld().getUID());
        if (claim != null){
            if (PermissionHelper.getPermissionHelper().hasPermission(player.getUniqueId(), player.getLocation(), PermissionRoute.BUILD)){
                player.sendMessage(Component.text("You cannot eject from claims you can build in.", NamedTextColor.RED));
                return;
            }
            int notified = 0;
            for(Player plr : Bukkit.getOnlinePlayers()){
                if(!plr.getUniqueId().equals(player.getUniqueId()) && plr.hasPermission("cmc.notifystuck") && !plr.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                    notified++;
                    plr.sendMessage(Component.text("[STUCK] " + player.getName() + " is stuck and needs help!", NamedTextColor.LIGHT_PURPLE));
                    plr.playSound(plr.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, SoundCategory.MASTER, 1.0f,1.0f);
                }
            }
            if(notified > 0){
                player.sendMessage(Component.text(notified + " online staff members were notified. Please wait for help. Abuse of this system (spam) will result in a ban.", NamedTextColor.GREEN));
                return;
            }
            if(lastUnstuck.containsKey(player.getUniqueId()) &&
                    lastUnstuck.get(player.getUniqueId()).isAfter(Instant.now().minus(30, ChronoUnit.MINUTES))) {
                player.sendMessage(Component.text("You can only unstuck once every 3 hours. If you need more help, contact a mod in the /discord.", NamedTextColor.RED));
                return;
            }
            if(stuckFirst.containsKey(player.getUniqueId())){
                if(stuckFirst.get(player.getUniqueId()).isAfter(Instant.now().minus(10, ChronoUnit.MINUTES))){
                    player.sendMessage(Component.text("10 minutes must elapse before you can run /stuck again. Please wait patiently.", NamedTextColor.YELLOW));
                    return;
                }else{
                    stuckFirst.remove(player.getUniqueId()); // GO
                }
            }else{
                stuckFirst.put(player.getUniqueId(), Instant.now());
                player.sendMessage(Component.text("In 10 minutes, you will be able to run /stuck again to be teleported out of this claim. This wait is to avoid abuse. If you need immediate help, request help in the /discord.", NamedTextColor.YELLOW));
                return;
            }
            Location otherLocation = player.getLocation();

            if (GlobalConfig.useCommandInsteadOfEdgeEject){
                player.performCommand(GlobalConfig.claimEjectCommand);
            } else {
                int distMax = Math.abs(location.getBlockX() - claim.getMaxX());
                int distMin = Math.abs(location.getBlockX() - claim.getMinX());

                World world = location.getWorld();
                if (distMax > distMin) {    //Find closest side
                    PaperLib.teleportAsync(player, new Location(world, claim.getMinX() - 1,
                            world.getHighestBlockYAt(claim.getMinX() - 1,
                                    location.getBlockZ()) + 1, location.getBlockZ()));
                } else {
                    PaperLib.teleportAsync(player, new Location(world, claim.getMaxX() + 1,
                            world.getHighestBlockYAt(claim.getMaxX() + 1,
                                    location.getBlockZ()) + 1, location.getBlockZ()));
                }
            }
            CrashClaim.getPlugin().getLogger().warning("[UNSTUCK] Sent " + player.getName() + " from " + otherLocation + " to " + player.getLocation() + " to unstuck them.");
            lastUnstuck.put(player.getUniqueId(), Instant.now());
            player.sendMessage(Component.text("You have been successfully removed from the claim.", NamedTextColor.GREEN));
        } else {
            player.spigot().sendMessage(Localization.EJECT__NO_CLAIM.getMessage(player));
        }
    }
}
