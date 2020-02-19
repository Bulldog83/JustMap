package ru.bulldog.justmap.minimap.waypoint;

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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.MinecraftServer;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.JsonFactory;

public class WaypointKeeper extends JsonFactory {
	
	private final static File WAYPOINTS_DIR = new File(JustMap.MAP_DIR, "waypoints/");
	
	private static Map<Integer, List<Waypoint>> waypoints;	
	
	private static WaypointKeeper instance;
	private static File currentStorage;
	
	public static WaypointKeeper getInstance() {
		if (instance == null) {
			instance = new WaypointKeeper();
		}		
		
		File waypointsFile = new File(waypointsFolder(), "/waypoints.json");
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
		for (Entry<Integer, List<Waypoint>> entry : waypoints.entrySet()) {
			List<Waypoint> list = entry.getValue();			
			for (Waypoint wp : list) {
				waypointArray.add(wp.toJson());
			}
		}
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("waypoints", waypointArray);
		
		File waypointsFile = new File(waypointsFolder(), "/waypoints.json");
		storeJson(waypointsFile, jsonObject);
	}
	
	public void addNew(Waypoint waypoint) {
		getWaypoints(waypoint.dimension, false).add(waypoint);
	}
	
	public void remove(Waypoint waypoint) {
		getWaypoints(waypoint.dimension, false).remove(waypoint);
	}
	
	public List<Waypoint> getWaypoints(int dimension, boolean hiddenFilter) {
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
	
	public List<Integer> getDimensions() {
		return new ArrayList<>(waypoints.keySet());
	}
	
	private static File waypointsFolder() {
		MinecraftClient client = MinecraftClient.getInstance();
		
		File waypointsDir;
		if (client.isIntegratedServerRunning()) {
			MinecraftServer server = client.getServer();
			waypointsDir = new File(WAYPOINTS_DIR, String.format("local/%s/", server.getLevelName()));
		} else if (!client.isInSingleplayer()) {
			ServerInfo server = client.getCurrentServerEntry();
			waypointsDir = new File(WAYPOINTS_DIR, String.format("servers/%s/", server.name));
		} else {		
			waypointsDir = new File(WAYPOINTS_DIR, "undefined/");
		}
		
		if (!waypointsDir.exists()) {
			waypointsDir.mkdirs();
		}
		
		return waypointsDir;
	}
}
