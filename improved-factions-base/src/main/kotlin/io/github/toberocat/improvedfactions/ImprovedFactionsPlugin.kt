package io.github.toberocat.improvedfactions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jeff_media.updatechecker.UpdateCheckSource
import com.jeff_media.updatechecker.UpdateChecker
import com.jeff_media.updatechecker.UserAgentBuilder
import io.github.toberocat.guiengine.GuiEngineApi
import io.github.toberocat.guiengine.function.FunctionProcessor
import io.github.toberocat.guiengine.utils.FileUtils
import io.github.toberocat.guiengine.utils.logger.PluginLogger
import io.github.toberocat.improvedfactions.claims.FactionClaims
import io.github.toberocat.improvedfactions.commands.FactionCommandExecutor
import io.github.toberocat.improvedfactions.components.icon.FactionIconComponent
import io.github.toberocat.improvedfactions.components.icon.FactionIconComponentBuilder
import io.github.toberocat.improvedfactions.components.permission.FactionPermissionComponent
import io.github.toberocat.improvedfactions.components.permission.FactionPermissionComponentBuilder
import io.github.toberocat.improvedfactions.components.permission.TYPE
import io.github.toberocat.improvedfactions.components.rank.FactionRankComponent
import io.github.toberocat.improvedfactions.components.rank.FactionRankComponentBuilder
import io.github.toberocat.improvedfactions.components.rankselector.FactionRankSelectorComponent
import io.github.toberocat.improvedfactions.components.rankselector.FactionRankSelectorComponentBuilder
import io.github.toberocat.improvedfactions.factions.Factions
import io.github.toberocat.improvedfactions.functions.FactionPermissionFunction
import io.github.toberocat.improvedfactions.invites.FactionInvites
import io.github.toberocat.improvedfactions.listeners.TerritoryEnterListener
import io.github.toberocat.improvedfactions.modules.base.BaseModule
import io.github.toberocat.improvedfactions.modules.power.PowerRaidsModule
import io.github.toberocat.improvedfactions.papi.PapiExpansion
import io.github.toberocat.improvedfactions.ranks.FactionRankHandler
import io.github.toberocat.improvedfactions.ranks.FactionRanks
import io.github.toberocat.improvedfactions.translation.updateLanguages
import io.github.toberocat.improvedfactions.zone.ZoneHandler
import io.github.toberocat.improvedfactions.utils.BStatsCollector
import io.github.toberocat.improvedfactions.utils.threadPool
import io.github.toberocat.toberocore.command.CommandExecutor
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database


/**
 * Created: 04.08.2023
 * @author Tobias Madlberger (Tobias)
 */
val objectMapper = ObjectMapper().registerKotlinModule()
const val SPIGOT_RESOURCE_ID = 95617

class ImprovedFactionsPlugin : JavaPlugin() {

    private lateinit var database: Database
    lateinit var guiEngineApi: GuiEngineApi
    lateinit var adventure: BukkitAudiences

    companion object {
        lateinit var instance: ImprovedFactionsPlugin
            private set
        val modules = mutableMapOf<String, BaseModule>(
            PowerRaidsModule.powerRaidsPair()
        )
    }

    fun addModuleCommands(executor: CommandExecutor) {
        modules.filter { it.value.shouldEnable(this) }
            .forEach { (_, module) -> module.addCommands(this, executor) }
    }

    override fun onEnable() {
        instance = this
        adventure = BukkitAudiences.create(this)

        BStatsCollector(this)
        checkForUpdate()

        copyFolders()
        loadConfig()

        database = DatabaseConnector(this).createDatabase()

        guiEngineApi = GuiEngineApi(this)
        registerComponents()
        registerFunctions()
        guiEngineApi.reload(PluginLogger(logger))

        registerModules()
        registerListeners()
        registerCommands()
        registerPapi()

        updateLanguages(this)
    }

    override fun onDisable() {
        adventure.close()
        threadPool.shutdown()
    }

    private fun checkForUpdate() {
        if (!config.getBoolean("update-checker")) return

        UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID.toString())
            .setDownloadLink(SPIGOT_RESOURCE_ID)
            .setDonationLink("https://www.paypal.com/donate/?hosted_button_id=BGB6QWR886Q6Y")
            .setChangelogLink(SPIGOT_RESOURCE_ID)
            .setNotifyOpsOnJoin(true)
            .setColoredConsoleOutput(true)
            .setSupportLink("https://discord.com/invite/yJYyNRfk39")
            .setNotifyByPermissionOnJoin("factions.updatechecker")
            .setUserAgent(
                UserAgentBuilder()
                    .addServerVersion()
                    .addBukkitVersion()
                    .addPluginNameAndVersion()
            )
            .checkEveryXHours(24.0)
            .checkNow()
    }

    private fun copyFolders() {
        FileUtils.copyAll(this, "languages")
    }

    private fun registerModules() = modules.filter { it.value.shouldEnable(this) }
        .forEach { (_, module) ->
            module.onEnable()
            module.reloadConfig(this)
        }

    private fun registerPapi() {
        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PapiExpansion().register()
            logger.info("Loaded improved factions papi extension")
            return
        }

        logger.info("Papi not found. Skipping Papi registration")
    }


    fun loadConfig() {
        saveDefaultConfig()
        Factions.maxNameLength = config.getInt("factions.unsafe.max-name-length", 36)
        Factions.maxIconLength = config.getInt("factions.unsafe.max-icon-length", 5000)
        Factions.maxSpacesInName = config.getInt("factions.max-spaces-in-name", 5)
        Factions.nameRegex = Regex(config.getString("factions.name-regex") ?: "[a-zA-Z ]*")
        FactionInvites.inviteExpiresInMinutes = config.getInt("factions.invites-expire-in", 5)
        FactionRanks.maxRankNameLength = config.getInt("factions.max-rank-name-length", 50)
        FactionClaims.blockedWorlds = config.getStringList("blacklisted-worlds").toSet()
        FactionRankHandler.guestRankName =
            config.getString("factions.unsafe.guest-rank-name") ?: FactionRankHandler.guestRankName
        FactionRanks.rankNameRegex = Regex(config.getString("factions.rank-name-regex") ?: "[a-zA-Z ]*")

        config.getConfigurationSection("zones")?.getKeys(false)?.let {
            it.forEach { zone ->
                val section = config.getConfigurationSection("zones.$zone")
                if (section == null) {
                    logger.warning("Invalid formatted zone $zone found in config")
                    return@forEach
                }
                ZoneHandler.createZone(this, zone, section)
            }
        }

        ZoneHandler.defaultZoneCheck(this)

        modules.values.forEach { it.reloadConfig(this) }
    }


    private fun registerCommands() {
        FactionCommandExecutor(this)
    }

    private fun registerComponents() {
        guiEngineApi.registerFactory(
            "faction-icon", FactionIconComponent::class.java, FactionIconComponentBuilder::class.java
        )

        guiEngineApi.registerFactory(
            "faction-rank", FactionRankComponent::class.java, FactionRankComponentBuilder::class.java
        )

        guiEngineApi.registerFactory(
            TYPE, FactionPermissionComponent::class.java, FactionPermissionComponentBuilder::class.java
        )

        guiEngineApi.registerFactory(
            FactionRankSelectorComponent.TYPE,
            FactionRankSelectorComponent::class.java,
            FactionRankSelectorComponentBuilder::class.java
        )
    }

    private fun registerFunctions() {
        FunctionProcessor.registerComputeFunction(FactionPermissionFunction())
    }

    private fun registerListeners() {
        server.pluginManager.registerEvents(TerritoryEnterListener(), this)
    }
}