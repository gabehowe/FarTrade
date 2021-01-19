package io.github.gabehowe.fartrade

import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import io.github.gabehowe.fartrade.TradeInventory

class FarTrade : JavaPlugin() {
    private lateinit var economy: Economy
    override fun onEnable() {
        getCommand("trade")?.setExecutor(TradeCommand())
        TradeInventory(this).init()
        logger.severe("hi how are you doing")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun setupEconomy(): Boolean {
        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(Economy::class.java)
            ?: return false
        economy = rsp.provider
        @Suppress("SENSELESS_COMPARISON")
        return economy != null
    }

}