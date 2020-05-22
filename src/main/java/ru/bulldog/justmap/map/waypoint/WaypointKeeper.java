package ru.bulldog.justmap.map.waypoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.StorageUtil;

public class WaypointKeeper extends JsonFactory {
	
	private static Map<Identifier, List<Waypoint>> waypoints;	
	
	private static WaypointKeeper instance;
	private static File currentStorage;
	
	public static WaypointKeeper getInstance() {
		if (instance == null) {
			instance = new WaypointKeeper();
		}		
		
		File waypointsFile = new File(StorageUtil.filesDir(), "/waypoints.json");
		if (currentStorage == null || !currentStorage.equals(waypointsFile)) {
			currentStorage = waypointsFile;
			instance.loadWaypoints();
		}		
		
		return instance;
	}
	
	private WaypointKeeper() {}
	
	private void loadWaypoints() {
		waypoints = new HashMap<>();		
		if (currentStorage.exists()) {
			JsonObject jsonObject = loadJson(currentStorage);
			if (jsonObject.has("waypoints")) {
				JsonArray waypointObject = jsonObject.getAsJsonArray("waypoints");
				for(JsonElement elem : waypointObject) {
					Waypoint wp = Waypoint.fromJson((JsonObject) elem);				
					getWaypoints(wp.dimension, false).add(wp);
				}
			}
		}
	}
	
	public void saveWaypoints() {
		JsonArray waypointArray = new JsonArray();
		for (Entry<Identifier, List<Waypoint>> entry : waypoints.entrySet()) {
			List<Waypoint> list = entry.getValue();			
			for (Waypoint wp : list) {
				waypointArray.add(wp.toJson());
			}
		}
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("waypoints", waypointArray);
		
		File waypointsFile = new File(StorageUtil.filesDir(), "/waypoints.json");
		storeJson(waypointsFile, jsonObject);
	}
	
	public void addNew(Waypoint waypoint) {
		getWaypoints(waypoint.dimension, false).add(waypoint);
	}
	
	public void remove(Waypoint waypoint) {
		getWaypoints(waypoint.dimension, false).remove(waypoint);
	}
	
	public List<Waypoint> getWaypoints(Identifier dimension, boolean hiddenFilter) {
		List<Waypoint> list;
		if (waypoints.get(dimension) == null) {
			list = new ArrayList<>();
			waypoints.put(dimension, list);
		} else {
			list = waypoints.get(dimension);
		}
		
		if (hiddenFilter) {
			return list.stream().filter(Waypoint::isVisible)
								.collect(Collectors.toList());
		}
		
		return list;
	}
	
	public List<Identifier> getDimensions() {
		return new ArrayList<>(waypoints.keySet());
	}
}
