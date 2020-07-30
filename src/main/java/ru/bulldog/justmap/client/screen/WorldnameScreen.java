package ru.bulldog.justmap.client.screen;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.LangUtil;
import ru.bulldog.justmap.util.RenderUtil;

public class WorldnameScreen extends Screen {

	private final static Text TITLE = LangUtil.getText("gui", "screen.worldname");
	private final static Identifier FRAME_TEXTURE = new Identifier(JustMap.MODID, "textures/screen_background.png");
	
	private final Screen parent;
	private TextFieldWidget nameField;
	private ButtonWidget saveButton;
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
		this.frameWidth = frameWidth > 320 ? frameWidth : width > 320 ? 320 : width;
		int btnY = 0;
		if (frameWidth == width) {
			this.frameHeight = height;
			btnY = height - 25;
			this.x = 0;
			this.y = 0;
		} else {
			this.frameHeight = (frameWidth * 10) / 16;
			btnY = (y + frameHeight) - 25;
			this.x = center - frameWidth / 2;
			this.y = height / 2 - frameHeight / 2;
		}
		Text defaultText = new LiteralText(WorldManager.currentWorldName());
		this.nameField = new TextFieldWidget(textRenderer, x + 20, y + 40, frameWidth - 40, 20, defaultText);
		this.saveButton = new ButtonWidget(center - 20, btnY, 40, 20, LangUtil.getText("gui", "save"), (b) -> { this.save(); });
		this.buttons.add(saveButton);
		this.children.add(nameField);
	}
	
	private void save() {
		String worldName = saveButton.getMessage().getString();
		worldName = worldName.trim().replaceAll(" +", " ");
		if (worldName == "") {
			worldName = "Default";
		}
		System.out.println(worldName);
		WorldManager.setCurrentWorldName(worldName);
		this.success = true;
		this.onClose();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.drawCenteredString(matrices, textRenderer, LangUtil.getString("gui", "worldname_title"), center, y + 20, Colors.WHITE);
		for (Element child : children) {
			if (child instanceof Drawable) {
				((Drawable) child).render(matrices, mouseX, mouseY, delta);
			}
		}
		this.buttons.forEach(element -> element.render(matrices, mouseX, mouseY, delta));
	}
	
	@Override
	public void renderBackground(MatrixStack matrices) {
		super.renderBackground(matrices);
		RenderUtil.bindTexture(FRAME_TEXTURE);
		RenderUtil.startDraw();
		RenderUtil.addQuad(x, y, frameWidth, frameHeight);
		RenderUtil.endDraw();
	}

	@Override
	public void onClose() {
		if (!success) {
			WorldManager.setCurrentWorldName("Default");
		}
		this.client.openScreen(parent);
	}
}
