package io.github.gabehowe.fartrade

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*
import net.md_5.bungee.api.*
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent


class TradeCommand(private val farTrade: FarTrade) : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args.size == 1) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (Bukkit.getPlayer(player.uniqueId)!!.name == sender.name) {
                    continue
                }
                val list = mutableListOf<String>()
                list.add(Bukkit.getPlayer(player.uniqueId)!!.name)
                return list
            }
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can use this command!")
            return true
        }
        if (args.isEmpty()) {
            return false
        }
        val sessionID = UUID.randomUUID()
        if (args.size == 1) {
            if (args[0].equals(sender.name, ignoreCase = true)) {
                sender.sendMessage("§cYou can't trade with yourself!")
                return true
            }
            if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
                sender.sendMessage("§c'${args[0]}§c' is not online or does not exist")
                return true
            }
            val otherPlayer = Bukkit.getPlayer(args[0])!!
            if (farTrade.voteMap[Pair(sender.uniqueId, Bukkit.getPlayer(args[0])!!.uniqueId)] == true) {
                sender.sendMessage("§cYou already have an outgoing trade request to ${Bukkit.getPlayer(args[0])!!.displayName}§c")
                return true
            }
            farTrade.voteSessionList.add(sessionID)
            farTrade.voteMap[Pair(sender.uniqueId, Bukkit.getPlayer(args[0])!!.uniqueId)] = true
            sender.sendMessage("§aYou sent a trade request to ${otherPlayer.displayName}§c")
            val msg = TextComponent("§a§l[ACCEPT]")
            msg.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept ${sender.name}")
            val msg2 = TextComponent("§c§l[DENY]")
            msg2.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny ${sender.name}")
            otherPlayer.sendMessage("§a${sender.displayName}§c sent you a trade request              ")
            msg.addExtra(TextComponent(" "))
            msg.addExtra(msg2)
            otherPlayer.spigot().sendMessage(msg)
            otherPlayer.playSound(otherPlayer.location, Sound.BLOCK_NOTE_BLOCK_FLUTE, 1.0f, 2.0f)
            Bukkit.getServer().scheduler.runTaskLater(farTrade, Runnable {
                if (!farTrade.voteSessionList.contains(sessionID)) {
                    return@Runnable
                }
                else {
                    farTrade.voteMap.remove(Pair(sender.uniqueId, Bukkit.getPlayer(args[0])!!.uniqueId))
                    farTrade.voteSessionList.remove(sessionID)
                    sender.sendMessage("§cYour trade request to ${otherPlayer.displayName}§c §cexpired")
                    sender.playSound(sender.location, Sound.ENTITY_PIG_DEATH, 1.0f, 1.0f)
                    otherPlayer.sendMessage("§cYour trade request from ${sender.displayName}§c §cexpired")
                    otherPlayer.playSound(otherPlayer.location, Sound.ENTITY_PIG_DEATH, 1.0f, 1.0f)
                }
            },(farTrade.voteTime.toDouble() * 20).toLong())
            return true
        }
        if (args.size > 2) {
            return false
        }
        if (args[0].toLowerCase() == "deny") {
            if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {
                sender.sendMessage("§c'${args[1]}' is not online or does not exist")
                return true
            }
            if (farTrade.voteMap[Pair(Bukkit.getPlayer(args[1])!!.uniqueId, sender.uniqueId)] != true) {
                sender.sendMessage("§cCouldn't find an active trade request from ${Bukkit.getPlayer(args[1])!!.displayName}§c")
                return true
            }
            sender.playSound(sender.location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 0.75f)
            farTrade.voteMap.remove(Pair(Bukkit.getPlayer(args[1])!!.uniqueId, sender.uniqueId))
            farTrade.voteSessionList.remove(sessionID)
            Bukkit.getScheduler().cancelTasks(farTrade)
            Bukkit.getPlayer(args[1])?.sendMessage("§c${sender.displayName}§c §cdenied your trade request")
            return true
        }
        if (args[0].toLowerCase() == "accept") {
            if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {
                sender.sendMessage("§c'${args[1]}§c' is not online or does not exist")
                return true
            }
            if (!farTrade.voteMap.any { it.key.second == sender.uniqueId }) {
                sender.sendMessage("§cCouldn't find an active trade request from ${Bukkit.getPlayer(args[1])!!.displayName}§c")
                return true
            }
            if (args[1].equals(sender.name, ignoreCase = true)) {
                sender.sendMessage("§cYou can't trade with yourself!")
                return true
            }
            Bukkit.getScheduler().cancelTasks(farTrade)
            farTrade.newTrade(Bukkit.getPlayer(args[1])!!.uniqueId, sender.uniqueId)
            farTrade.voteMap.remove(Pair(Bukkit.getPlayer(args[1])!!.uniqueId, sender.uniqueId))
            farTrade.voteSessionList.remove(sessionID)
            return true
        }
        return false
    }
}