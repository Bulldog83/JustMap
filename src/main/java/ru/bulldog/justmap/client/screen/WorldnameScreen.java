package ru.bulldog.justmap.client.screen;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.util.LangUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class WorldnameScreen extends Screen {

	private final static Component TITLE = LangUtil.getText("gui", "screen.worldname");
	private final static ResourceLocation FRAME_TEXTURE = new ResourceLocation(JustMap.MODID, "textures/screen_background.png");
	
	private final Screen parent;
	private EditBox nameField;
	private boolean success = false;
	private int center;
	private int frameWidth;
	private int frameHeight;
	private int x, y;
	
	public WorldnameScreen(Screen parent) {
		super(TITLE);
		this.parent = parent;
	}
	
	@Override
	public void init() {
		this.center = width / 2;
		this.frameWidth = width / 3;
		this.frameWidth = frameWidth > 320 ? frameWidth : Math.min(width, 320);
		int btnY;
		if (frameWidth == width) {
			this.frameHeight = height;
			btnY = height - 40;
			this.x = 0;
			this.y = 0;
		} else {
			this.frameHeight = (frameWidth * 10) / 16;
			this.x = center - frameWidth / 2;
			this.y = height / 2 - frameHeight / 2;
			btnY = (y + frameHeight) - 40;
		}
		Component defaultText = new TextComponent("Default");
		this.nameField = new EditBox(font, x + 20, y + 50, frameWidth - 40, 20, defaultText);
		this.setFocused(this.nameField);
		this.nameField.setFocus(true);
		this.addWidget(new Button(center - 30, btnY, 80, 20, LangUtil.getText("gui", "save"), this::onPressSave));
		this.addWidget(nameField);
	}
	
	private void onPressSave(Button button) {
		String worldName = nameField.getValue();
		worldName = worldName.trim().replaceAll(" +", " ");
		if (worldName.equals("")) {
			worldName = "Default";
		}
		WorldManager.setCurrentWorldName(worldName);
		this.success = true;
		onClose();
	}
	
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		drawCenteredString(matrices, font, LangUtil.getString("gui", "worldname_title"), center, y + 25, Colors.WHITE);
		for (GuiEventListener child : children()) {
			if (child instanceof Widget) {
				((Widget) child).render(matrices, mouseX, mouseY, delta);
			}
		}
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void renderBackground(PoseStack matrices) {
		super.renderBackground(matrices);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderUtil.bindTexture(FRAME_TEXTURE);
		RenderUtil.startDraw();
		RenderUtil.addQuad(x, y, frameWidth, frameHeight);
		RenderUtil.endDraw();
		RenderSystem.disableBlend();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			this.onPressSave(null);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		if (!success) {
			WorldManager.setCurrentWorldName("Default");
		}
		minecraft.setScreen(parent);
	}
}
