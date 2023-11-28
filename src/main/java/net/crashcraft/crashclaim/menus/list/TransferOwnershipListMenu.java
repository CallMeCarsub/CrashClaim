package net.crashcraft.crashclaim.menus.list;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import dev.whip.crashutils.menusystem.defaultmenus.PlayerListMenu;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.BaseClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.PermState;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.claimobjects.permission.PlayerPermissionSet;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.permissions.SimplePermissionMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

public class TransferOwnershipListMenu {
    public TransferOwnershipListMenu(BaseClaim baseClaim, Player viewer, GUI previous){
        if (baseClaim instanceof SubClaim){
            //Try and close an inventory, want to close a loose end just in case
            viewer.closeInventory();
            return;
        }
        Claim claim = (Claim) baseClaim;
        if (!claim.getOwner().equals(viewer.getUniqueId()) && !PermissionHelper.getPermissionHelper().getBypassManager().isBypass(viewer.getUniqueId())){
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

        uuids.remove(claim.getOwner());
        if(!uuids.contains(viewer.getUniqueId())) uuids.add(viewer.getUniqueId());

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

        new PlayerListMenu(BaseComponent.toLegacyText(Localization.MENU_LIST_TRANSFER_OWNER__TITLE.getMessage(null)), viewer, previous, finalUUIDS, (gui, uuid) -> {
            //new SimplePermissionMenu(viewer, claim, uuid, gui).open();
            ItemStack message = Localization.TRANSFER_OWNER__MENU__CONFIRMATION__MESSAGE.getItem(viewer);
            new ConfirmationMenu(viewer,
                    Localization.TRANSFER_OWNER__MENU__CONFIRMATION__TITLE.getMessage(viewer),
                    message,
                    Localization.TRANSFER_OWNER__MENU__CONFIRMATION__ACCEPT.getItem(viewer),
                    Localization.TRANSFER_OWNER__MENU__CONFIRMATION__DENY.getItem(viewer),
                    (player, aBoolean) -> {
                        if (aBoolean) {
                            boolean transferByOwner = claim.getOwner().equals(viewer.getUniqueId());
                            if (transferByOwner|| PermissionHelper.getPermissionHelper().getBypassManager().isBypass(viewer.getUniqueId())) {
                                if(transferByOwner) {
                                    // ensure the owner keeps perms if they're transferring
                                    // but if its staff bypass, just ignore.
                                    PlayerPermissionSet ownerSet = claim.getPerms().getPlayerPermissions().get(claim.getOwner());
                                    ownerSet.setDefaultConatinerValue(PermState.ENABLED);
                                    ownerSet.setBuild(PermState.ENABLED);
                                    ownerSet.setModifyClaim(PermState.ENABLED);
                                    ownerSet.setEntities(PermState.ENABLED);
                                    ownerSet.setInteractions(PermState.ENABLED);
                                    ownerSet.setModifyPermissions(PermState.ENABLED);
                                    ownerSet.setTeleportation(PermState.ENABLED);
                                    ownerSet.setViewSubClaims(PermState.ENABLED);
                                }else{
                                    // remove old owner perms for staff transfers
                                    claim.getPerms().getPlayerPermissions().remove(claim.getOwner());
                                }
                                claim.getContribution().put(uuid, claim.getContribution().remove(claim.getOwner()));
                                OfflinePlayer originalOwner = Bukkit.getOfflinePlayer(claim.getOwner());
                                OfflinePlayer newOwner = Bukkit.getOfflinePlayer(uuid);
                                claim.setOwner(uuid);
                                claim.setToSave(true);

                                CrashClaim.getPlugin().getLogger().info("[CLAIM TRANSFER] Ownership of claim " + claim.getId() + " transferred from " +
                                        originalOwner.getName() + " (" + originalOwner.getUniqueId() + ") to " +
                                        newOwner.getName() + " (" + newOwner.getUniqueId() + ") | Transfer performed by " + viewer.getName() + " (" + viewer.getUniqueId() + ")");
                                player.spigot().sendMessage(Localization.TRANSFER_OWNER__SUCCESS.getMessage(player));
                            } else {
                                player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                            }
                        }
                        return "";
                    }, player -> "").open();
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
