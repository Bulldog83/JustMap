package ru.bulldog.justmap.client.screen;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.util.math.MathHelper;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.widget.TitledButtonWidget;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerRange;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.Predicates;
import ru.bulldog.justmap.util.colors.Colors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WaypointEditor extends MapScreen {
	
	private static final Text TITLE = new TranslatableText(JustMap.MODID + ".gui.screen.waypoints_editor");

	private final Waypoint waypoint;
	
	private int colorIndex;
	private int iconIndex;
	private int showRange;
	
	private int spacing = 2;
	private int padding = 10;
	private int rowH = 20;
	
	private TitledButtonWidget<TextFieldWidget> nameField;
	private CheckboxWidget isHidden;
	private CheckboxWidget isTrackable;
	private CheckboxWidget isRenderable;
	private ButtonWidget prevColorButton, nextColorButton;
	private ButtonWidget prevIconButton, nextIconButton;
	private TextFieldWidget xField, yField, zField;
	private ButtonWidget saveButton, cancelButton;
	private Consumer<Waypoint> onSaveCallback;
	
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
		
		int screenW = center > 480 ? center : width > 480 ? 480 : width;
	
		this.x = center - screenW / 2;
		this.y = 60;
		
		int row = rowH + spacing;
		
		int ex = x + padding;
		int ey = y;
		int ew = screenW - padding * 2;
		this.nameField = new TitledButtonWidget<>(font, new TextFieldWidget(font, 0, 0, ew - 30, 12, "Name"), ex, ey, ew, rowH, "", lang("name"));
		this.nameField.changeFocus(true);
		this.nameField.widget.setMaxLength(48);
		this.nameField.widget.setText(waypoint.name);
		
		this.children.add(nameField);
		
		Predicate<String> validNumber = (s) -> Predicates.or(s, Predicates.isInteger, Predicates.isEmpty, "-"::equals);
		
		ew = 60;
		int px = center - (ew * 3) / 2;
		
		ey += row;
		
		this.xField = new TextFieldWidget(font, px, ey, ew, rowH, "");
		this.xField.setTextPredicate(validNumber);
		this.xField.setMaxLength(7);
		this.xField.setText(waypoint.pos.getX() + "");
		
		this.yField = new TextFieldWidget(font, px + ew, ey, ew, rowH, "");
		this.yField.setTextPredicate(validNumber);
		this.yField.setMaxLength(7);
		this.yField.setText(waypoint.pos.getY() + "");
		
		this.zField = new TextFieldWidget(font, px + 2 * ew, ey, ew, rowH, "");
		this.zField.setTextPredicate(validNumber);
		this.zField.setMaxLength(7);
		this.zField.setText(waypoint.pos.getZ() + "");
		
		this.children.add(xField);
		this.children.add(yField);
		this.children.add(zField);
		
		ey += row;
		
		ew = 20;
		this.prevColorButton = new ButtonWidget(ex, ey, ew, rowH, "<", (b) -> cycleColor(-1));
		this.children.add(prevColorButton);
		
		this.nextColorButton = new ButtonWidget(x + screenW - ew - padding, ey, ew, rowH, ">", (b) -> cycleColor(1));
		this.children.add(nextColorButton);
		
		ey += row;
		
		this.prevIconButton = new ButtonWidget(ex, ey, ew, rowH, "<", (b) -> cycleIcon(-1));
		this.children.add(prevIconButton);
		
		this.nextIconButton = new ButtonWidget(x + screenW - ew - padding, ey, ew, rowH, ">", (b) -> cycleIcon(1));
		this.children.add(nextIconButton);
		
		ey += row * 1.5;
		
		int sliderW = (int) (screenW * 0.6);
		int elemX = width / 2 - sliderW / 2;
		
		this.isHidden = new CheckboxWidget(elemX, ey, ew, rowH, lang("wp_hidden"), waypoint.hidden);
		this.isTrackable = new CheckboxWidget(elemX + 100, ey, ew, rowH, lang("wp_tracking"), waypoint.tracking);
		this.isRenderable = new CheckboxWidget(elemX + 200, ey, ew, rowH, lang("wp_render"), waypoint.render);
		this.children.add(isHidden);
		this.children.add(isTrackable);
		this.children.add(isRenderable);

		ey += row * 1.25;

		IntegerRange maxRangeConfig = JustMapClient.getConfig().getEntry("max_render_dist");
		final int SHOW_RANGE_MAX = maxRangeConfig.maxValue();
		this.showRange = waypoint.showRange;
		this.children.add(new SliderWidget(elemX, ey, sliderW, rowH, (double) this.showRange / SHOW_RANGE_MAX) {
			{
				this.updateMessage();
			}

			@Override
			protected void updateMessage() {
				this.setMessage(lang("wp_render_dist") + WaypointEditor.this.showRange);
			}

			@Override
			protected void applyValue() {
				WaypointEditor.this.showRange = MathHelper.floor(MathHelper.clampedLerp(0, SHOW_RANGE_MAX, this.value));
			}
		});
		
		ew = 60;
		ey = height - (rowH / 2 + 16);
		this.saveButton = new ButtonWidget(center - ew - 2, ey, ew, rowH, lang("save"), (b) -> { save(); onClose(); });
		this.children.add(saveButton);
		
		this.cancelButton = new ButtonWidget(center + 2, ey, ew, rowH, lang("cancel"), (b) -> onClose());
		this.children.add(cancelButton);
		
		this.setInitialFocus(nameField);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		String dimensionName = info == null ? lang("unknown") : I18n.translate(info.getFirst());
		this.drawCenteredString(font, dimensionName, center, 15, Colors.WHITE);
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
		this.waypoint.name = nameField.widget.getText();		
		int color = Waypoint.WAYPOINT_COLORS[colorIndex];
		if(Waypoint.getIcon(iconIndex) != null) {
			this.waypoint.setIcon(Waypoint.getIcon(iconIndex), color);
		} else {
			this.waypoint.color = color;
		}
		this.waypoint.hidden = isHidden.isChecked();
		this.waypoint.tracking = isTrackable.isChecked();
		this.waypoint.render = isRenderable.isChecked();
		
		int xPos = (xField.getText().isEmpty() || xField.getText().equals("-")) ? 0 : Integer.parseInt(xField.getText());
		int yPos = (yField.getText().isEmpty() || yField.getText().equals("-")) ? 0 : Integer.parseInt(yField.getText());
		int zPos = (zField.getText().isEmpty() || zField.getText().equals("-")) ? 0 : Integer.parseInt(zField.getText());
		
		this.waypoint.pos = new BlockPos(xPos, yPos, zPos);

		this.waypoint.showRange = this.showRange;
		
		if (onSaveCallback != null) {
			this.onSaveCallback.accept(waypoint);
		}
		
		WaypointKeeper.getInstance().saveWaypoints();
	}
	
	@Override
	public void onClose() {
		this.minecraft.openScreen(parent);
	}
	
	@Override
	public void renderForeground() {
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
		this.borderedRect(x, y, w, h, color, 2, 0xFFCCCCCC);
		icon.draw(ix, iy);
	}
	
	@Override
	public void tick() {}
	
	private void rect(int x, int y, int w, int h, int color) {
		fill(x, y, x + w, y + h, color);
	}
	
	private void borderedRect(int x, int y, int w, int h, int color, int border, int borderColor) {
		int hb = border >> 1;
		this.rect(x, y, w, h, borderColor);
		this.rect(x + hb, y + hb, w - border, h - border, color);
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
