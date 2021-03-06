package com.playmonumenta.plugins.enchantments;

import java.util.EnumSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.enchantments.EnchantmentManager.ItemSlot;

public class Recoil implements BaseEnchantment {
	private static final String PROPERTY_NAME = ChatColor.GRAY + "Recoil";

	@Override
	public String getProperty() {
		return PROPERTY_NAME;
	}

	@Override
	public EnumSet<ItemSlot> validSlots() {
		return EnumSet.of(ItemSlot.MAINHAND, ItemSlot.OFFHAND);
	}

	@Override
	public void onLaunchProjectile(Plugin plugin, Player player, int level, Projectile proj, ProjectileLaunchEvent event) {
		if (player.isSneaking()) {
			player.setCooldown(player.getInventory().getItemInMainHand().getType(), (int)(20 * Math.sqrt(level)));
		} else {
			Vector velocity = player.getLocation().getDirection().multiply(-0.5 * Math.sqrt(level));
			player.setVelocity(velocity.setY(Math.max(0.1, velocity.getY())));
		}
	}

}
