package com.playmonumenta.plugins.abilities.alchemist.harbinger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.point.Raycast;
import com.playmonumenta.plugins.point.RaycastData;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

/*
 * Shift+LClick with a bow to afflict the mob you're looking at (within 32 blocks)
 * with a plague that slows the target with slowness 3 and deals 3 DPS for 8 / 10 seconds.
 * If a mob with the plague is killed you gain an extra alch potion,
 * and the plague transfers to 1/2 nearby mobs within 5 blocks. Cooldown: 40 / 30 seconds.
 */

public class PurpleHaze extends Ability {

	public static class HazedMob {
		public Player triggeredBy;
		public LivingEntity mob;
		public int duration;
		public int ticksLeft;
		public int transfers;
		public HazedMob(LivingEntity mob, Player triggeredBy, int duration, int transfers) {
			this.mob = mob;
			this.triggeredBy = triggeredBy;
			this.ticksLeft = duration;
			this.duration = duration;
			this.transfers = transfers;
		}
	}

	private LivingEntity target = null;

	private static final int PURPLE_HAZE_1_DURATION = 8 * 20;
	private static final int PURPLE_HAZE_2_DURATION = 10 * 20;
	private static final int PURPLE_HAZE_1_COOLDOWN = 40 * 20;
	private static final int PURPLE_HAZE_2_COOLDOWN = 30 * 20;
	private static final int PURPLE_HAZE_1_TRANSFERS = 1;
	private static final int PURPLE_HAZE_2_TRANSFERS = 2;
	private static final double PURPLE_HAZE_DAMAGE = 3;
	private static final int PURPLE_HAZE_RADIUS = 5;
	private static final int PURPLE_HAZE_RANGE = 32;

	private static Map<UUID, HazedMob> mHazedMobs = new HashMap<UUID, HazedMob>();
	private static Map<UUID, HazedMob> newHazedMobs = new HashMap<UUID, HazedMob>();

	private static BukkitRunnable mRunnable = null;

