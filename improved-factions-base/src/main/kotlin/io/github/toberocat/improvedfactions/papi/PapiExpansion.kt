package io.github.toberocat.improvedfactions.papi

import io.github.toberocat.improvedfactions.config.ImprovedFactionsConfig
import io.github.toberocat.improvedfactions.user.factionUser
import io.github.toberocat.improvedfactions.utils.toOfflinePlayer
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * Created: 20.07.2023
 * @author Tobias Madlberger (Tobias)
 */

class PapiExpansion(private val pluginConfig: ImprovedFactionsConfig) : PlaceholderExpansion() {
    private val placeholders = HashMap<String, (player: OfflinePlayer) -> String?>()

    init {
        placeholders["owner"] = { it.factionUser().faction()?.owner?.toOfflinePlayer()?.name }
        placeholders["name"] = { it.factionUser().faction()?.name }
        placeholders["rank"] = { it.factionUser().rank().name }
        placeholders["power"] = { it.factionUser().faction()?.accumulatedPower?.toString() }
        placeholders["maxPower"] = { it.factionUser().faction()?.maxPower?.toString() }
    }

    override fun getAuthor(): String = "Tobero"

    override fun getIdentifier(): String = "faction"

    override fun getVersion(): String = "1.0.0"

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        return player?.let { transaction { placeholders[params]?.invoke(it) } }
            ?: pluginConfig.defaultPlaceholders[params]
    }
}
