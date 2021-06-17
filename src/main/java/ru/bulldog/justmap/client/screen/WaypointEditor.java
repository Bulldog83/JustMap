package ru.bulldog.justmap.client.screen;

import net.minecraft.client.gui.components.events.GuiEventListener;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.widget.TitledButtonWidget;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerRange;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.Predicates;
import ru.bulldog.justmap.util.colors.Colors;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

@SuppressWarnings("ConstantConditions")
public class WaypointEditor extends MapScreen {
	
	private static final Component TITLE = new TranslatableComponent(JustMap.MODID + ".gui.screen.waypoints_editor");

	private final Waypoint waypoint;
	
	private int colorIndex;
	private int iconIndex;
	private int showRange;

	private final int rowH = 20;

	private final Consumer<Waypoint> onSaveCallback;
	private TitledButtonWidget<EditBox> nameField;
	private Checkbox isHidden;
	private Checkbox isTrackable;
	private Checkbox isRenderable;
	private Button prevColorButton, nextColorButton;
	private EditBox xField, yField, zField;
	
	public WaypointEditor(Waypoint waypoint, Screen parent, Consumer<Waypoint> onSaveCallback) {
		super(TITLE, parent);
		
		this.waypoint = waypoint;
		colorIndex = getColorIndex(waypoint.color);
		this.iconIndex = getIconIndex(waypoint.getIcon());
		this.onSaveCallback = onSaveCallback;
	}
	
	@Override
	public void init() {
		super.init();
		
		this.center = width / 2;
		
		int screenW = center > 480 ? center : Math.min(width, 480);
	
		this.x = center - screenW / 2;
		this.y = 60;

		int spacing = 2;
		int row = rowH + spacing;

		int padding = 10;
		int ex = x + padding;
		int ey = y;
		int ew = screenW - padding * 2;
		this.nameField = new TitledButtonWidget<>(font, new EditBox(font, 0, 0, ew - 30, 12, new TextComponent("Name")), ex, ey, ew, rowH, "", lang("name").getContents());
		this.nameField.changeFocus(true);
		this.nameField.widget.setMaxLength(48);
		this.nameField.widget.setValue(waypoint.name);

		@SuppressWarnings("unchecked")
		List<GuiEventListener> children = (List<GuiEventListener>) children();
		children.add(nameField);
		
		Predicate<String> validNumber = (s) -> Predicates.or(s, Predicates.isInteger, Predicates.isEmpty, "-"::equals);
		
		ew = 60;
		int px = center - (ew * 3) / 2;
		
		ey += row;
		
		this.xField = new EditBox(font, px, ey, ew, rowH, new TextComponent(""));
		this.xField.setFilter(validNumber);
		this.xField.setMaxLength(7);
		this.xField.setValue(waypoint.pos.getX() + "");
		
		this.yField = new EditBox(font, px + ew, ey, ew, rowH, new TextComponent(""));
		this.yField.setFilter(validNumber);
		this.yField.setMaxLength(7);
		this.yField.setValue(waypoint.pos.getY() + "");
		
		this.zField = new EditBox(font, px + 2 * ew, ey, ew, rowH, new TextComponent(""));
		this.zField.setFilter(validNumber);
		this.zField.setMaxLength(7);
		this.zField.setValue(waypoint.pos.getZ() + "");
		
		children.add(xField);
		children.add(yField);
		children.add(zField);
		
		ey += row;
		
		ew = 20;
		this.prevColorButton = new Button(ex, ey, ew, rowH, new TextComponent("<"), (b) -> cycleColor(-1));
		children.add(prevColorButton);
		
		this.nextColorButton = new Button(x + screenW - ew - padding, ey, ew, rowH, new TextComponent(">"), (b) -> cycleColor(1));
		children.add(nextColorButton);
		
		ey += row;

		Button prevIconButton = new Button(ex, ey, ew, rowH, new TextComponent("<"), (b) -> cycleIcon(-1));
		children.add(prevIconButton);

		Button nextIconButton = new Button(x + screenW - ew - padding, ey, ew, rowH, new TextComponent(">"), (b) -> cycleIcon(1));
		children.add(nextIconButton);
		
		ey += row * 1.5;
		
		int sliderW = (int) (screenW * 0.6);
		int elemX = width / 2 - sliderW / 2;
		
		this.isHidden = new Checkbox(elemX, ey, ew, rowH, lang("wp_hidden"), waypoint.hidden);
		this.isTrackable = new Checkbox(elemX + 100, ey, ew, rowH, lang("wp_tracking"), waypoint.tracking);
		this.isRenderable = new Checkbox(elemX + 200, ey, ew, rowH, lang("wp_render"), waypoint.render);
		children.add(isHidden);
		children.add(isTrackable);
		children.add(isRenderable);

		ey += row * 1.25;

		IntegerRange maxRangeConfig = JustMapClient.getConfig().getEntry("max_render_dist");
		final int SHOW_RANGE_MAX = maxRangeConfig.maxValue();
		this.showRange = waypoint.showRange;
		children.add(new AbstractSliderButton(elemX, ey, sliderW, rowH, TextComponent.EMPTY, (double) this.showRange / SHOW_RANGE_MAX) {
			{
				this.updateMessage();
			}

			@Override
			protected void updateMessage() {
				this.setMessage(new TextComponent(lang("wp_render_dist").getString() + WaypointEditor.this.showRange));
			}

			@Override
			protected void applyValue() {
				WaypointEditor.this.showRange = Mth.floor(Mth.clampedLerp(0, SHOW_RANGE_MAX, this.value));
			}
		});
		
		ew = 60;
		ey = height - (rowH / 2 + 16);
		Button saveButton = new Button(center - ew - 2, ey, ew, rowH, lang("save"), (b) -> {
			save();
			onClose();
		});
		children.add(saveButton);

		Button cancelButton = new Button(center + 2, ey, ew, rowH, lang("cancel"), (b) -> onClose());
		children.add(cancelButton);
		
		setInitialFocus(nameField);
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float delta) {
		super.render(matrixStack, mouseX, mouseY, delta);
		String dimensionName = info == null ? lang("unknown").getContents() : I18n.get(info.getFirst());
		drawCenteredString(matrixStack, font, dimensionName, center, 15, Colors.WHITE);
	}
	
