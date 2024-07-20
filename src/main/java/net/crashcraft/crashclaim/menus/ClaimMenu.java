package net.crashcraft.crashclaim.menus;

import dev.whip.crashutils.menusystem.GUI;
import dev.whip.crashutils.menusystem.defaultmenus.ConfirmationMenu;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.claimobjects.Claim;
import net.crashcraft.crashclaim.claimobjects.SubClaim;
import net.crashcraft.crashclaim.config.GlobalConfig;
import net.crashcraft.crashclaim.localization.Localization;
import net.crashcraft.crashclaim.menus.helpers.InputPrompt;
import net.crashcraft.crashclaim.menus.list.PlayerPermListMenu;
import net.crashcraft.crashclaim.menus.list.SubClaimListMenu;
import net.crashcraft.crashclaim.menus.list.TransferOwnershipListMenu;
import net.crashcraft.crashclaim.menus.permissions.SimplePermissionMenu;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClaimMenu extends GUI {
    private final Claim claim;
    private final PermissionHelper helper;
    private final GUI previousMenu;

    private boolean hasSubClaims;

    public ClaimMenu(Player player, Claim claim, GUI previousMenu) {
        super(player, BaseComponent.toLegacyText(Localization.MENU__CLAIM__TITLE.getMessage(null)), 54);
        this.claim = claim;
        this.previousMenu = previousMenu;
        this.helper = PermissionHelper.getPermissionHelper();
        setupGUI();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void loadItems() {
        hasSubClaims = false;

        ItemStack descItem;
        if (claim.getOwner().equals(getPlayer().getUniqueId())){
            descItem = Localization.MENU__GENERAL__CLAIM_ITEM_NO_OWNER.getItem(player,
                    "name", claim.getName(),
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "lower_bound_y", Integer.toString(claim.getLowerBoundY()),
                    "world", Bukkit.getWorld(claim.getWorld()).getName()
            );
        } else {
            descItem = Localization.MENU__GENERAL__CLAIM_ITEM.getItem(player,
                    "name", claim.getName(),
                    "min_x", Integer.toString(claim.getMinX()),
                    "min_z", Integer.toString(claim.getMinZ()),
                    "max_x", Integer.toString(claim.getMaxX()),
                    "max_z", Integer.toString(claim.getMaxZ()),
                    "lower_bound_y", Integer.toString(claim.getLowerBoundY()),
                    "world", Bukkit.getWorld(claim.getWorld()).getName(),
                    "owner", Bukkit.getOfflinePlayer(claim.getOwner()).getName()
            );
        }

        descItem.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));
        inv.setItem(13, descItem);

        if(claim.getOwner().equals(getPlayer().getUniqueId()) || PermissionHelper.getPermissionHelper().getBypassManager().isBypass(getPlayer().getUniqueId())){
            inv.setItem(22, Localization.MENU__CLAIM__TRANSFER.getItem(player));
        }

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
            inv.setItem(28, Localization.MENU__PERMISSIONS__BUTTONS__PER_PLAYER.getItem(player));
            inv.setItem(29, Localization.MENU__PERMISSIONS__BUTTONS__GLOBAL.getItem(player));
        } else {
            inv.setItem(28, Localization.MENU__PERMISSIONS__BUTTONS__PER_PLAYER_DISABLED.getItem(player));
            inv.setItem(29, Localization.MENU__PERMISSIONS__BUTTONS__GLOBAL_DISABLED.getItem(player));
        }

        if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
            inv.setItem(32, Localization.MENU__PERMISSIONS__BUTTONS__RENAME.getItem(player));
            inv.setItem(33, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY.getItem(player));
            inv.setItem(34, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_EXIT.getItem(player));
            inv.setItem(42, Localization.MENU__PERMISSIONS__BUTTONS__SET_LOWER_BOUND_Y.getItem(player));
            inv.setItem(49, Localization.MENU__PERMISSIONS__BUTTONS__DELETE.getItem(player));
        } else {
            inv.setItem(32, Localization.MENU__PERMISSIONS__BUTTONS__RENAME_DISABLED.getItem(player));
            inv.setItem(33, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_ENTRY_DISABLED.getItem(player));
            inv.setItem(34, Localization.MENU__PERMISSIONS__BUTTONS__EDIT_EXIT_DISABLED.getItem(player));
            inv.setItem(42, Localization.MENU__PERMISSIONS__BUTTONS__SET_LOWER_BOUND_Y_DISABLED.getItem(player));
            inv.setItem(49, Localization.MENU__PERMISSIONS__BUTTONS__DELETE_DISABLED.getItem(player));
        }

        hasSubClaims = false;
        for (SubClaim subClaim : claim.getSubClaims()){
            if (helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)
                    || helper.hasPermission(subClaim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                inv.setItem(30, Localization.MENU__PERMISSIONS__BUTTONS_SUBCLAIMS.getItem(player));
                hasSubClaims = true;
                break;
            }
        }
        if (!hasSubClaims){
            inv.setItem(30, Localization.MENU__PERMISSIONS__BUTTONS_NO_SUBCLAIMS.getItem(player));
        }

        if (previousMenu != null){
            inv.setItem(45, Localization.MENU__GENERAL__BACK_BUTTON.getItem(player));
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event, String rawItemName) {
        switch (event.getSlot()){
            case 22:
                if(claim.getOwner().equals(getPlayer().getUniqueId()) || PermissionHelper.getPermissionHelper().getBypassManager().isBypass(getPlayer().getUniqueId())){
                    if(event.getClick().isLeftClick()){
                        new TransferOwnershipListMenu(claim, getPlayer(), this);
                    }else if(event.getClick().isRightClick()){
                        forceClose();
                        InputPrompt.of(getPlayer(), LegacyComponentSerializer.legacyAmpersand().serialize(Localization.MENU__CLAIM__TRANSFER__MESSAGE.getComponent(player)),
                                (input) -> {
                                    OfflinePlayer playerLookup = Bukkit.getOfflinePlayerIfCached(input);
                                    if(playerLookup != null && (playerLookup.hasPlayedBefore() || playerLookup.isOnline())) {
                                        return true;
                                    }else {
                                        player.spigot().sendMessage(Localization.MENU__CLAIM__TRANSFER_BAD_USERNAME__MESSAGE.getMessage(player));
                                        return false;
                                    }
                                }, (result) -> {
                                    if(result == null) {
                                        Players.msg(player, "&cTransfer timed out.");
                                        return;
                                    }
                                    OfflinePlayer playerLookup = Bukkit.getOfflinePlayerIfCached(result);
                                    if(playerLookup != null && (playerLookup.hasPlayedBefore() || playerLookup.isOnline())) {
                                        TransferOwnershipListMenu.handleTransferConfirmation(getPlayer(), getPlayer().getUniqueId(), claim, playerLookup.getUniqueId());
                                    }else{
                                        player.spigot().sendMessage(Localization.MENU__CLAIM__TRANSFER_BAD_USERNAME_FATAL__MESSAGE.getMessage(player));
                                    }
                                }).start();
                    }
                }else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 28:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new PlayerPermListMenu(claim, getPlayer(), this);
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 29:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                    new SimplePermissionMenu(player, claim, null, this).open();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 30:
                if (hasSubClaims) {
                    new SubClaimListMenu(getPlayer(), this, claim).open();
                }
                break;
            case 32:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    forceClose();
                    InputPrompt.of(getPlayer(), LegacyComponentSerializer.legacyAmpersand().serialize(Localization.MENU__CLAIM__RENAME__MESSAGE.getComponent(player)),
                            (input) -> {
                                return true;
                            }, (result) -> {
                                if(result == null) {
                                    Players.msg(player, "&cRename timed out.");
                                    return;
                                }
                                claim.setName(result);
                                player.spigot().sendMessage(Localization.MENU__CLAIM__RENAME__CONFIRMATION.getMessage(player,
                                        "name", result));
                            }).start();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 33:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    forceClose();
                    InputPrompt.of(getPlayer(), LegacyComponentSerializer.legacyAmpersand().serialize(Localization.MENU__CLAIM__ENTRY_MESSAGE__MESSAGE.getComponent(player)),
                            (input) -> {
                                return true;
                            }, (result) -> {
                                if(result == null) {
                                    Players.msg(player, "&cRename timed out.");
                                    return;
                                }
                                if(result.equalsIgnoreCase("remove") || result.equalsIgnoreCase("none") || result.equalsIgnoreCase("clear")){
                                    result = null;
                                }
                                claim.setEntryMessage(result);
                                player.spigot().sendMessage(Localization.MENU__CLAIM__ENTRY_MESSAGE__CONFIRMATION.getMessage(player,
                                        "entry_message", result == null?"<nothing>":result));
                            }).start();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 34:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    forceClose();
                    InputPrompt.of(getPlayer(), LegacyComponentSerializer.legacyAmpersand().serialize(Localization.MENU__CLAIM__EXIT_MESSAGE__MESSAGE.getComponent(player)),
                            (input) -> {
                                return true;
                            }, (result) -> {
                                if(result == null) {
                                    Players.msg(player, "&cRename timed out.");
                                    return;
                                }
                                if(result.equalsIgnoreCase("remove") || result.equalsIgnoreCase("none") || result.equalsIgnoreCase("clear")){
                                    result = null;
                                }
                                claim.setExitMessage(result);
                                player.spigot().sendMessage(Localization.MENU__CLAIM__EXIT_MESSAGE__CONFIRMATION.getMessage(player,
                                        "exit_message", result == null?"<nothing>":result));
                            }).start();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 42: // lower bound y
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    forceClose();
                    World world = Bukkit.getWorld(claim.getWorld());
                    if(world == null){
                        Players.msg(player, "&cFailed to load world for claim - cancelled.");
                        return;
                    }
                    InputPrompt.of(getPlayer(), LegacyComponentSerializer.legacyAmpersand().serialize(Localization.MENU__CLAIM__LOWER_BOUND_Y__MESSAGE.getComponent(player,
                                    "min_height", Integer.toString(world.getMinHeight()),
                                    "max_height", Integer.toString(world.getMaxHeight()))),
                            (input) -> {
                                try{
                                    int newBound = Integer.parseInt(input);
                                    if(newBound > world.getMaxHeight() || newBound < world.getMinHeight()){
                                        return false;
                                    }
                                }catch (Exception e){
                                    return false;
                                }
                                return true;
                            }, (result) -> {
                                if(result == null) {
                                    Players.msg(player, "&cLower Bound Y adjustment timed out.");
                                    return;
                                }
                                claim.setLowerBoundY(Integer.parseInt(result));
                                player.spigot().sendMessage(Localization.MENU__CLAIM__LOWER_BOUND_Y__CONFIRMATION.getMessage(player,
                                        "lower_bound_y", result));
                            }).start();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 49:
                if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_CLAIM)) {
                    ItemStack message = Localization.UN_CLAIM__MENU__CONFIRMATION__MESSAGE.getItem(player);
                    message.setType(GlobalConfig.visual_menu_items.getOrDefault(claim.getWorld(), Material.OAK_FENCE));

                    new ConfirmationMenu(player,
                            Localization.UN_CLAIM__MENU__CONFIRMATION__TITLE.getMessage(player),
                            message,
                            Localization.UN_CLAIM__MENU__CONFIRMATION__ACCEPT.getItem(player),
                            Localization.UN_CLAIM__MENU__CONFIRMATION__DENY.getItem(player),
                            (player, aBoolean) -> {
                                if (aBoolean) {
                                    if (helper.hasPermission(claim, getPlayer().getUniqueId(), PermissionRoute.MODIFY_PERMISSIONS)) {
                                        CrashClaim.getPlugin().getDataManager().deleteClaim(claim);
                                    } else {
                                        player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                                    }
                                }
                                return "";
                            }, player -> "").open();
                } else {
                    player.spigot().sendMessage(Localization.MENU__GENERAL__INSUFFICIENT_PERMISSION.getMessage(player));
                    forceClose();
                }
                break;
            case 45:
                if (previousMenu == null){
                    return;
                }
                previousMenu.open();
                break;
        }
    }

    public GUI getPreviousMenu() {
        return previousMenu;
    }
}
