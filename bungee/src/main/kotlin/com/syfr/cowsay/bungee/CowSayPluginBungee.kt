package com.syfr.cowsay.bungee

import com.syfr.cowsay.bungee.command.CowSayCommand
import com.syfr.cowsay.bungee.database.CowSayDatabase
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.md_5.bungee.event.EventHandler
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class CowSayPluginBungee : Plugin(), Listener {

    private val playersData = ConcurrentHashMap<UUID, PlayerSpec>(64)
    private var database: CowSayDatabase? = null

    override fun onEnable() {
        this.saveConfig()

        this.database = CowSayDatabase(ConfigurationProvider
                .getProvider(YamlConfiguration::class.java).load(File(this.dataFolder, "config.yml")))

        this.proxy.registerChannel("cowsay:channel")
        this.proxy.pluginManager.registerCommand(this, CowSayCommand(this))
        this.proxy.pluginManager.registerListener(this, this)
    }

    override fun onDisable() {
        this.proxy.unregisterChannel("cowsay:channel")

        for ((uuid, spec) in this.playersData) {
            this.savePlayerSpec(uuid, spec)
        }
        this.playersData.clear()
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val uuid = event.player.uniqueId
        val spec = this.playersData.remove(uuid) ?: return
        this.savePlayerSpec(uuid, spec)
    }

    fun fetchPlayerSpec(uuid: UUID, callback: ((spec: PlayerSpec) -> Unit)) {
        val data = playersData[uuid]
        if (data !== null) {
            callback.invoke(data)
            return
        }

        val database = this.database ?: return
        this.proxy.scheduler.runAsync(this) {
            database.fetchLastSayAndCount(uuid).let { data ->
                val spec = PlayerSpec(data.first, data.second)
                this.playersData[uuid] = spec
                callback.invoke(spec)
            }
        }
    }

    private fun savePlayerSpec(uuid: UUID, spec: PlayerSpec) {
        val database = this.database ?: return
        this.proxy.scheduler.runAsync(this) {
            database.updateLastSayAndCount(uuid, spec.lastSay, spec.count.get())
        }
    }

    private fun saveConfig() {
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs()
        }

        val configFile = File(this.dataFolder, "config.yml")
        if (!configFile.exists()) {
            Files.copy(this.getResourceAsStream("config.yml"), configFile.toPath())
        }
    }

    data class PlayerSpec(var lastSay: String?, var count: AtomicInteger)
}
