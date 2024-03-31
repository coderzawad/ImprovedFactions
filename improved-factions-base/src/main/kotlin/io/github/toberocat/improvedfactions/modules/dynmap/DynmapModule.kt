package io.github.toberocat.improvedfactions.modules.dynmap

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.improvedfactions.claims.FactionClaim
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.modules.dynmap.config.DynmapModuleConfig
import io.github.toberocat.toberocore.command.CommandExecutor
import org.bukkit.Bukkit
import org.dynmap.DynmapCommonAPI
import org.dynmap.DynmapCommonAPIListener
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.MarkerSet

class DynmapModule : BaseModule {

    override val moduleName = MODULE_NAME

    private var api: DynmapCommonAPI? = null
    private var set: MarkerSet? = null

    private val resAreas: Map<String, AreaMarker> = HashMap()

    val config = DynmapModuleConfig()

    override fun onEnable() {
        DynmapCommonAPIListener.register(
            object : DynmapCommonAPIListener() {
                override fun apiEnabled(api: DynmapCommonAPI) {
                    this@DynmapModule.api = api
                    createFactionMarker(api)
                }
            }
        )
    }

    private fun createFactionMarker(api: DynmapCommonAPI) {
        val markerApi = api.markerAPI ?: return

        set = (markerApi.getMarkerSet(config.markerSetId) ?: markerApi.createMarkerSet(
            config.markerSetId,
            config.markerSetDisplayName,
            null,
            false
        )).also {
            it.markerSetLabel = config.markerSetDisplayName
            it.layerPriority = config.markerSetPriority
            it.hideByDefault = config.markerSetHiddenByDefault
        }
    }

    private fun handleClaim(faction: FactionClaim, newmap: Map<String, AreaMarker>) {}

    override fun reloadConfig(plugin: ImprovedFactionsPlugin) {
        config.reload(plugin.config)
    }

    override fun addCommands(plugin: ImprovedFactionsPlugin, executor: CommandExecutor) {}

    override fun shouldEnable(plugin: ImprovedFactionsPlugin): Boolean {
        val shouldEnable = super.shouldEnable(plugin)
        if (!shouldEnable) {
            return false
        }

        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            return true
        }

        warn("Dynmap module is enabled but Dynmap is not installed. Disabling Dynmap module.")
        return false
    }

    companion object {
        const val MODULE_NAME = "dynmap"
        fun dynmapModule() =
            (ImprovedFactionsPlugin.modules[MODULE_NAME] as? DynmapModule) ?: throw IllegalStateException()

        fun dynmapPair() = MODULE_NAME to DynmapModule()
    }
}