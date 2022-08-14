package io.github.toberocat.improvedFactions.faction.local.managers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.toberocat.improvedFactions.event.EventExecutor;
import io.github.toberocat.improvedFactions.exceptions.faction.FactionHandlerNotFound;
import io.github.toberocat.improvedFactions.exceptions.faction.FactionNotInStorage;
import io.github.toberocat.improvedFactions.faction.Faction;
import io.github.toberocat.improvedFactions.faction.components.rank.GuestRank;
import io.github.toberocat.improvedFactions.faction.components.rank.Rank;
import io.github.toberocat.improvedFactions.faction.components.rank.members.FactionRank;
import io.github.toberocat.improvedFactions.faction.local.LocalFactionHandler;
import io.github.toberocat.improvedFactions.player.OfflineFactionPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FactionPerm {

    private Map<UUID, String> memberRanks;

    @JsonIgnore
    private Faction<?> faction;

    public FactionPerm() {
    }

    public FactionPerm(Faction<?> faction) {
        this.faction = faction;
        memberRanks = new HashMap<>();
    }

    public Rank getPlayerRank(OfflineFactionPlayer<?> player) throws FactionNotInStorage {
        if (faction.isMember(player)) return getRank(player);
        if (faction.isAllied(player)) return getRank(player).getEquivalent();
        return Rank.fromString(GuestRank.REGISTRY);
    }

    private Rank getRank(OfflineFactionPlayer<?> player) {
        LocalFactionHandler handler = LocalFactionHandler.getInstance();

        if (handler == null) throw new FactionHandlerNotFound("A local faction " +
                "required a local handler, but didn't find it. " +
                "This is a critical bug and needs to be reported to the dev using discord / github");
        return handler.getSavedRank(player);
    }

    public void setRank(OfflineFactionPlayer<?> player, FactionRank rank) {
        if (rank == null) {
            memberRanks.remove(player.getUniqueId());
            return;
        }
        String old = memberRanks.put(player.getUniqueId(), rank.getRegistry());

        FactionRank oldRank = (FactionRank) Rank.fromString(old == null ? GuestRank.REGISTRY : old);
        EventExecutor.getExecutor().factionMemberRankUpdate(faction, player, oldRank, rank);
    }

    public Map<UUID, String> getMemberRanks() {
        return memberRanks;
    }

    public void setMemberRanks(Map<UUID, String> memberRanks) {
        this.memberRanks = memberRanks;
    }

    @JsonIgnore
    public Faction<?> getFaction() {
        return faction;
    }

    @JsonIgnore
    public void setFaction(Faction<?> faction) {
        this.faction = faction;
    }
}
