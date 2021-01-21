package io.github.gabehowe.fartrade

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class TradeCommand(private val farTrade: FarTrade) : TabExecutor {
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String,args: Array<out String>): MutableList<String> {
        if(args.size == 1) {
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
        if (args.size != 1 ) {
            return false
        }
        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
            sender.sendMessage("§c'${args[0]}' is not online or does not exist")
        }
        if (args[0].equals(sender.displayName, ignoreCase = true)) {
            sender.sendMessage("§cYou can't trade with yourself!")
            return true
        }
        farTrade.newTrade(sender.uniqueId,Bukkit.getPlayer(args[0])!!.uniqueId)
        return true
    }
}