package net.crashcraft.crashclaim.menus.list;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.PlayerListMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.claimobjects.permission.parent.ParentPermissionGroup;
import net.crashcraft.crashclaim.data.ClaimDataManager;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.permissions.SimplePermissionMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class PlayerPermListMenu {
    public PlayerPermListMenu(BaseClaim claim, Player viewer, GUI previous){
        if (!PermissionHelper.getPermissionHelper().hasPermission(claim, viewer.getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)){
            viewer.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(viewer));
            //Try and close an inventory, want to close a loose end just in case
            viewer.closeInventory();
            return;
        }

        ArrayList<UUID> uuids = new ArrayList<>(claim.getPerms().getPlayerPermissions().keySet());

        // get rid of any permission entries that do fucking nothing
        uuids.removeIf(uuid -> {
            PlayerPermissionSet permissionSet = claim.getPerms().getPlayerPermissions().get(uuid);
            return permissionSet == null || permissionSet.isDefault();
        });

        for (Player player : Bukkit.getOnlinePlayers()){
            if (isVanished(player) || !player.getWorld().getUID().equals(claim.getWorld())){
                continue;
            }

            if (!uuids.contains(player.getUniqueId())) {
                Claim claimAt = CrashClaim.getPlugin().getDataManager().getClaim(player.getLocation());
                if(claimAt != null && claimAt.getId() == claim.getId()) {
                    uuids.add(player.getUniqueId());
                }
            }
        }

        uuids.remove(viewer.getUniqueId());    //Cant modify perms of yourself

        if (claim instanceof SubClaim){ //Owners permissions are off limits.
            SubClaim temp = (SubClaim) claim;
            uuids.remove(temp.getParent().getOwner());
        } else if (claim instanceof Claim){
            Claim temp = (Claim) claim;
            uuids.remove(temp.getOwner());
        } else {
            throw new RuntimeException("Claim was not of known type.");
        }

        Map<String, UUID> names = new HashMap<>();
        List<String> nameList = new ArrayList<>();
        uuids.forEach(uuid -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            names.put(offlinePlayer.getName(), uuid);
            nameList.add(offlinePlayer.getName());
        });
        nameList.sort(Comparator.comparing(s -> s.toLowerCase(Locale.ENGLISH)));
        ArrayList<UUID> finalUUIDS = new ArrayList<>();
        nameList.forEach(name -> finalUUIDS.add(names.get(name)));

        new PlayerListMenu(BaseComponent.toLegacyText(Localization.MENU__LIST_PLAYERS__TITLE.getMessage(null)), viewer, previous, finalUUIDS, (gui, uuid) -> {
            new SimplePermissionMenu(viewer, claim, uuid, gui).open();
            return "";
        }).open();
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}
