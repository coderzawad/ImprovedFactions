package io.github.toberocat.improvedfactions.claims

import io.github.toberocat.toberocore.command.exceptions.CommandException
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import org.jetbrains.exposed.sql.and
import kotlin.math.ceil
import kotlin.math.floor

fun Block.getFactionClaim(): FactionClaim? = location.getFactionClaim()
fun Location.getFactionClaim(): FactionClaim? = world?.name?.let {
    getFactionClaim((x / 16.0).largerValueRound(), (z / 16.0).largerValueRound(), it)
}
fun Chunk.getFactionClaim() = getFactionClaim(x, z, world.name)

fun getFactionClaim(x: Int, z: Int, worldName: String) = FactionClaim.find {
    FactionClaims.chunkX eq x and
            (FactionClaims.chunkZ eq z) and
            (FactionClaims.world eq worldName)
}.firstOrNull()

fun squareClaimAction(
    centerChunk: Chunk,
    squareRadius: Int,
    action: (chunk: Chunk) -> Unit,
    handleError: (e: CommandException) -> Unit
): ClaimStatistics {
    var successfulClaims = 0
    var totalClaims = 0
    val world = centerChunk.world
    val centerX = centerChunk.x
    val centerZ = centerChunk.z
    for (x in -squareRadius..squareRadius) {
        for (z in -squareRadius..squareRadius) {
            val chunk = world.getChunkAt(centerX + x, centerZ + z)
            try {
                totalClaims++
                action(chunk)
                successfulClaims++
            } catch (e: CommandException) {
                handleError(e)
            }
        }
    }
    return ClaimStatistics(totalClaims, successfulClaims)
}

private fun Double.largerValueRound() = (if (this < 0) floor(this) else ceil(this)).toInt()