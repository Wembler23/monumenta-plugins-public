package pe.project.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Stray;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import pe.project.Plugin;
import pe.project.managers.potion.PotionManager.PotionID;
import pe.project.utils.EntityUtils;
import pe.project.utils.InventoryUtils;
import pe.project.utils.MessagingUtils;
import pe.project.utils.MovementUtils;
import pe.project.utils.ParticleUtils;
import pe.project.utils.PlayerUtils;
import pe.project.utils.ScoreboardUtils;

/*
	AmplifyingHex
	Blasphemy
	CursedWound
	GraspingClaws
	SoulRend
	VersatileMagic
*/

public class WarlockClass extends BaseClass {
	private static int AMPLIFYING_ID = 71;
	private static int AMPLIFYING_1_EFFECT_DAMAGE = 4;
	private static int AMPLIFYING_2_EFFECT_DAMAGE = 6;
	private static int AMPLIFYING_FIRE_DURATION = 4 * 20;
	private static int AMPLIFYING_RADIUS = 8;
	private static double AMPLIFYING_DOT_ANGLE = 0.33;
	private static int AMPLIFYING_COOLDOWN = 16 * 20;

	private static int BLASPHEMY_RADIUS = 3;
	private static float BLASPHEMY_KNOCKBACK_SPEED = 0.4f;
	private static int BLASPHEMY_WITHER_LEVEL = 0;
	private static int BLASPHEMY_WITHER_DURATION = 10 * 20;
	
	private static int CURSED_WOUND_1_EFFECT_LEVEL = 0;
	private static int CURSED_WOUND_1_DURATION = 8 * 20;
	private static int CURSED_WOUND_2_EFFECT_LEVEL = 1;	
	private static int CURSED_WOUND_2_DURATION = 7 * 20;
	
	private static int GRASPING_CLAWS_ID = 74;
	private static int GRASPING_CLAWS_RADIUS = 6;
	private static float GRASPING_CLAWS_SPEED = 0.25f;
	private static int GRASPING_CLAWS_MAIN_EFFECT_LEVEL = 2;
	private static int GRASPING_CLAWS_SECOND_EFFECT_LEVEL = 0;
	private static int GRASPING_CLAWS_FIRE_DURATION = 7 * 20;
	private static int GRASPING_CLAWS_DURATION = 7 * 20;
	private static int GRASPING_CLAWS_COOLDOWN = 24 * 20;

	private static int SOUL_REND_ID = 75;
	private static double SOUL_REND_HEAL_MULT = 0.333;
	private static int SOUL_REND_RADIUS = 6;
	private static int SOUL_REND_COOLDOWN = 10 * 20;

	public WarlockClass(Plugin plugin, Random random) {
		super(plugin, random);
	}
	

	@Override
	public void AbilityOffCooldown(Player player, int abilityID) {
		if (abilityID == AMPLIFYING_ID) {
			MessagingUtils.sendActionBarMessage(mPlugin, player, "Amplifying Hex is now off cooldown");
		} else if (abilityID == GRASPING_CLAWS_ID) {
			MessagingUtils.sendActionBarMessage(mPlugin, player, "Grasping Claws is now off cooldown");
		} else if (abilityID == SOUL_REND_ID) {
			MessagingUtils.sendActionBarMessage(mPlugin, player, "Soul Rend is now off cooldown");
		}
	}


	@Override
	public void PlayerItemHeldEvent(Player player, ItemStack mainHand, ItemStack offHand) {
		_testItemsInHands(player, mainHand, offHand);
	}

	
/// VERSATILE MAGIC
	private void _testItemsInHands(Player player, ItemStack mainHand, ItemStack offHand) {
		int versatileMagic = ScoreboardUtils.getScoreboardValue(player, "VersatileMagic");
		if (versatileMagic > 0) {
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.DAMAGE_RESISTANCE);
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.INCREASE_DAMAGE);
			mPlugin.mPotionManager.removePotion(player, PotionID.ABILITY_SELF, PotionEffectType.SPEED);

			//	Player has a wand in their offHand
			if (InventoryUtils.isWandItem(offHand)) {
				mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 0, true, false));
				if (InventoryUtils.isScytheItem(mainHand) && versatileMagic > 1) {	// If they also have a scythe in the mainHand
					mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000000, 0, true, false));
				}
			}
			//	Player has a scythe in their offHand
			else if (InventoryUtils.isScytheItem(offHand)) {
				mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.SPEED, 1000000, 0, true, false));
				if (InventoryUtils.isWandItem(mainHand) && versatileMagic > 1) {		// If they also have a wand in the mainHand
					mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000000, 0, true, false));
				}
			}
		}
	}

	
