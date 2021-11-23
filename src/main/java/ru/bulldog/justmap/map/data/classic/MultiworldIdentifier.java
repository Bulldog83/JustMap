package ru.bulldog.justmap.map.data.classic;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.util.PosUtil;

public class MultiworldIdentifier {
	public final BlockPos spawnPosition;
	public final Identifier dimensionType;

	public MultiworldIdentifier(BlockPos spawnPosition, Identifier dimensionType) {
		this.spawnPosition = spawnPosition;
		this.dimensionType = dimensionType;
	}

	public MultiworldIdentifier(BlockPos spawnPosition, World world) {
		this(spawnPosition, world.getRegistryKey().getValue());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MultiworldIdentifier that = (MultiworldIdentifier) o;

		if (!spawnPosition.equals(that.spawnPosition)) return false;
		return dimensionType.equals(that.dimensionType);
	}

	@Override
	public int hashCode() {
		int result = spawnPosition.hashCode();
		result = 31 * result + dimensionType.hashCode();
		return result;
	}

	public JsonObject toJson() {
		JsonObject jsonKey = new JsonObject();
		jsonKey.addProperty("dimension", dimensionType.toString());
		jsonKey.add("position", PosUtil.toJson(spawnPosition));
		return jsonKey;
	}

	public static MultiworldIdentifier fromJson(JsonObject object) {
		Identifier dimensionType = new Identifier(JsonHelper.getString(object, "dimension"));
		BlockPos spawnPosition = PosUtil.fromJson(JsonHelper.getObject(object, "position"));
		return new MultiworldIdentifier(spawnPosition, dimensionType);
	}
}
