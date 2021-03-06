package com.playmonumenta.plugins.commands;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.network.SocketManager;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.GreedyStringArgument;
import io.github.jorelali.commandapi.api.arguments.StringArgument;

public class BroadcastCommand {
	private static final String[] ALLOWED_COMMANDS = {
	            "whitelist",
	            "ban",
	            "ban-ip",
	            "pardon",
	            "pardon-ip",
	            "op",
	            "deop",

	            "say",
	            "msg",
	            "tell",
	            "tellraw",
	            "title",
	            "mute",
	            "unmute",

	            "re",
	            "function",
	            "difficulty",
	            "scoreboard",
	            "tag",
	            "team",
	            "setblock",
	            "restart-empty"
	        };

	private static final List<String> ALLOWED_COMMANDS_LIST = Arrays.asList(ALLOWED_COMMANDS);

	public static void register(Plugin plugin) {
		CommandPermission perm = CommandPermission.fromString("monumenta.command.broadcastcommand");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("command", new StringArgument().overrideSuggestions(ALLOWED_COMMANDS));
		CommandAPI.getInstance().register("broadcastcommand",
		                                  perm,
		                                  arguments,
		                                  (sender, args) -> {
											  run(plugin, sender, (String)args[0], null);
		                                  }
		);

		arguments.put("args", new GreedyStringArgument());
		CommandAPI.getInstance().register("broadcastcommand",
		                                  perm,
		                                  arguments,
		                                  (sender, args) -> {
											  run(plugin, sender, (String)args[0], (String)args[1]);
		                                  }
		);
	}

	private static void run(Plugin plugin, CommandSender sender, String command, String args) {
		/* Make sure the command is on the whitelist */
		if (!ALLOWED_COMMANDS_LIST.contains(command)) {
			sender.sendMessage(ChatColor.RED + "This command is not supported!");
			return;
		}

		/* Get the player's name, if any */
		String name = "";
		if (sender instanceof Player) {
			name = ((Player)sender).getName();
		} else if (sender instanceof ProxiedCommandSender) {
			CommandSender callee = ((ProxiedCommandSender) sender).getCallee();
			if (callee instanceof Player) {
				name = ((Player)callee).getName();
			}
		}

		String commandStr = command;
		if (args != null) {
			commandStr += " " + args;
		}

		/* Replace all instances of @S with the player's name */
		commandStr = commandStr.replaceAll("@S", name);

		if (!(sender instanceof Player) || ((Player)sender).isOp()) {
			sender.sendMessage(ChatColor.GOLD + "Broadcasting command '" + commandStr + "' to all servers!");
		}

		try {
			SocketManager.broadcastCommand(plugin, commandStr);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "Broadcasting command failed");
		}
	}
}
