package com.playmonumenta.bossfights;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.playmonumenta.bossfights.bosses.BossAbilityGroup;
import com.playmonumenta.bossfights.events.SpellCastEvent;
import com.playmonumenta.bossfights.spells.Spell;

public class Boss {
	private final Plugin mPlugin;
	private final List<BossAbilityGroup> mAbilities;

	public Boss(Plugin plugin, BossAbilityGroup ability) {
		mPlugin = plugin;
		mAbilities = new ArrayList<BossAbilityGroup>(5);
		mAbilities.add(ability);
	}

	public void add(BossAbilityGroup ability) {
		mAbilities.add(ability);
	}

	public void bossDamagedByEntity(EntityDamageByEntityEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			if (!event.isCancelled()) {
				ability.bossDamagedByEntity(event);

				if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
					for (Spell passives : ability.getPassives()) {
						passives.bossDamagedByEntity(event);
					}
				}

				if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
					for (Spell actives : ability.getActiveSpells()) {
						actives.bossDamagedByEntity(event);
					}
				}
			}
		}
	}

	public void bossDamagedEntity(EntityDamageByEntityEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			if (!event.isCancelled()) {
				ability.bossDamagedEntity(event);

				if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
					for (Spell passives : ability.getPassives()) {
						passives.bossDamagedEntity(event);
					}
				}

				if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
					for (Spell actives : ability.getActiveSpells()) {
						actives.bossDamagedEntity(event);
					}
				}
			}
		}
	}

	public void bossLaunchedProjectile(ProjectileLaunchEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			if (!event.isCancelled()) {
				ability.bossLaunchedProjectile(event);

				if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
					for (Spell passives : ability.getPassives()) {
						passives.bossLaunchedProjectile(event);
					}
				}

				if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
					for (Spell actives : ability.getActiveSpells()) {
						actives.bossLaunchedProjectile(event);
					}
				}
			}
		}
	}

	public void bossProjectileHit(ProjectileHitEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			ability.bossProjectileHit(event);
			if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
				for (Spell passives : ability.getPassives()) {
					passives.bossProjectileHit(event);
				}
			}

			if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
				for (Spell actives : ability.getActiveSpells()) {
					actives.bossProjectileHit(event);
				}
			}
		}
	}

	public void bossHitByProjectile(ProjectileHitEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			ability.bossHitByProjectile(event);
			if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
				for (Spell passives : ability.getPassives()) {
					passives.bossHitByProjectile(event);
				}
			}

			if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
				for (Spell actives : ability.getActiveSpells()) {
					actives.bossHitByProjectile(event);
				}
			}
		}
	}

	public void areaEffectAppliedToBoss(AreaEffectCloudApplyEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			ability.areaEffectAppliedToBoss(event);

			if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
				for (Spell passives : ability.getPassives()) {
					passives.areaEffectAppliedToBoss(event);
				}
			}

			if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
				for (Spell actives : ability.getActiveSpells()) {
					actives.areaEffectAppliedToBoss(event);
				}
			}
		}
	}

	public void splashPotionAppliedToBoss(PotionSplashEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			ability.splashPotionAppliedToBoss(event);

			if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
				for (Spell passives : ability.getPassives()) {
					passives.splashPotionAppliedToBoss(event);
				}
			}

			if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
				for (Spell actives : ability.getActiveSpells()) {
					actives.splashPotionAppliedToBoss(event);
				}
			}
		}
	}

	public void bossCastAbility(SpellCastEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			ability.bossCastAbility(event);
			if (ability.getPassives() != null && !ability.getPassives().isEmpty()) {
				for (Spell passives : ability.getPassives()) {
					passives.bossCastAbility(event);
				}
			}

			if (ability.getActiveSpells() != null && !ability.getActiveSpells().isEmpty()) {
				for (Spell actives : ability.getActiveSpells()) {
					actives.bossCastAbility(event);
				}
			}
		}
	}

	public void bossPathfind(EntityPathfindEvent event) {
		for (BossAbilityGroup ability : mAbilities) {
			ability.bossPathfind(event);
		}
	}

	public void unload() {
		/* NOTE
		 *
		 * Unload will cause state to be serialized to the mob's equipment. This is fine if
		 * only one of the BossAbilityGroup's has data to serialize - but if not, only the
		 * last ability will actually have saved data, and the boss will fail to initialize
		 * later.
		 *
		 * Overcoming this limitation requires substantial refactoring.
		 */
		for (BossAbilityGroup ability : mAbilities) {
			ability.unload();
		}

		/*
		 * Clear mAbilities the next available chance - this prevents ConcurrentModificationException's
		 * when the boss dies or is unloaded as a result of abilities
		 */
		new BukkitRunnable() {
			@Override
			public void run() {
				mAbilities.clear();
			}
		}.runTaskLater(mPlugin, 0);
	}

	public void death() {
		for (BossAbilityGroup ability : mAbilities) {
			ability.mDead = true;
			ability.death();
		}
	}
}
