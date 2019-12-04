package net.crashcraft.whipclaim.claimobjects;

import net.crashcraft.whipclaim.permissions.PermissionRoute;
import org.bukkit.Material;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class PermissionGroup implements Serializable {
    private static final long serialVersionUID = 30L;

    /**
     * Base Claim will have all perms
     * Sub Claim will have all perms except for admin as that gets inherited
     */

    private PermissionSet globalPermissionSet;
    private HashMap<UUID, PermissionSet> playerPermissions;

    private BaseClaim owner;

    public PermissionGroup(){

    }

    public PermissionGroup(BaseClaim owner, PermissionSet globalPermissionSet, HashMap<UUID, PermissionSet> playerPermissions) {
        this.owner = owner;
        this.globalPermissionSet = globalPermissionSet == null ?
                new PermissionSet(PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE,
                        PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, PermState.DISABLE, new HashMap<>()) : globalPermissionSet;
        this.playerPermissions = playerPermissions == null ? new HashMap<>() : playerPermissions ;
    }

    public PermissionSet getPermissionSet() {
        return globalPermissionSet;
    }

    public PermissionSet getPlayerPermissionSet(UUID id) {
        return playerPermissions.get(id);
    }

    //Used for fixing owner permissions only
    public void setPlayerPermissionSet(UUID uuid, PermissionSet permissionSet) {
        playerPermissions.put(uuid, permissionSet);

        if (owner instanceof Claim){
            Claim claim = (Claim) owner;
            claim.setToSave(true);
        } else if (owner instanceof SubClaim){
            SubClaim claim = (SubClaim) owner;
            claim.getParent().setToSave(true);
        }
    }

    public HashMap<UUID, PermissionSet> getPlayerPermissions(){
        return playerPermissions;
    }

    public void setOwner(BaseClaim owner){
        this.owner = owner;
    }

    public void setPermission(PermissionRoute route, int value){
        route.setPerm(globalPermissionSet, value);
    }

    public void setPlayerPermission(UUID uuid, PermissionRoute route, int value){
        route.setPerm(getPlayerPermissionSet(uuid), value);
    }

    public void setContainerPermission(PermissionRoute route, int value, Material material){
        route.setListPerms(globalPermissionSet, material, value);
    }

    public void setContainerPlayerPermission(UUID uuid, PermissionRoute route, int value, Material material) {
        route.setListPerms(getPlayerPermissionSet(uuid), material, value);
    }
}