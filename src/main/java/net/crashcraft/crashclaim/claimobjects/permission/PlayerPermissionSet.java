package net.crashcraft.crashclaim.claimobjects.permission;

import net.crashcraft.crashclaim.claimobjects.PermState;
import org.bukkit.Material;

import java.util.HashMap;

public class PlayerPermissionSet extends PermissionSet implements Cloneable {
    private int modifyPermissions;
    private int modifyClaim;

    public PlayerPermissionSet() {

    }

    public PlayerPermissionSet(int build, int interactions, int entities, int explosions, int teleportation, int viewSubClaims, HashMap<Material, Integer> containers, int defaultContainerValue, int modifyPermissions, int modifyClaim) {
        super(build, interactions, entities, teleportation, viewSubClaims, containers, defaultContainerValue);
        this.modifyPermissions = modifyPermissions;
        this.modifyClaim = modifyClaim;
    }

    public int getModifyPermissions() {
        return modifyPermissions;
    }

    public void setModifyPermissions(int modifyPermissions) {
        this.modifyPermissions = modifyPermissions;
    }

    public int getModifyClaim() {
        return modifyClaim;
    }

    public void setModifyClaim(int modifyClaim) {
        this.modifyClaim = modifyClaim;
    }

    public static PlayerPermissionSet createDefault() {
        return new PlayerPermissionSet(PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL, PermState.NEUTRAL,
                new HashMap<>(), PermState.NEUTRAL, PermState.DISABLE, PermState.DISABLE);
    }

    public boolean isEquivalent(PlayerPermissionSet other){
        return this.getModifyClaim() == other.getModifyClaim() &&
                this.getModifyPermissions() == other.getModifyPermissions() &&
                this.getBuild() == other.getBuild() &&
                this.getEntities() == other.getEntities() &&
                this.getInteractions() == other.getInteractions() &&
                this.getTeleportation() == other.getTeleportation() &&
                this.getDefaultConatinerValue() == other.getDefaultConatinerValue() &&
                this.getContainers().entrySet().stream()
                        .allMatch(entry -> !other.getContainers().containsKey(entry.getKey()) || other.getContainers().get(entry.getKey()).equals(entry.getValue())) &&
                this.getViewSubClaims() == other.getViewSubClaims();
    }

    public boolean isDefault(){
        PlayerPermissionSet defaultPerms = createDefault();
        return defaultPerms.isEquivalent(this);
    }

    public PlayerPermissionSet clone() {
        return (PlayerPermissionSet) super.clone();
    }
}
