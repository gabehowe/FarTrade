package io.github.gabehowe.fartrade

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player


class TradeCommand(private val farTrade: FarTrade) : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args.size == 1) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (Bukkit.getPlayer(player.uniqueId)!!.displayName == sender.name) {
                    continue
                }
                var list = mutableListOf<String>()
                list.add(Bukkit.getPlayer(player.uniqueId)!!.displayName)
                return list
            }
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return true
        }
        if (args.size == 1) {
            if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
                sender.sendMessage("§c'${args[0]}' is not online or does not exist")
            }
            if (farTrade.voteMap[Pair(sender.uniqueId, Bukkit.getPlayer(args[0])!!.uniqueId)] == false) {
                sender.sendMessage("§cYou already have an outgoing trade request to ${args[0]}")
                return true
            }
            farTrade.voteMap[Pair(sender.uniqueId, Bukkit.getPlayer(args[0])!!.uniqueId)] = false
            val msg = TextComponent("§a§l[ACCEPT]")
            msg.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept ${sender.displayName}")
            val msg2 = TextComponent("§a§l[ACCEPT]")
            msg2.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny ${sender.displayName}")
            Bukkit.getPlayer(args[0])!!.sendMessage("§a${sender.displayName} sent you a trade request              ")
            val msg3 = TextComponent(msg.text + " " + msg2.text)
            Bukkit.getPlayer(args[0])!!.spigot().sendMessage(msg3)
            return true
        }
        if (args.size > 2) {
            return false
        }
        if (args[0].toLowerCase() == "deny") {
            farTrade.voteMap.remove(Pair(Bukkit.getPlayer(args[1])!!.uniqueId, sender.uniqueId))
            Bukkit.getPlayer(args[1])?.sendMessage("§c${sender.displayName} denied your trade request")
            return true
        }
        if (args[0].toLowerCase() == "accept") {
            if (!farTrade.voteMap.any { it.key.second == sender.uniqueId }) {
                sender.sendMessage("§c${args[1]} has not extended a trade request to you")
                return true
            }
            val filter =
                farTrade.voteMap.filter { it.key.first == sender.uniqueId || it.key.second == sender.uniqueId }
                    .toList()
            if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {
                sender.sendMessage("§c'${args[1]}' is not online or does not exist")
            }
            if (args[1].equals(sender.displayName, ignoreCase = true)) {
                sender.sendMessage("§cYou can't trade with yourself!")
                return true
            }
            farTrade.newTrade(sender.uniqueId, Bukkit.getPlayer(args[1])!!.uniqueId)
            return true
        }
        return false
    }
}