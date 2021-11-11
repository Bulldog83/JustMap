package ru.bulldog.justmap.map.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import ru.bulldog.justmap.util.PosUtil;

public class WorldKey {
	private final Identifier dimension;
	private String worldId;
	private String worldName;
	private BlockPos worldPos;

	public WorldKey(Identifier dimension) {
		this.dimension = dimension;
		this.worldId = this.dimension.toString();
	}

	public WorldKey(RegistryKey<World> worldKey) {
		this(worldKey.getValue());
	}

	public void setWorldName(String name) {
		this.worldName = name;
		// if specified, directly use world name as key
		this.worldId = name;
	}

	public void setWorldPos(BlockPos worldPos) {
		this.worldPos = worldPos;
		if (worldName == null) {
			this.worldId = String.format("%s_%s", dimension, PosUtil.shortPosString(worldPos));
		}
	}

	public Identifier getDimension() {
		return this.dimension;
	}

	public String getName() {
		return this.worldName;
	}

	public BlockPos getWorldPos() {
		return this.worldPos;
	}

	public void clearName() {
		this.worldName = null;
		if (worldPos != null) {
			this.worldId = String.format("%s_%s", dimension, PosUtil.shortPosString(worldPos));
		} else {
			this.worldId = dimension.toString();
		}
	}

	public void clearWorldPos() {
		this.worldPos = null;
		if (worldName != null) {
			this.worldId = worldName;
		} else {
			this.worldId = dimension.toString();
		}
	}

	public String toFolder() {
		String folder = this.worldId.replaceAll("[/ ]+", "_");
		folder = folder.replaceAll("[,:&\"|<>?*]", "_");

		return folder;
	}

	public JsonElement toJson() {
		JsonObject jsonKey = new JsonObject();
		jsonKey.addProperty("dimension", this.dimension.toString());
		if (worldName != null) {
			// if name is set, don't save the position
			jsonKey.addProperty("name", worldName);
			return jsonKey;
		}
		if (worldPos != null) {
			jsonKey.add("position", PosUtil.toJson(worldPos));
		}
		return jsonKey;
	}

	public static WorldKey fromJson(JsonObject element) {
		if (!element.has("dimension")) return null;
		Identifier dimension = new Identifier(JsonHelper.getString(element, "dimension"));
		WorldKey worldKey = new WorldKey(dimension);
		if (element.has("name")) {
			worldKey.setWorldName(JsonHelper.getString(element, "name"));
			return worldKey;
		}
		if (element.has("position")) {
			BlockPos worldPos = PosUtil.fromJson(JsonHelper.getObject(element, "position"));
			worldKey.setWorldPos(worldPos);
		}

		return worldKey;
	}

	@Override
	public String toString() {
		return String.format("WorldKey [%s]", this.worldId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof WorldKey)) return false;
		WorldKey anotherKey = (WorldKey) obj;
		return this.worldId.equals(anotherKey.worldId);
	}

	@Override
	public int hashCode() {
		return this.worldId.hashCode();
	}
}