	private void cycleColor(int i) {
		this.colorIndex += i;
		if (colorIndex < 0) {
			this.colorIndex = Waypoint.WAYPOINT_COLORS.length - 1;
		} else if (colorIndex >= Waypoint.WAYPOINT_COLORS.length) {
			this.colorIndex = 0;
		}
	}	
	
	private void cycleIcon(int i) {
		this.iconIndex += i;
		if (iconIndex < 0) {
			this.iconIndex = Waypoint.amountIcons();
		} else if (iconIndex >= Waypoint.amountIcons()) {
			this.iconIndex = 0;
		}
	}
	
	private void save() {
		this.waypoint.name = nameField.widget.getValue();		
		int color = Waypoint.WAYPOINT_COLORS[colorIndex];
		if(Waypoint.getIcon(iconIndex) != null) {
			this.waypoint.setIcon(Waypoint.getIcon(iconIndex), color);
		} else {
			this.waypoint.color = color;
		}
		this.waypoint.hidden = isHidden.selected();
		this.waypoint.tracking = isTrackable.selected();
		this.waypoint.render = isRenderable.selected();
		
		int xPos = (xField.getValue().isEmpty() || xField.getValue().equals("-")) ? 0 : Integer.parseInt(xField.getValue());
		int yPos = (yField.getValue().isEmpty() || yField.getValue().equals("-")) ? 0 : Integer.parseInt(yField.getValue());
		int zPos = (zField.getValue().isEmpty() || zField.getValue().equals("-")) ? 0 : Integer.parseInt(zField.getValue());
		
		this.waypoint.pos = new BlockPos(xPos, yPos, zPos);

		this.waypoint.showRange = this.showRange;
		
		if (onSaveCallback != null) {
			this.onSaveCallback.accept(waypoint);
		}
		
		WaypointKeeper.getInstance().saveWaypoints();
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}
	
	@Override
	public void renderForeground(PoseStack matrixStack) {
		int x = prevColorButton.x + prevColorButton.getWidth() + 2;
		int y = prevColorButton.y + 3;
		int w = nextColorButton.x - x - 2;
		int h = 12;
		
		int col = Waypoint.WAYPOINT_COLORS[colorIndex];
		
		Icon icon;
		if (iconIndex > 0) {
			icon = Waypoint.getIcon(iconIndex);			
		} else {
			icon = Waypoint.getColoredIcon(col);
		}
		int ix = center - icon.getWidth() / 2;
		int iy = y + rowH + (rowH / 2 - icon.getHeight() / 2);
		int color = iconIndex > 0 ? icon.color : col;
		this.borderedRect(matrixStack, x, y, w, h, color);
		icon.draw(ix, iy);
	}
	
	@Override
	public void tick() {}
	
	private void rect(PoseStack matrixStack, int x, int y, int w, int h, int color) {
		fill(matrixStack, x, y, x + w, y + h, color);
	}
	
	private void borderedRect(PoseStack matrixStack, int x, int y, int w, int h, int color) {
		int hb = 2 >> 1;
		this.rect(matrixStack, x, y, w, h, -3355444);
		this.rect(matrixStack, x + hb, y + hb, w - 2, h - 2, color);
	}
	
	private int getColorIndex(int color) {
		for (int i = 0; i < Waypoint.WAYPOINT_COLORS.length; i++) {
			if (Waypoint.WAYPOINT_COLORS[i] == color) {
				return i;
			}
		}		
		return 0;
	}
	
	private int getIconIndex(Icon icon) {
		if (icon == null) return 0;
		return icon.key;
	}
	
	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (int_1 == GLFW.GLFW_KEY_ENTER) {
			this.save();
			this.onClose();
			return true;
		}		
		return super.keyPressed(int_1, int_2, int_3);
	}
}
