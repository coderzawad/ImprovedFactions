package io.github.toberocat.improvedfactions.claims.clustering

import io.github.toberocat.improvedfactions.modules.power.PowerRaidsModule.Companion.powerRaidModule
import io.github.toberocat.improvedfactions.modules.power.handles.FactionPowerRaidModuleHandle
import io.github.toberocat.improvedfactions.utils.LazyUpdate
import java.util.*

class FactionCluster(val factionId: Int, id: UUID, positions: MutableSet<Position>) : Cluster(id, positions) {
    private val powerModuleHandle: FactionPowerRaidModuleHandle = powerRaidModule().factionModuleHandle
    private val unprotectedPositions = LazyUpdate(mutableSetOf()) {
        mutableSetOf<Position>().apply {
            powerModuleHandle.calculateUnprotectedChunks(this@FactionCluster, this)
        }
    }

    override fun scheduleUpdate() {
        unprotectedPositions.scheduleUpdate()
    }

    fun isUnprotected(x: Int, y: Int, world: String): Boolean {
        return Position(x, y, world) in unprotectedPositions.get()
    }
}