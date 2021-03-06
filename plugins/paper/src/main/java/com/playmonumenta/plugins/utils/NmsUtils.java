package com.playmonumenta.plugins.utils;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftTrident;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import com.playmonumenta.plugins.Plugin;

import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityDamageSource;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.Vec3D;

public class NmsUtils {
	public static void resetPlayerIdleTimer(Player player) {
		CraftPlayer p = (CraftPlayer)player;
		EntityPlayer playerHandle = p.getHandle();
		playerHandle.resetIdleTimer();
	}

	//Returns the trident projectile as an ItemStack
	public static ItemStack getTridentItem(Trident trident) {
		return ((CraftTrident) trident).getHandle().trident.getBukkitStack();
	}

	public static void removeVexSpawnAIFromEvoker(LivingEntity boss) {
		/* TODO: This has not yet been ported from 1.13! */
		Plugin.getInstance().getLogger().severe("Attempted to remove vex spawn from evoker but this code has not been ported from 1.13!");
	}

	private static class CustomDamageSource extends EntityDamageSource {
		String mKilledUsingMsg;

		public CustomDamageSource(Entity damager, @Nullable String killedUsingMsg) {
			super("custom", damager);

			if (killedUsingMsg == null) {
				mKilledUsingMsg = "magic";
			} else {
				mKilledUsingMsg = killedUsingMsg;
			}
		}

		@Override
		public IChatBaseComponent getLocalizedDeathMessage(EntityLiving entityliving) {
			// death.attack.indirectMagic.item=%1$s was killed by %2$s using %3$s
			String s = "death.attack.indirectMagic.item";
			return new ChatMessage(s, new Object[] { entityliving.getScoreboardDisplayName(), this.x.getScoreboardDisplayName(), mKilledUsingMsg});
		}
	}

	public static void customDamageEntity(@Nonnull LivingEntity entity, double amount, @Nonnull Player damager) {
		customDamageEntity(entity, amount, damager, null);
	}

	public static void customDamageEntity(@Nonnull LivingEntity entity, double amount, @Nonnull Player damager, @Nullable String killedUsingMsg) {
        DamageSource reason = new CustomDamageSource(((CraftHumanEntity) damager).getHandle(), killedUsingMsg);

        ((CraftLivingEntity)entity).getHandle().damageEntity(reason, (float) amount);
	}

	private static class UnblockableEntityDamageSource extends EntityDamageSource {
		public UnblockableEntityDamageSource(Entity entity) {
			super("custom", entity);
		}

		@Override
		public Vec3D w() {
			return null;
		}

		@Override
		public IChatBaseComponent getLocalizedDeathMessage(EntityLiving entityliving) {
			String s = "death.attack.mob";
			return new ChatMessage(s, new Object[] { entityliving.getScoreboardDisplayName(), this.x.getScoreboardDisplayName()});
		}

	}

	public static void unblockableEntityDamageEntity(@Nonnull LivingEntity damagee, double amount, @Nonnull LivingEntity damager) {
        DamageSource reason = new UnblockableEntityDamageSource(damager == null ? null : ((CraftLivingEntity) damager).getHandle());

        ((CraftLivingEntity)damagee).getHandle().damageEntity(reason, (float) amount);
	}

	private static Object getPrivateField(String fieldName, Class<?> clazz, Object object) throws NoSuchFieldException, IllegalAccessException {
		Field field;
		Object o = null;

		field = clazz.getDeclaredField(fieldName);

		field.setAccessible(true);

		o = field.get(object);

		return o;
	}
}
