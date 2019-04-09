package com.playmonumenta.bossfights.spells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.bossfights.utils.Utils;

public class SpellFrostNova extends SpellBaseAoE {

	public SpellFrostNova(Plugin plugin, Entity launcher, int radius, int power) {
		super(plugin, launcher, radius, 80, 0, false, Sound.ENTITY_SNOWBALL_THROW,
			(Location loc) -> {
				World world = loc.getWorld();
				world.spawnParticle(Particle.CLOUD, loc, 15, ((double) radius) / 2, ((double) radius) / 2, ((double) radius) / 2, 0.05);
			},
			(Location loc) -> {
				World world = loc.getWorld();
				world.spawnParticle(Particle.SNOWBALL, loc, 2, 0.25, 0.25, 0.25, 0);
			},
			(Location loc) -> {
				World world = loc.getWorld();
				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.77F);
				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.5F);
				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 0.65F);
			},
			(Location loc) -> {
				World world = loc.getWorld();
				world.spawnParticle(Particle.CLOUD, loc, 3, 0.1, 0.1, 0.1, 0.2);
				world.spawnParticle(Particle.SNOWBALL, loc, 1, 0.25, 0.25, 0.25, 0);
			},
			(Location loc) -> {
				for (Player player : Utils.playersInRange(launcher.getLocation(), radius)) {
					double distance = player.getLocation().distance(launcher.getLocation());
					int pot_pow = (int)(power * ((radius - distance) / radius));
					player.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 1, pot_pow));
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 2));
				}
			}
		);
	}
}