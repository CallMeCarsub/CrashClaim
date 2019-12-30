package net.crashcraft.whipclaim.visualize.api.providers;

import net.crashcraft.whipclaim.claimobjects.BaseClaim;
import net.crashcraft.whipclaim.visualize.api.*;
import net.crashcraft.whipclaim.visualize.api.claim.GlowClaimVisual;
import net.crashcraft.whipclaim.visualize.api.marker.GlowMarketVisual;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GlowVisualProvider implements VisualProvider {
    @Override
    public BaseVisual spawnClaimVisual(VisualColor color, VisualGroup parent, BaseClaim claim, int y) {
        return new GlowClaimVisual(color, parent, parent.getPlayer(), y, claim);
    }

    @Override
    public BaseVisual spawnMarkerVisual(VisualColor color, VisualGroup parent, Location location) {
        return new GlowMarketVisual(color, parent, parent.getPlayer(), location);
    }
}