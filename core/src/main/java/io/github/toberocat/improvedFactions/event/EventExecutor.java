package io.github.toberocat.improvedFactions.event;

import io.github.toberocat.improvedFactions.faction.Faction;
import io.github.toberocat.improvedFactions.faction.components.rank.members.FactionRank;
import io.github.toberocat.improvedFactions.player.FactionPlayer;
import io.github.toberocat.improvedFactions.player.OfflineFactionPlayer;
import io.github.toberocat.improvedFactions.world.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class EventExecutor implements EventListener {

    private static final EventExecutor EVENT_EXECUTOR = new EventExecutor();

    public static EventExecutor getExecutor() {
        return EVENT_EXECUTOR;
    }

    @Override
    public void protectChunk(@NotNull Chunk chunk, @NotNull String registry) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.protectChunk(chunk, registry));
    }

    @Override
    public void removeProtection(@NotNull Chunk chunk, @Nullable String oldRegistry) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.removeProtection(chunk, oldRegistry));
    }

    @Override
    public void factionMemberRankUpdate(@NotNull Faction<?> faction,
                                        @NotNull OfflineFactionPlayer<?> player,
                                        @NotNull FactionRank oldRank,
                                        @NotNull FactionRank newRank) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.factionMemberRankUpdate(faction, player, oldRank, newRank));
    }

    @Override
    public void transferOwnership(@NotNull Faction<?> faction, @NotNull OfflineFactionPlayer<?> oldOwner, @NotNull FactionPlayer<?> newOwner) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.transferOwnership(faction, oldOwner, newOwner));
    }

    @Override
    public void joinMember(@NotNull Faction<?> faction, @NotNull OfflineFactionPlayer<?> player, @NotNull FactionRank joinedAs) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.joinMember(faction, player, joinedAs));
    }

    @Override
    public void leaveMember(@NotNull Faction<?> faction, @NotNull FactionPlayer<?> player) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.leaveMember(faction, player));
    }

    @Override
    public void kickMember(@NotNull Faction<?> faction, @NotNull OfflineFactionPlayer<?> player) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.kickMember(faction, player));
    }

    @Override
    public void banMember(@NotNull Faction<?> faction, @NotNull OfflineFactionPlayer<?> player) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.banMember(faction, player));
    }

    @Override
    public void pardonPlayer(@NotNull Faction<?> faction, @NotNull OfflineFactionPlayer<?> player) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.pardonPlayer(faction, player));
    }

    @Override
    public void allyFaction(@NotNull Faction<?> first, @NotNull Faction<?> second) {
        new LinkedList<>(EventListener.HANDLER_LIST) // Make save copy
                .forEach(x -> x.allyFaction(first, second));
    }
}
