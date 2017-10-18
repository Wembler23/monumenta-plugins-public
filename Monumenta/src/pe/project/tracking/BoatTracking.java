package pe.project.tracking;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;

import pe.project.locations.safezones.SafeZoneConstants.SafeZones;
import pe.project.managers.LocationManager;

public class BoatTracking implements EntityTracking {
	private Set<Boat> mEntities = new HashSet<Boat>();
	
	@Override
	public void addEntity(Entity entity) {
		mEntities.add((Boat)entity);
	}
	
	@Override
	public void removeEntity(Entity entity) {
		mEntities.remove(entity);
	}

	@Override
	public void update(World world, int ticks) {
		Iterator<Boat> boatIter = mEntities.iterator();
		while (boatIter.hasNext()) {
			Boat boat = boatIter.next();
			if (boat != null && boat.isValid()) {
				SafeZones safeZone = LocationManager.withinAnySafeZone(boat);
				if (safeZone != SafeZones.None) {
					boatIter.remove();
					boat.remove();
				}
			} else {
				boatIter.remove();
			}
		}
	}

	@Override
	public void unloadTrackedEntities() {
		mEntities.clear();
	}
}
