package ru.bulldog.justmap.client.map.skin;

import java.io.File;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.storage.StorageUtil;

public final class SkinLoader extends JsonFactory {

	private final static File SKINS_FOLDER = StorageUtil.skinsDir();
	private final static TextureManager textureManager = Minecraft.getInstance().getTextureManager();
	
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
	
	private static void loadSkin(File folder, File skinFile) throws Exception {
		JsonObject skinData = getJsonObject(skinFile);
		String name = GsonHelper.getAsString(skinData, "name");
		int width = GsonHelper.getAsInt(skinData, "width");
		int height = GsonHelper.getAsInt(skinData, "height");
		int border = GsonHelper.getAsInt(skinData, "border");
		MapSkin.SkinType shape = getSkinType(GsonHelper.getAsString(skinData, "shape", "universal"));
		boolean resizable = GsonHelper.getAsBoolean(skinData, "resizable", false);
		boolean repeating = GsonHelper.getAsBoolean(skinData, "repeating", false);
		String textureType = GsonHelper.getAsString(skinData, "texture_type");
		if (textureType.equals("source")) {	
			ResourceLocation texture = new ResourceLocation(GsonHelper.getAsString(skinData, "texture"));
			MapSkin.addUniversalSkin(name, texture, width, height, border);
		} else if (textureType.equals("image")) {
			String imageName = GsonHelper.getAsString(skinData, "image");
			File imageFile = new File(folder, imageName);
			NativeImage skinImage = ImageUtil.loadImage(imageFile, width, height);
			String prefix = String.format("%s_%s", JustMap.MOD_ID, imageName);
			ResourceLocation textureId = textureManager.register(prefix, new DynamicTexture(skinImage));
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
	
	private static MapSkin.SkinType getSkinType(String shape) throws JsonParseException {
		switch(shape.toLowerCase()) {
			case "round":
			case "circle":
				return MapSkin.SkinType.ROUND;
			case "universal":
				return MapSkin.SkinType.UNIVERSAL;
			case "square":
				return MapSkin.SkinType.SQUARE;
			default:
				throw new JsonParseException("Invalid skin shape: '" + shape + "'");
		}		
	}
}
