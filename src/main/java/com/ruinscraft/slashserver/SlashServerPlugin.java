package com.ruinscraft.slashserver;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Set;

public class SlashServerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Set<String> commands = getConfig().getConfigurationSection("commands").getKeys(false);

        for (String label : commands) {
            String server = getConfig().getString("commands." + label + ".server");
            String permission = getConfig().getString("commands." + label + ".permission");

            Command command = new Command(label) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
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

                    player.sendMessage(ChatColor.GOLD + "Sending you to " + server + "...");

                    sendToServer(player, server);

                    return true;
                }
            };

            registerCommand(command);
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void registerCommand(Command command) {
        try {
            Field bukkitCommandMap = getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(getServer());
            commandMap.register("ruinscraft-slashserver", command);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

}
