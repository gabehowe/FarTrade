package io.github.gabehowe.unitedpostalservice

import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

class UnitedPostalService : JavaPlugin() {
    private lateinit var economy: Economy
    override fun onEnable() {
        if (!setupEconomy()) {
            logger.severe(
                String.format(
                    "[%s] - Disabled due to no Vault dependency found!",
                    description.name
                )
            )
            server.pluginManager.disablePlugin(this)
            return
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(Economy::class.java)
            ?: return false
        economy = rsp.provider
        @Suppress("SENSELESS_COMPARISON")
        return economy != null
    }

}