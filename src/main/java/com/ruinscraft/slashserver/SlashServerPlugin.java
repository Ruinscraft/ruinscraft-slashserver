package com.ruinscraft.slashserver;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class SlashServerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Set<String> commands = getConfig().getConfigurationSection("commands").getKeys(false);

        for (String command : commands) {
            String server = getConfig().getString("commands." + command + ".server");
            String permission = getConfig().getString("commands." + command + ".permission");

            CommandExecutor commandExecutor = (sender, command1, label, args) -> {
                if (!(sender instanceof Player)) {
                    return false;
                }

                Player player = (Player) sender;

                if (permission != null && !permission.isEmpty()) {
                    if (!player.hasPermission(permission)) {
                        player.sendMessage(ChatColor.RED + "You do not have permission for this server.");
                        return false;
                    }
                }

                sendToServer(player, server);

                return true;
            };

            getCommand(command).setExecutor(commandExecutor);
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

}
