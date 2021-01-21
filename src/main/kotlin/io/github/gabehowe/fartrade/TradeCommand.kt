package io.github.gabehowe.fartrade

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TradeCommand(private val farTrade: FarTrade) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return true
        }
        farTrade.initialize(sender.displayName, farTrade.senderInv)
        sender.openInventory(farTrade.senderInv)
        return true
    }
}