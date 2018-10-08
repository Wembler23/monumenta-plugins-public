package com.playmonumenta.plugins.abilities;

import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.utils.ScoreboardUtils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AbilityCollection {

	private List<Ability> mAbilities;
	private Player mPlayer;

	public AbilityCollection(Player player) {
		mAbilities = new ArrayList<Ability>();
		mPlayer = player;
	}

	public List<Ability> getAbilities() {
		return mAbilities;
	}

	/**
	 * Removes the ability that is specified in the parameters.
	 * You can use the getAbility() methods in order to get the
	 * ability that needs to be removed.
	 * @param abil The ability that will be removed, if it exists
	 */
	public void removeAbility(Ability abil) {
		if (mAbilities.contains(abil)) {
			abil.player = null;
			mAbilities.remove(abil);
		}
	}

	public void addAbility(Ability abil) {
		abil.player = mPlayer;
//		abil.mPlugin = Plugin.getInstance();
//		abil.mWorld = abil.mPlugin.mWorld;
//		abil.mRandom = abil.mPlugin.mRandom;
		mAbilities.add(abil);
	}

	public Ability getAbility(String scoreboardId) {
		for (Ability abil : mAbilities) {
			if (abil.getInfo() != null) {
				AbilityInfo info = abil.getInfo();
				if (info.scoreboardId != null) {
					if (scoreboardId.equals(info.scoreboardId)) {
						return abil;
					}
				}
			}
		}
		return null;
	}

	public Ability getAbility(Spells spell) {
		for (Ability abil : mAbilities) {
			if (abil.getInfo() != null) {
				AbilityInfo info = abil.getInfo();
				if (info.linkedSpell != null) {
					if (spell.equals(info.linkedSpell)) {
						return abil;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Refreshes the player's ability collection.
	 */
	public void refreshAbilities() {
		mAbilities.clear();
		for (Ability ab : Ability.getAbilities()) {
			Ability abil = ab.getInstance();
			abil.player = mPlayer;
			if (abil.getInfo() != null) {
				AbilityInfo info = abil.getInfo();
				Bukkit.broadcastMessage(info.scoreboardId);
				if (abil.canUse(mPlayer)) {
					if (info.scoreboardId != null) {
						int score = ScoreboardUtils.getScoreboardValue(mPlayer, info.scoreboardId);
						if (score > 0) {
							Bukkit.broadcastMessage("add " + info.scoreboardId);
							addAbility(abil);
						}
					} else {
						addAbility(abil);
					}
				}
			}
		}
	}
}
