package ru.bulldog.justmap.map.minimap.skin;

import java.io.File;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.minimap.skin.MapSkin.SkinType;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.storage.StorageUtil;

public final class SkinLoader extends JsonFactory {

	private final static File SKINS_FOLDER = StorageUtil.skinsDir();
	private final static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

	private SkinLoader() {}

	public static void loadSkins() {
		File[] skinFolders = SKINS_FOLDER.listFiles();
		for (File folder : skinFolders) {
			if (folder.isFile()) continue;
			File skinFile = new File(folder, "skin_data.json");
			if (!skinFile.exists()) continue;
			try {
				loadSkin(folder, skinFile);
			} catch (Exception ex) {
				JustMap.LOGGER.warning("Can't load skin: " + skinFile.getPath());
				JustMap.LOGGER.warning(ex.getLocalizedMessage());
			}
		}
	}

	private static void loadSkin(File folder, File skinFile) {
		JsonObject skinData = getJsonObject(skinFile);
		String name = JsonHelper.getString(skinData, "name");
		int width = JsonHelper.getInt(skinData, "width");
		int height = JsonHelper.getInt(skinData, "height");
		int border = JsonHelper.getInt(skinData, "border");
		SkinType shape = getSkinType(JsonHelper.getString(skinData, "shape", "universal"));
		boolean resizable = JsonHelper.getBoolean(skinData, "resizable", false);
		boolean repeating = JsonHelper.getBoolean(skinData, "repeating", false);
		String textureType = JsonHelper.getString(skinData, "texture_type");
		if (textureType.equals("source")) {
			Identifier texture = new Identifier(JsonHelper.getString(skinData, "texture"));
			MapSkin.addUniversalSkin(name, texture, width, height, border);
		} else if (textureType.equals("image")) {
			String imageName = JsonHelper.getString(skinData, "image");
			File imageFile = new File(folder, imageName);
			NativeImage skinImage = ImageUtil.loadImage(imageFile, width, height);
			String prefix = String.format("%s_%s", JustMap.MODID, imageName);
			Identifier textureId = textureManager.registerDynamicTexture(prefix, new NativeImageBackedTexture(skinImage));
			switch (shape) {
				case ROUND:
					MapSkin.addRoundSkin(name, textureId, skinImage, width, height, border);
					break;
				case SQUARE:
					MapSkin.addSquareSkin(name, textureId, skinImage, width, height, border, resizable, repeating);
					break;
				case UNIVERSAL:
					MapSkin.addUniversalSkin(imageName, textureId, skinImage, width, height, border);
					break;
			}
		} else {
			throw new JsonParseException("Invalid skin texture type: '" + textureType + "'");
		}
	}

	private static SkinType getSkinType(String shape) throws JsonParseException {
		switch(shape.toLowerCase()) {
			case "round":
			case "circle":
				return SkinType.ROUND;
			case "universal":
				return SkinType.UNIVERSAL;
			case "square":
				return SkinType.SQUARE;
			default:
				throw new JsonParseException("Invalid skin shape: '" + shape + "'");
		}
	}
}
