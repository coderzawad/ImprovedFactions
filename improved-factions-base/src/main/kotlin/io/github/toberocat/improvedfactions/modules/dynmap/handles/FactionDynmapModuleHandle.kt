package io.github.toberocat.improvedfactions.modules.dynmap.handles

import io.github.toberocat.improvedfactions.claims.clustering.cluster.Cluster
import io.github.toberocat.improvedfactions.claims.clustering.position.ChunkPosition
import io.github.toberocat.improvedfactions.factions.Faction
import org.bukkit.Location

interface FactionDynmapModuleHandle {
    fun factionHomeChange(faction: Faction, homeLocation: Location)
    fun clusterChange(cluster: Cluster)
    fun clusterRemove(cluster: Cluster)
    fun removePosition(position: ChunkPosition)
}