/// BLASPHEMOUS AURA	
	@Override
	public void PlayerDamagedByLivingEntityEvent(Player player, LivingEntity damager, double damage) {
		if (!(damager instanceof Player)) {
			//	ABILITY: Blasphemous Aura
			{
				int blasphemy = ScoreboardUtils.getScoreboardValue(player, "BlasphemousAura");
				if (blasphemy > 0) {
					Location loc = player.getLocation();
					World world = player.getWorld();
					
					ParticleUtils.playParticlesInWorld(world, Particle.SMOKE_NORMAL, loc.add(0, 1, 0), 30, 1.5, 0.6, 1.5, 0.001);
					ParticleUtils.playParticlesInWorld(world, Particle.SPELL, loc.add(0, 1, 0), 30, 1.5, 0.6, 1.5, 0.001);
					world.playSound(loc, "entity.player.attack.knockback", 0.8f, 0.6f);
	
					List<Entity> entities = player.getNearbyEntities(BLASPHEMY_RADIUS, BLASPHEMY_RADIUS, BLASPHEMY_RADIUS);
					for(int i = 0; i < entities.size(); i++) {
						Entity e = entities.get(i);
						if(EntityUtils.isHostileMob(e)) {
							LivingEntity mob = (LivingEntity)e;
							MovementUtils.KnockAway(player, mob, BLASPHEMY_KNOCKBACK_SPEED);
							if (blasphemy >= 1) {
								mob.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, BLASPHEMY_WITHER_DURATION, BLASPHEMY_WITHER_LEVEL, true, false));
							}
						}
					}
				}
			}
		}
	}

	
