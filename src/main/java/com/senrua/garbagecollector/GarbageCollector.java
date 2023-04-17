package com.senrua.garbagecollector;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GarbageCollector extends JavaPlugin implements CommandExecutor {

    private long lastGcTime = 0;
    private boolean automaticCleanupEnabled = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("Garbage Collector Plugin has been enabled.");
        startMemoryCheckTask();
        getCommand("garbagecollection").setExecutor(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Garbage Collector Plugin has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("garbagecollection")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("hyronic.gc")) {
                    player.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
            }

            if (args.length == 0) {
                manualGarbageCollection();
                sender.sendMessage("§aManual garbage collection triggered.");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("on")) {
                    automaticCleanupEnabled = true;
                    sender.sendMessage("§aAutomatic garbage collection enabled.");
                } else if (args[0].equalsIgnoreCase("off")) {
                    automaticCleanupEnabled = false;
                    sender.sendMessage("§aAutomatic garbage collection disabled.");
                } else {
                    sender.sendMessage("§cInvalid argument. Usage: /garbagecollection [on|off]");
                }
            } else {
                sender.sendMessage("§cInvalid arguments. Usage: /garbagecollection [on|off]");
            }

            return true;
        }
        return false;
    }

    private void startMemoryCheckTask() {
        int checkIntervalTicks = 20 * 300; // Check every 300 seconds (5 minutes)
        Bukkit.getScheduler().runTaskTimer(this, this::checkMemoryUsage, checkIntervalTicks, checkIntervalTicks);
    }

    private void checkMemoryUsage() {
        if (!automaticCleanupEnabled) {
            return;
        }

        double memoryThreshold = getConfig().getDouble("memoryThreshold");
        long cooldownMinutes = 30;

        Runtime runtime = Runtime.getRuntime();
        double usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (double) (1024 * 1024 * 1024); // GB value

        if (usedMemory >= memoryThreshold && System.currentTimeMillis() - lastGcTime >= cooldownMinutes * 60 * 1000) {
            manualGarbageCollection();
            getLogger().info("Garbage collection triggered due to memory usage!");
        }
    }

    private void manualGarbageCollection() {
        System.gc();
        lastGcTime = System.currentTimeMillis();
    }
}
