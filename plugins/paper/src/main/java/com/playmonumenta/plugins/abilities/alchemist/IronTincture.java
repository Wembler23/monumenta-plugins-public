package com.playmonumenta.plugins.abilities.alchemist;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.utils.AbsorptionUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;

public class IronTincture extends Ability {
	private static final int IRON_TINCTURE_THROW_COOLDOWN = 10 * 20;
	private static final int IRON_TINCTURE_USE_COOLDOWN = 50 * 20;
	private static final int IRON_TINCTURE_1_ABSORPTION = 8;
	private static final int IRON_TINCTURE_2_ABSORPTION = 12;
	private static final int IRON_TINCTURE_ABSORPTION_DURATION = 20 * 50;
	private static final int IRON_TINCTURE_TICK_PERIOD = 2;
	private static final double IRON_TINCTURE_VELOCITY = 0.7;

	private final int mAbsorption;

	public IronTincture(Plugin plugin, World world, Player player) {
		super(plugin, world, player, "Iron Tincture");
		mInfo.mLinkedSpell = Spells.IRON_TINCTURE;
		mInfo.mScoreboardId = "IronTincture";
		mInfo.mShorthandName = "IT";
		mInfo.mDescriptions.add("Crouch and right-click to throw a tincture. If you walk over the tincture, gain 8 absorption health for 50 seconds, up to 8 absorption health. If an ally walks over it, or is hit by it, you both gain the effect. If it isn't grabbed before it disappears it will quickly come off cooldown. Cooldown: 50 seconds.");
		mInfo.mDescriptions.add("Effect and effect cap increased to 12 absorption health.");
		mInfo.mCooldown = IRON_TINCTURE_USE_COOLDOWN; // Full duration cooldown
		mInfo.mTrigger = AbilityTrigger.RIGHT_CLICK;
		mAbsorption = getAbilityScore() == 1 ? IRON_TINCTURE_1_ABSORPTION : IRON_TINCTURE_2_ABSORPTION;
	}

	@Override
	public boolean runCheck() {
		ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
		return mPlayer.isSneaking()
		       && !InventoryUtils.isBowItem(mainHand)
		       && mainHand.getType() != Material.SPLASH_POTION
		       && mainHand.getType() != Material.LINGERING_POTION;
	}

	@Override
	public void cast(Action action) {
		Location loc = mPlayer.getLocation().add(0, 1.8, 0);
		ItemStack itemTincture = new ItemStack(Material.SPLASH_POTION);
		mWorld.playSound(loc, Sound.ENTITY_SNOWBALL_THROW, 1, 0.15f);
		Item tincture = mWorld.dropItem(loc, itemTincture);
		tincture.setPickupDelay(Integer.MAX_VALUE);

		Vector vel = mPlayer.getEyeLocation().getDirection().normalize();
		vel.multiply(IRON_TINCTURE_VELOCITY);

		tincture.setVelocity(vel);
		tincture.setGlowing(true);

		// Full duration cooldown - is shortened if not picked up
		putOnCooldown();

		new BukkitRunnable() {
			int mTinctureDecay = 0;

			@Override
			public void run() {
				mWorld.spawnParticle(Particle.SPELL, tincture.getLocation(), 3, 0, 0, 0, 0.1);

				for (Player p : PlayerUtils.playersInRange(tincture.getLocation(), 1)) {
					// Prevent players from picking up their own tincture instantly
					if (p == mPlayer && tincture.getTicksLived() < 12) {
						continue;
					}

					mWorld.playSound(tincture.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.85f);
					mWorld.spawnParticle(Particle.BLOCK_DUST, tincture.getLocation(), 50, 0.1, 0.1, 0.1, 0.1, Material.GLASS.createBlockData());
					mWorld.spawnParticle(Particle.FIREWORKS_SPARK, tincture.getLocation(), 30, 0.1, 0.1, 0.1, 0.2);
					tincture.remove();

					execute(mPlayer);
					if (p != mPlayer) {
						execute(p);
					}

					mPlugin.mTimers.removeCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell);
					putOnCooldown();

					this.cancel();
					return;
				}

				mTinctureDecay += IRON_TINCTURE_TICK_PERIOD;
				if (mTinctureDecay >= IRON_TINCTURE_THROW_COOLDOWN || !tincture.isValid() || tincture.isDead()) {
					tincture.remove();
					this.cancel();

					// Take the skill off cooldown (by setting to 0)
					mPlugin.mTimers.addCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell, 0);
				}
			}

		}.runTaskTimer(mPlugin, 0, IRON_TINCTURE_TICK_PERIOD);
	}

	private void execute(Player player) {
		AbsorptionUtils.addAbsorption(player, mAbsorption, mAbsorption, IRON_TINCTURE_ABSORPTION_DURATION);

		mWorld.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.2f, 1.0f);
		mWorld.spawnParticle(Particle.FLAME, player.getLocation(), 30, 0.25, 0.1, 0.25, 0.125);
		new BukkitRunnable() {
			double mRotation = 0;
			double mY = 0.15;
			final double mRadius = 1.15;
			@Override
			public void run() {
				Location loc = player.getLocation();
				mRotation += 15;
				mY += 0.175;
				for (int i = 0; i < 3; i++) {
					double degree = Math.toRadians(mRotation + (i * 120));
					loc.add(Math.cos(degree) * mRadius, mY, Math.sin(degree) * mRadius);
					mWorld.spawnParticle(Particle.FLAME, loc, 1, 0.05, 0.05, 0.05, 0.05);
					mWorld.spawnParticle(Particle.SPELL_INSTANT, loc, 2, 0.05, 0.05, 0.05, 0);
					loc.subtract(Math.cos(degree) * mRadius, mY, Math.sin(degree) * mRadius);
				}

				if (mY >= 1.8) {
					this.cancel();
				}
			}

		}.runTaskTimer(mPlugin, 0, 1);
	}
}
