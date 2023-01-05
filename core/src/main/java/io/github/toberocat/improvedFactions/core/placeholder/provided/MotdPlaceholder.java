package io.github.toberocat.improvedFactions.core.placeholder.provided;

import io.github.toberocat.improvedFactions.core.faction.Faction;
import io.github.toberocat.improvedFactions.core.player.OfflineFactionPlayer;
import org.jetbrains.annotations.NotNull;

public class MotdPlaceholder implements FactionPlaceholder {
    @Override
    public @NotNull String run(@NotNull Faction<?> faction) {
        return faction.getMotd();
    }

    @Override
    public @NotNull String label() {
        return "motd";
    }
}
