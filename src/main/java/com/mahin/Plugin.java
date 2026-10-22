package com.mahin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
public final class Plugin extends JavaPlugin {
    private String ParseColors(String RawText) {
        return ChatColor.translateAlternateColorCodes('&', RawText);
    }
    private String GetColoredMessage(int ping, String type, String PlayerName) {
        if (PlayerName == null) {
            PlayerName = "";
        }
        String level;
        if (ping < getConfig().getInt("good.limit")) {
            level = "good";
        }
        else if (ping < getConfig().getInt("medium.limit")) {
            level = "medium";
        }
        else {
            level = "bad";
        }
        return ParseColors(getConfig().getString(level + "." + type + "_ping").replace("%ping%", Integer.toString(ping)).replace("%player%", PlayerName));
    }
    @Override
    public void onEnable() {
        String UpdateStatus;
        String VersionString = "";
        int VersionInteger = 0;
        HttpURLConnection GithubRequest = null;
        Scanner scanner = null;
        try {
            GithubRequest = (HttpURLConnection) new URI("https://api.github.com/repos/MahinBro001238/PingCommand/releases/latest").toURL().openConnection();
            GithubRequest.setConnectTimeout(5000);
            GithubRequest.setReadTimeout(5000);
            scanner = new Scanner(GithubRequest.getInputStream());
            VersionString = scanner.useDelimiter("\\A").next().split("\"tag_name\":\"v")[1].split("\"")[0];
            scanner.close();
            GithubRequest.disconnect();
            VersionInteger = Integer.parseInt(VersionString.replace(".", ""));
        }
        catch (Exception e) {
            if (GithubRequest != null) {
                GithubRequest.disconnect();
            }
            if (scanner != null) {
                scanner.close();
            }
        }
        if (VersionInteger > 10) {
            UpdateStatus = "There is a new update: v" + VersionString;
        }
        else if (VersionInteger == 10) {
            UpdateStatus = "You are using the latest version";
        }
        else {
            UpdateStatus = "Unable to check for updates";
        }
        new Metrics(this, 34835);
        saveDefaultConfig();
        getLogger().info("=============================================");
        getLogger().info("Ping Command v1.0");
        getLogger().info("Made by MahinBro001238");
        getLogger().info("Licensed under the Apache License 2.0");
        getLogger().info("For support: https://discord.gg/8yrbGUCFFD");
        getLogger().info(UpdateStatus);
        getLogger().info("Ping Command has been enabled");
        getLogger().info("=============================================");
    }
    @Override
    public void onDisable() {
        getLogger().info("Ping Command v1.0 has been disabled");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("ping")) {
            if (sender instanceof Player) {
                Player CommandExecutor = (Player) sender;
                if (CommandExecutor.hasPermission("pingcommand.ping")) {
                    if (args.length == 0) {
                        CommandExecutor.sendMessage(GetColoredMessage(CommandExecutor.getPing(), "own", null));
                    } 
                    else if (args.length == 1) {
                        if (getConfig().getBoolean("view_others_ping")) {
                            if (CommandExecutor.isPermissionSet("pingcommand.other") && !CommandExecutor.hasPermission("pingcommand.other")) {
                                CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.no_permission_other")));
                            }
                            else {
                                Player player = Bukkit.getPlayer(args[0]);
                                if (player != null) {
                                    CommandExecutor.sendMessage(GetColoredMessage(player.getPing(), "other", player.getName()));
                                }
                                else {
                                    CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.unknown_player").replace("%player%", args[0])));
                                }
                            }
                        }
                        else {
                            if (CommandExecutor.hasPermission("pingcommand.other")) {
                                Player player = Bukkit.getPlayer(args[0]);
                                if (player != null) {
                                    CommandExecutor.sendMessage(GetColoredMessage(player.getPing(), "other", player.getName()));
                                }
                                else {
                                    CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.unknown_player").replace("%player%", args[0])));
                                }
                            }
                            else {
                                CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.no_permission_other")));
                            }
                        }
                    }
                    else {
                        CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.incorrect_usage")));
                    }
                }
                else {
                    CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.no_permission")));
                }
            }
            else {
                if (args.length != 1) {
                    sender.sendMessage("Incorrect use of command.");
                    sender.sendMessage("Please use /ping <player> to see another player's ping.");
                }
                else {
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player == null) {
                        sender.sendMessage("Player " + args[0] + " does not exist");
                    }
                    else {
                        sender.sendMessage(player.getName() + "'s Ping: " + player.getPing() + "ms");
                    }
                }
            }
        }
        else if (label.equalsIgnoreCase("pingreload")) {
            if (sender instanceof Player) {
                Player CommandExecutor = (Player) sender;
                if (CommandExecutor.hasPermission("pingcommand.reload")) {
                    reloadConfig();
                    CommandExecutor.sendMessage(ParseColors("&aSuccessfully reloaded config."));
                }
                else {
                    CommandExecutor.sendMessage(ParseColors(getConfig().getString("messages.no_permission")));
                }
            }
            else {
                reloadConfig();
                sender.sendMessage("Successfully reloaded config.");
            }
        }
        return true;
    }
}