/// CURSED WOUND and SOUL REND
	@Override
	public boolean LivingEntityDamagedByPlayerEvent(Player player, LivingEntity damagee, double damage, DamageCause cause) {

	// First we need to check if we're holding a scythe, if not we can bail out of both
		if (InventoryUtils.isScytheItem(player.getInventory().getItemInMainHand())) {
			// Cursed Wound
			int cursedWound = ScoreboardUtils.getScoreboardValue(player, "CursedWound");
			if (cursedWound > 0) {
				ParticleUtils.playParticlesInWorld(player.getWorld(), Particle.LAVA, damagee.getLocation().add(0, 1, 0), 4, 0.15, 0.15, 0.15, 0.0);
				if (cursedWound == 1) {
					damagee.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, CURSED_WOUND_1_DURATION, CURSED_WOUND_1_EFFECT_LEVEL, true, false));		
				}
				else if (cursedWound == 2) {
					damagee.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, CURSED_WOUND_2_DURATION, CURSED_WOUND_2_EFFECT_LEVEL, true, false));			
				}
			}
			
			// Soul Rend
			int soulRend = ScoreboardUtils.getScoreboardValue(player, "SoulRend");
			if (soulRend > 0) {
				if (PlayerUtils.isCritical(player) && EntityUtils.isHostileMob(damagee)) {
					if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), SOUL_REND_ID)) {
	
						double soulHealValue = damage * SOUL_REND_HEAL_MULT;
						
						Location loc = player.getLocation();
						World world = player.getWorld();
						
						if (soulRend == 1) {ParticleUtils.playParticlesInWorld(world, Particle.DAMAGE_INDICATOR, loc.add(0, 1, 0), 18, 0.75, 0.5, 0.75, 0.0);}
						else if (soulRend == 2) {ParticleUtils.playParticlesInWorld(world, Particle.DAMAGE_INDICATOR, loc.add(0, 1, 0), 60, 2.0, 0.75, 2.0, 0.0);}
						world.playSound(loc, "entity.magmacube.squish", 1.0f, 0.66f);
						world.playSound(loc, "entity.player.attack.crit", 1.0f, 1.2f);
						
						List<Entity> entities = player.getNearbyEntities(SOUL_REND_RADIUS, SOUL_REND_RADIUS, SOUL_REND_RADIUS);
						entities.add(player);
						for (Entity entity : entities) {
							if (entity instanceof Player) {
								Player p = (Player)entity;

								//	If this is us or we're allowing anyone to get it.
								if (p == player || soulRend > 1) {
									PlayerUtils.healPlayer(p, soulHealValue);
								}
							}
						}
						
						//	Put Soul Rend on cooldown
						mPlugin.mTimers.AddCooldown(player.getUniqueId(), SOUL_REND_ID, SOUL_REND_COOLDOWN);
					}
				}
			}
		}
		
		return false;
	}

	
	/// GRASPING CLAWS
	@Override
	public void LivingEntityShotByPlayerEvent(Player player, Arrow arrow, LivingEntity damagee, EntityDamageByEntityEvent event) {
		int graspingClaws = ScoreboardUtils.getScoreboardValue(player, "GraspingClaws");
		if (graspingClaws > 0) {
			if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), GRASPING_CLAWS_ID)) {
				Location loc = damagee.getLocation();
				World world = damagee.getWorld();
				
				world.playSound(loc, "block.enchantment_table.use", 1.0f, 0.8f);
				ParticleUtils.playParticlesInWorld(world, Particle.ENCHANTMENT_TABLE, loc.add(0, 1, 0), 200, 3, 3, 3, 0.0);
				ParticleUtils.playParticlesInWorld(world, Particle.DRAGON_BREATH, loc.add(0, 1, 0), 50, 0.1, 0.1, 0.1, 0.0);
				
				List<Entity> entities = damagee.getNearbyEntities(GRASPING_CLAWS_RADIUS, GRASPING_CLAWS_RADIUS, GRASPING_CLAWS_RADIUS);
				for (Entity entity : entities) {
					if (EntityUtils.isHostileMob(entity)) {
						LivingEntity mob = (LivingEntity)entity;
						if (mob != damagee) {
							MovementUtils.PullTowards(damagee, mob, GRASPING_CLAWS_SPEED);
							if (graspingClaws == 2) {
								mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, GRASPING_CLAWS_DURATION , GRASPING_CLAWS_SECOND_EFFECT_LEVEL, true, false));			
								mob.setFireTicks(GRASPING_CLAWS_FIRE_DURATION);						
							}
						}
						else 
						{
							mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, GRASPING_CLAWS_DURATION , GRASPING_CLAWS_MAIN_EFFECT_LEVEL, true, false));			
							mob.setFireTicks(GRASPING_CLAWS_FIRE_DURATION);
						}
					}
				}
				
				//	Put Soul Rend on cooldown
				mPlugin.mTimers.AddCooldown(player.getUniqueId(), GRASPING_CLAWS_ID, GRASPING_CLAWS_COOLDOWN);
			}
		}
	}
	
	
	/// AMPLIFYING HEX
	public void PlayerInteractEvent(Player player, Action action, ItemStack itemInHand, Material blockClicked) {
		//	If sneaking and left click with a wand OR a scythe
		if (player.isSneaking()) {
			ItemStack mainHand = player.getInventory().getItemInMainHand();
			if (InventoryUtils.isScytheItem(mainHand) || InventoryUtils.isWandItem(mainHand)) {
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					int amplifyingHex = ScoreboardUtils.getScoreboardValue(player, "AmplifyingHex");
					if (amplifyingHex > 0) {
						if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), AMPLIFYING_ID)) {
							ParticleUtils.explodingConeEffect(mPlugin, player, AMPLIFYING_RADIUS, Particle.DRAGON_BREATH, 0.6f, Particle.SMOKE_NORMAL, 0.4f, AMPLIFYING_DOT_ANGLE);
							player.getWorld().playSound(player.getLocation(), "entity.polar_bear.warning", 1.0f, 1.6f);
							
							Vector playerDir = player.getEyeLocation().getDirection().setY(0).normalize();
							List<Entity> entities = player.getNearbyEntities(AMPLIFYING_RADIUS, AMPLIFYING_RADIUS, AMPLIFYING_RADIUS);
							for(Entity e : entities) {
								if(EntityUtils.isHostileMob(e)) {
									LivingEntity mob = (LivingEntity)e;

									Vector toMobVector = mob.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0).normalize();
									if (playerDir.dot(toMobVector) > AMPLIFYING_DOT_ANGLE) {
										int debuffCount = 0;
										
										if (mob.getPotionEffect(PotionEffectType.WITHER) != null) {debuffCount++;}
										if (mob.getPotionEffect(PotionEffectType.SLOW) != null) {debuffCount++;}
										if (mob.getPotionEffect(PotionEffectType.WEAKNESS) != null) {debuffCount++;}
										if (mob.getPotionEffect(PotionEffectType.SLOW_DIGGING) != null) {debuffCount++;}
										if (mob.getPotionEffect(PotionEffectType.POISON) != null) {debuffCount++;}
										if (mob.getFireTicks() > 0) {debuffCount++;}
										
										int damageMult = AMPLIFYING_1_EFFECT_DAMAGE;
										if (amplifyingHex == 2) {
											damageMult = AMPLIFYING_2_EFFECT_DAMAGE;
										}
										
										if (debuffCount > 0) {
											EntityUtils.damageEntity(mPlugin, mob, debuffCount * damageMult, player);
											
											if (amplifyingHex == 2) {
												mob.setFireTicks(debuffCount * AMPLIFYING_FIRE_DURATION);
											}
										}
									}
								}
							}
							
							//	Put Amplifying Hex on cooldown
							mPlugin.mTimers.AddCooldown(player.getUniqueId(), AMPLIFYING_ID, AMPLIFYING_COOLDOWN);
						}
					}
				}
			}
		}
	}
}