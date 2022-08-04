package io.github.toberocat.improvedFactions.exceptions.faction;

import io.github.toberocat.improvedFactions.faction.Faction;
import org.jetbrains.annotations.NotNull;

public class FactionOwnerIsOfflineException extends Exception {

    private final Faction<?> faction;

    public FactionOwnerIsOfflineException(@NotNull Faction<?> faction) {
        super("Owner of " + faction.getRegistry() + " is offline");
        this.faction = faction;
    }

    public Faction<?> getFaction() {
        return faction;
    }
}
