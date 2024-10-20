package com.syfr.cowsay.spigot

import com.google.common.io.ByteStreams
import com.syfr.cowsay.spigot.entity.CowSayEntity
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.File

class CowSayPluginSpigot : JavaPlugin(), PluginMessageListener {

    override fun onEnable() {
        this.server.messenger.registerIncomingPluginChannel(this, "cowsay:channel", this)

        if (!File(this.dataFolder, "config.yml").exists()) {
            this.saveDefaultConfig()
            this.reloadConfig()
        }
    }

    override fun onDisable() {
        this.server.messenger.unregisterIncomingPluginChannel(this, "cowsay:channel", this)

        for (world in this.server.worlds) {
            for (cow in world.getEntitiesByClass(Cow::class.java)) {
                (((cow as CraftEntity).handle) as? CowSayEntity)?.destroy()
            }
        }
    }

    override fun onPluginMessageReceived(channel: String?, player: Player?, bytes: ByteArray?) {
        if (!channel.equals("cowsay:channel", ignoreCase = true)) {
            return;
        }

        val input = ByteStreams.newDataInput(bytes)
        val subChannel = input.readUTF()

        if (subChannel.equals("spawn")) {
            val bukkitPlayer = Bukkit.getPlayer(input.readUTF())
            val lastSay = input.readUTF()
            val sayCount = input.readInt()

            bukkitPlayer.let { CowSayEntity.spawn(it, this.config, "$sayCount $lastSay") }
        }
    }
}