	public PurpleHaze(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.linkedSpell = Spells.PURPLE_HAZE;
		mInfo.scoreboardId = "PurpleHaze";
		mInfo.cooldown = getAbilityScore() == 1 ? PURPLE_HAZE_1_COOLDOWN : PURPLE_HAZE_2_COOLDOWN;
		mInfo.trigger = AbilityTrigger.LEFT_CLICK;
		/*
		 * Only one runnable ever exists for purple haze - it is a global list, not tied to any individual players
		 * At least one player must be a warlock for this to start running. Once started, it runs forever.
		 */
		if (mRunnable == null || mRunnable.isCancelled()) {
			mRunnable = new BukkitRunnable() {
				int counter = 0;
				@Override
				public void run() {
					counter++;
					if (counter % 20 == 0) {
						for (Map.Entry<UUID, HazedMob> entry : mHazedMobs.entrySet()) {
							HazedMob e = entry.getValue();
							LivingEntity damagee = e.mob;
							// Since purple haze damage has to stack with any other damage, EntityUtils.damageEntity()
							// might not see it as intentional damage stacking, so iFrames need to be set manually.
							damagee.setNoDamageTicks(0);
							Vector v = damagee.getVelocity();
							EntityUtils.damageEntity(plugin, damagee, PURPLE_HAZE_DAMAGE, e.triggeredBy, null, false /* do not register CustomDamageEvent */);
							damagee.setVelocity(v);
							damagee.setNoDamageTicks(0);
							PotionUtils.applyPotion(e.triggeredBy, damagee, new PotionEffect(PotionEffectType.SLOW, 40, 2, false, true));
							Location loc = damagee.getLocation();
							mWorld.spawnParticle(Particle.SPELL_WITCH, loc.clone().add(0, 1, 0), 15, 0, 0.2, 0, 0.0001);
							counter = 0;
						}
					}

					Iterator<Map.Entry<UUID, HazedMob>> iter = mHazedMobs.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry<UUID, HazedMob> e = iter.next();
						HazedMob hazer = e.getValue();
						hazer.ticksLeft--;
						if (hazer.ticksLeft <= 0 || hazer.mob.isDead()) {
							if (hazer.mob.isDead()) {
								Location loc = hazer.mob.getLocation();
								// Perhaps a ball of purple haze going from the dead mob to the next instead?
								mWorld.spawnParticle(Particle.SPELL_WITCH, loc.clone().add(0, 1, 0), 120, 4, 1, 4, 0.0001);

								for (int i = 0; i < hazer.transfers; i++) {
									double closest = PURPLE_HAZE_RADIUS + 1;
									LivingEntity closestMob = null;
									for (LivingEntity mob : EntityUtils.getNearbyMobs(loc, PURPLE_HAZE_RADIUS)) {
										UUID mobUUID = mob.getUniqueId();
										double distance = mob.getLocation().distance(loc);
										if (!mob.isDead() && distance < closest && !newHazedMobs.containsKey(mobUUID) && !mHazedMobs.containsKey(mobUUID)) {
											closest = distance;
											closestMob = mob;
										}
									}

									if (closestMob != null) {
										HazedMob hazed = new HazedMob(closestMob, hazer.triggeredBy, hazer.duration, hazer.transfers);
										newHazedMobs.put(hazed.mob.getUniqueId(), hazed);
										Location loc2 = hazed.mob.getLocation();
										mWorld.spawnParticle(Particle.SPELL_WITCH, loc2.clone().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.001);
									}
								}

								AbilityUtils.addAlchemistPotions(hazer.triggeredBy, 1);
							}
							iter.remove();
						}
					}
					// Adding to the set iterated on creates problems, so all additions to it are executed afterwards.
					for (UUID newHazedMob : newHazedMobs.keySet()) {
						mHazedMobs.put(newHazedMob, newHazedMobs.get(newHazedMob));
					}
					newHazedMobs.clear();
				}
			};
			mRunnable.runTaskTimer(plugin, 0, 1);
		}
	}

	@Override
	public boolean runCheck() {
		if (mPlayer.isSneaking()) {
			ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
			if (mainHand.getType() == Material.BOW) {

				// Basically makes sure if the target is in LoS and if there is a path.
				Location eyeLoc = mPlayer.getEyeLocation();
				Raycast ray = new Raycast(eyeLoc, eyeLoc.getDirection(), PURPLE_HAZE_RANGE);
				ray.throughBlocks = false;
				ray.throughNonOccluding = false;
				if (AbilityManager.getManager().isPvPEnabled(mPlayer)) {
					ray.targetPlayers = true;
				}

				RaycastData data = ray.shootRaycast();

				List<LivingEntity> rayEntities = data.getEntities();
				if (rayEntities != null && !rayEntities.isEmpty()) {
					for (LivingEntity t : rayEntities) {
						if (!t.getUniqueId().equals(mPlayer.getUniqueId()) && t.isValid() && !t.isDead() && EntityUtils.isHostileMob(t)) {
							target = t;
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public void cast() {
		LivingEntity entity = target;
		int purpleHaze = getAbilityScore();
		if (entity != null && !mHazedMobs.containsKey(entity.getUniqueId())) {
			HazedMob hazed = new HazedMob(entity, mPlayer, purpleHaze == 1 ? PURPLE_HAZE_1_DURATION : PURPLE_HAZE_2_DURATION,
			                              purpleHaze == 1 ? PURPLE_HAZE_1_TRANSFERS : PURPLE_HAZE_2_TRANSFERS);
			mHazedMobs.put(entity.getUniqueId(), hazed);
			Location loc = hazed.mob.getLocation();
			mWorld.spawnParticle(Particle.SPELL_WITCH, loc.clone().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.001);
			putOnCooldown();
		}
		target = null;
	}
}
