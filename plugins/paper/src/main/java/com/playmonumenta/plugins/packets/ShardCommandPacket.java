package com.playmonumenta.plugins.packets;

import com.google.gson.JsonObject;
import com.playmonumenta.plugins.Plugin;

import org.bukkit.Bukkit;

/**
 * CommandPacket sends a command to a single shard.
 */
public class ShardCommandPacket extends BasePacket {
	public static final String PacketOperation = "Monumenta.Shard.Command";

	/**
	 * Create a new packet containing a command.
	 * This command will be sent to a single shard
	 * @param target
	 * @param command
	 */
	public ShardCommandPacket(String targetShard, String command) {
		super(targetShard, PacketOperation, new JsonObject());
		mData.addProperty("command", command);
	}

	public static void handlePacket(Plugin plugin, BasePacket packet) throws Exception {
		if (!packet.hasData() ||
		    !packet.getData().has("command") ||
		    !packet.getData().get("command").isJsonPrimitive() ||
		    !packet.getData().getAsJsonPrimitive("command").isString()) {
			throw new Exception("CommandPacket failed to parse required string field 'command'");
		}
		String command = packet.getData().get("command").getAsString();
		plugin.getLogger().info("Executing received command '" + command + "'");

		/* Call this on the main thread */
		Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command));
	}
}
