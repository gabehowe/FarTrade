package io.github.gabehowe.fartrade

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import io.github.gabehowe.fartrade.FarTrade

class TradeCommand(private val farTrade: FarTrade) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return true
        }
        sender.openInventory(farTrade.tradeInventory)
        return true
    }
}