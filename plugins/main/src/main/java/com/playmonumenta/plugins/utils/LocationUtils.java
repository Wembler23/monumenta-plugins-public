package com.playmonumenta.plugins.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class LocationUtils {
	public static Vector getDirectionTo(Location to, Location from) {
		Vector vFrom = from.toVector();
		Vector vTo = to.toVector();
		return vTo.subtract(vFrom).normalize();
	}

	public static Location getEntityCenter(Entity e) {
		return e.getLocation().add(0, e.getHeight() / 2, 0);
	}

	public static boolean isLosBlockingBlock(Material mat) {
		return mat.isOccluding();
	}

	public static boolean isPathBlockingBlock(Material mat) {
		return mat.isSolid() || mat.equals(Material.LAVA);
	}

	public static boolean isWaterlogged(Block block) {
		BlockData data = block.getBlockData();
		if (data != null && data instanceof Waterlogged) {
			return ((Waterlogged)data).isWaterlogged();
		}
		return false;
	}

	public static boolean containsWater(Block block) {
		if (isWaterlogged(block)) {
			return true;
		}
		Material mat = block.getType();
		if (mat.equals(Material.BUBBLE_COLUMN) ||
		    mat.equals(Material.KELP) ||
		    mat.equals(Material.KELP_PLANT) ||
		    mat.equals(Material.SEAGRASS) ||
		    mat.equals(Material.TALL_SEAGRASS)) {
			return true;
		}
		return false;
	}

	public static boolean isRail(Block block) {
		BlockData data = block.getBlockData();
		if (data != null && data instanceof Rail) {
			return true;
		}
		return false;
	}

	public static boolean isValidMinecartLocation(Location loc) {
		Block block = loc.getBlock();
		if (isRail(block)) {
			return true;
		}

		block = loc.subtract(0, 1, 0).getBlock();
		if (isRail(block)) {
			return true;
		}

		/*
		 * Check up to 50 blocks underneath the location. Stop when
		 * a non-air block is hit. If it's a liquid, this is allowed, otherwise it's not
		 */
		loc = loc.clone();
		for (int i = loc.getBlockY(); i > (Math.max(0, loc.getBlockY() - 50)); i--) {
			loc.setY(i);
			block = loc.getBlock();
			if (isRail(block)) {
				return true;
			} else if (!block.isEmpty()) {
				return false;
			}
		}

		return false;
	}

	public static boolean isLocationInWater(Location loc) {
		Block block = loc.getBlock();
		if (block.isLiquid() || containsWater(block)) {
			return true;
		}

		block = loc.subtract(0, 1, 0).getBlock();
		if (block.isLiquid() || containsWater(block)) {
			return true;
		}

		return false;
	}

	public static boolean isValidBoatLocation(Location loc) {
		if (isLocationInWater(loc)) {
			return true;
		}

		/*
		 * Check up to 50 blocks underneath the location. Stop when
		 * a non-air block is hit. If it's a liquid, this is allowed, otherwise it's not
		 */
		loc = loc.clone();
		for (int i = loc.getBlockY(); i > (Math.max(0, loc.getBlockY() - 50)); i--) {
			loc.setY(i);
			Block block = loc.getBlock();
			if (block.isLiquid() || containsWater(block)) {
				return true;
			} else if (!block.isEmpty()) {
				return false;
			}
		}

		return false;
	}

	public static boolean hasLosToLocation(Location fromLocation, Location toLocation) {
		int range = (int)fromLocation.distance(toLocation) + 1;
		Vector direction = toLocation.toVector().subtract(fromLocation.toVector()).normalize();

		BlockIterator bi = new BlockIterator(fromLocation.getWorld(), fromLocation.toVector(), direction, 0, range);

		while (bi.hasNext()) {
			Block b = bi.next();

			//  If we want to check Line of sight we want to make sure the the blocks are transparent.
			if (LocationUtils.isLosBlockingBlock(b.getType())) {
				return false;
			}
		}

		return true;
	}

	// Search a cuboid around a Location and return the first Location found with a block matching one of the given Materials
	public static Location getNearestBlock(Location center, int radius, Material... materials) {
		int cx = center.getBlockX();
		int cy = center.getBlockY();
		int cz = center.getBlockZ();
		World world = center.getWorld();
		Location nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (double x = cx - radius; x <= cx + radius; x++) {
			for (double z = cz - radius; z <= cz + radius; z++) {
				for (double y = (cy - radius); y <= (cy + radius); y++) {
					Location loc = new Location(world, x, y, z);
					double distance = Math.sqrt(((cx - x) * (cx - x)) + ((cz - z) * (cz - z)) + ((cy - y) * (cy - y)));
					if (distance < nearestDistance) {
						for (Material material : materials) {
							if (loc.getBlock().getType() == material) {
								nearest = loc;
								nearestDistance = distance;
								break;
							}
						}
					}
				}
			}
		}
		return nearest;
	}

	// Search a cuboid around a Location and return a List of all Chests inside the area
	public static List<Chest> getNearbyChests(Location center, int radius) {
		int cx = center.getBlockX();
		int cy = center.getBlockY();
		int cz = center.getBlockZ();
		World world = center.getWorld();
		List<Chest> chests = new ArrayList<Chest>();

		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int z = cz - radius; z <= cz + radius; z++) {
				for (int y = (cy - radius); y < (cy + radius); y++) {
					Location loc = new Location(world, x, y + 2, z);
					if (loc.getBlock().getState() instanceof Chest) {
						chests.add((Chest) loc.getBlock().getState());
					}
				}
			}
		}
		return chests;
	}
}
