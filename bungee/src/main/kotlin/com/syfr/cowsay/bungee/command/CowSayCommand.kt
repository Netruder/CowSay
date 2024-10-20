package com.syfr.cowsay.bungee.command

import com.google.common.io.ByteStreams
import com.syfr.cowsay.bungee.CowSayPluginBungee
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

class CowSayCommand(private val plugin: CowSayPluginBungee) : Command("cowsay") {

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender !is ProxiedPlayer) {
            sender?.sendMessage(TextComponent("This command is only for players"))
            return
        }

        if (args?.size == null || args.isEmpty()) {
            sender.sendMessage(TextComponent("Usage: /cowsay <Name>"))
            return
        }

        this.plugin.fetchPlayerSpec(sender.uniqueId) { spec ->
            val name = args.joinToString(" ")
            spec.lastSay = name

            val count = spec.count.incrementAndGet()

            val out = ByteStreams.newDataOutput()
            out.writeUTF("spawn")
            out.writeUTF(sender.name)
            out.writeUTF(name)
            out.writeInt(count)
            sender.server.info.sendData("cowsay:channel", out.toByteArray())

            sender.sendMessage(TextComponent("The cow will be spawned now..."))
        }
    }
}
