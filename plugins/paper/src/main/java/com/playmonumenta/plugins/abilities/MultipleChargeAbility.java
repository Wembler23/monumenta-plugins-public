package com.playmonumenta.plugins.abilities;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.scriptedquests.utils.MessagingUtils;

public class MultipleChargeAbility extends Ability {

	private final int mMaxCharges;

	private int mCharges;
	private boolean mWasOnCooldown;

	public MultipleChargeAbility(Plugin plugin, World world, Player player,
			String displayName, int maxCharges1, int maxCharges2) {
		super(plugin, world, player, displayName);
		mMaxCharges = getAbilityScore() == 1 ? maxCharges1 : maxCharges2;
		mCharges = mMaxCharges;
	}

	// Call this when the ability is cast; returns whether a charge was consumed or not
	protected boolean consumeCharge() {
		if (mCharges > 0) {
			mCharges--;
			PlayerUtils.callAbilityCastEvent(mPlayer, mInfo.mLinkedSpell);
			MessagingUtils.sendActionBarMessage(mPlayer, mInfo.mLinkedSpell.getName() + " Charges: " + mCharges);

			return true;
		}

		return false;
	}

	// This must be manually called if PeriodicTrigger is overridden by the superclass
	protected void manageChargeCooldowns() {
		boolean onCooldown = mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell);

		// If the skill is somehow on cooldown when charges are full, take it off cooldown
		if (mCharges == mMaxCharges && onCooldown) {
			mPlugin.mTimers.removeCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell);
		}

		// Increment charges if last check was on cooldown, and now is off cooldown.
		if (mCharges < mMaxCharges && mWasOnCooldown && !onCooldown) {
			mCharges++;
			MessagingUtils.sendActionBarMessage(mPlayer, mInfo.mLinkedSpell.getName() + " Charges: " + mCharges);
		}

		// Put on cooldown if charges can still be gained
		if (mCharges < mMaxCharges && !onCooldown) {
			putOnCooldown();
		}

		mWasOnCooldown = onCooldown;
	}

	// Remove the call to AbilityCastEvent, which is done instead on charge consumption
	@Override
	public void putOnCooldown() {
		if (!mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell)) {
			mPlugin.mTimers.addCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell, mInfo.mCooldown);
		}
	}

	@Override
	public void periodicTrigger(boolean fourHertz, boolean twoHertz, boolean oneSecond, int ticks) {
		manageChargeCooldowns();
	}

}
