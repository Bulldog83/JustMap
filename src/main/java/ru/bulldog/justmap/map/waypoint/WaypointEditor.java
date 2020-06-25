package ru.bulldog.justmap.map.waypoint;

import ru.bulldog.justmap.client.MapScreen;
import ru.bulldog.justmap.client.widget.TitledWidget;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.Predicates;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WaypointEditor extends MapScreen {
	
	private static final LiteralText title = new LiteralText("Edit Waypoint");

	private final Waypoint waypoint;
	
	private int colorIndex;
	private int iconIndex;
	
	private int spacing = 2;
	private int padding = 10;
	private int rowH = 20;
	
	private TitledWidget<TextFieldWidget> nameField;
	private CheckboxWidget isHidden;
	private CheckboxWidget isTrackable;
	private CheckboxWidget isRenderable;
	private ButtonWidget prevColorButton, nextColorButton;
	private ButtonWidget prevIconButton, nextIconButton;
	private TextFieldWidget xField, yField, zField;
	private ButtonWidget saveButton, cancelButton;
	private Consumer<Waypoint> onSaveCallback;
	
	public WaypointEditor(Waypoint waypoint, Screen parent, Consumer<Waypoint> onSaveCallback) {
		super(title, parent);
		
		this.waypoint = waypoint;
		colorIndex = getColorIndex(waypoint.color);
		this.iconIndex = getIconIndex(waypoint.getIcon());
		this.onSaveCallback = onSaveCallback;
	}
	
	@Override
	public void init() {
		super.init();
		
		center = width / 2;
		
		int screenW = Math.max(400, center);
	
		x = center - screenW / 2;
		y = 60;
		
		int row = rowH + spacing;
		
		int ex = x + padding;
		int ey = y;
		int ew = screenW - padding * 2;
		nameField = new TitledWidget<>(textRenderer, new TextFieldWidget(textRenderer, 0, 0, ew - 30, 12, new LiteralText("Name")), ex, ey, ew, rowH, "", lang("name").asString());
		nameField.changeFocus(true);
		nameField.widget.setMaxLength(48);
		nameField.widget.setText(waypoint.name);
		
		children.add(nameField);
		
		Predicate<String> validNumber = (s) -> Predicates.or(s, Predicates.isInteger, Predicates.isEmpty);
		
		ew = 60;
		int px = center - (ew * 3) / 2;
		
		ey += row;
		
		xField = new TextFieldWidget(textRenderer, px, ey, ew, rowH, new LiteralText(""));
		xField.setTextPredicate(validNumber);
		xField.setMaxLength(7);
		xField.setText(waypoint.pos.getX() + "");
		
		yField = new TextFieldWidget(textRenderer, px + ew, ey, ew, rowH, new LiteralText(""));
		yField.setTextPredicate(validNumber);
		yField.setMaxLength(7);
		yField.setText(waypoint.pos.getY() + "");
		
		zField = new TextFieldWidget(textRenderer, px + 2 * ew, ey, ew, rowH, new LiteralText(""));
		zField.setTextPredicate(validNumber);
		zField.setMaxLength(7);
		zField.setText(waypoint.pos.getZ() + "");
		
		children.add(xField);
		children.add(yField);
		children.add(zField);
		
		ey += row;
		
		ew = 20;
		prevColorButton = new ButtonWidget(ex, ey, ew, rowH, new LiteralText("<"), (b) -> cycleColor(-1));
		children.add(prevColorButton);
		
		nextColorButton = new ButtonWidget(x + screenW - ew - padding, ey, ew, rowH, new LiteralText(">"), (b) -> cycleColor(1));
		children.add(nextColorButton);
		
		ey += row;
		
		prevIconButton = new ButtonWidget(ex, ey, ew, rowH, new LiteralText("<"), (b) -> cycleIcon(-1));
		children.add(prevIconButton);
		
		nextIconButton = new ButtonWidget(x + screenW - ew - padding, ey, ew, rowH, new LiteralText(">"), (b) -> cycleIcon(1));
		children.add(nextIconButton);
		
		ey += row * 1.5;
		
		isHidden = new CheckboxWidget(ex, ey, ew, rowH, lang("wp_hidden"), waypoint.hidden);
		isTrackable = new CheckboxWidget(ex + 90, ey, ew, rowH, lang("wp_tracking"), waypoint.tracking);
		isRenderable = new CheckboxWidget(ex + 180, ey, ew, rowH, lang("wp_render"), waypoint.render);
		children.add(isHidden);
		children.add(isTrackable);
		children.add(isRenderable);		
		
		ew = 60;
		ey = height - (rowH / 2 + 16);
		saveButton = new ButtonWidget(center - ew - 2, ey, ew, rowH, lang("save"), (b) -> { save(); onClose(); });
		children.add(saveButton);
		
		cancelButton = new ButtonWidget(center + 2, ey, ew, rowH, lang("cancel"), (b) -> onClose());
		children.add(cancelButton);
		
		setInitialFocus(nameField);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
		super.render(matrixStack, mouseX, mouseY, delta);
		String dimensionName = info == null ? lang("unknown").asString() : I18n.translate(info.getFirst());
		drawCenteredString(matrixStack, textRenderer, dimensionName, center, 15, Colors.WHITE);
	}
	
	private void cycleColor(int i) {
		colorIndex += i;
		if (colorIndex < 0) {
			colorIndex = Waypoint.WAYPOINT_COLORS.length - 1;
		} else if (colorIndex >= Waypoint.WAYPOINT_COLORS.length) {
			colorIndex = 0;
		}
	}	
	
	private void cycleIcon(int i) {
		iconIndex += i;
		if (iconIndex < 0) {
			iconIndex = Waypoint.amountIcons();
		} else if (iconIndex >= Waypoint.amountIcons()) {
			iconIndex = 0;
		}
	}
	
	private void save() {
		waypoint.name = nameField.widget.getText();		
		int color = Waypoint.WAYPOINT_COLORS[colorIndex];
		if(Waypoint.getIcon(iconIndex) != null) {
			waypoint.setIcon(Waypoint.getIcon(iconIndex), color);
		} else {
			waypoint.color = color;
		}
		waypoint.hidden = isHidden.isChecked();
		waypoint.tracking = isTrackable.isChecked();
		waypoint.render = isRenderable.isChecked();
		
		int xPos = xField.getText().isEmpty() ? 0 : Integer.parseInt(xField.getText());
		int yPos = yField.getText().isEmpty() ? 0 : Integer.parseInt(yField.getText());
		int zPos = zField.getText().isEmpty() ? 0 : Integer.parseInt(zField.getText());
		
		waypoint.pos = new BlockPos(xPos, yPos, zPos);
		
		if (onSaveCallback != null) {
			onSaveCallback.accept(waypoint);
		}
		
		WaypointKeeper.getInstance().saveWaypoints();
	}
	
	@Override
	public void onClose() {
		client.openScreen(parent);
	}
	
	@Override
	public void renderForeground(MatrixStack matrixStack) {
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
		borderedRect(matrixStack, x, y, w, h, color, 2, 0xFFCCCCCC);
		icon.draw(ix, iy);
	}
	
	@Override
	public void tick() {}
	
	private void rect(MatrixStack matrixStack, int x, int y, int w, int h, int color) {
		fill(matrixStack, x, y, x + w, y + h, color);
	}
	
	private void borderedRect(MatrixStack matrixStack, int x, int y, int w, int h, int color, int border, int borderColor) {
		int hb = border >> 1;
		rect(matrixStack, x, y, w, h, borderColor);
		rect(matrixStack, x + hb, y + hb, w - border, h - border, color);
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
			save();
			onClose();
			return true;
		}		
		return super.keyPressed(int_1, int_2, int_3);
	}
}
