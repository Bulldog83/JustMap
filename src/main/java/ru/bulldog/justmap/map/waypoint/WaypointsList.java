package ru.bulldog.justmap.map.waypoint;

import com.mojang.datafixers.util.Pair;

import ru.bulldog.justmap.client.MapScreen;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.RandomUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;

public class WaypointsList extends MapScreen {
	private static class Entry implements Element {
		private MinecraftClient minecraft;
	
		private int x, y, width, height;
		
		private ButtonWidget editButton;
		private ButtonWidget deleteButton;
		private ButtonWidget tpButton;
		private Waypoint waypoint;
	
		public Entry(WaypointsList wayPointListEditor, int x, int y, int width, int height, Waypoint waypoint) {
			this.width = width;
			this.height = height + 2;
			this.waypoint = waypoint;
			
			this.minecraft = MinecraftClient.getInstance();
			
			this.editButton = new ButtonWidget(0, 0, 40, height, wayPointListEditor.lang("edit"), (b) -> wayPointListEditor.edit(waypoint));
			this.deleteButton = new ButtonWidget(0, 0, 40, height, wayPointListEditor.lang("delete"), (b) -> wayPointListEditor.delete(waypoint));
			this.tpButton = new ButtonWidget(0, 0, 40, height, wayPointListEditor.lang("teleport"), (b) -> wayPointListEditor.teleport(waypoint));
			
			this.setPosition(x, y);
		}
	
		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
			
			this.rightAlign(deleteButton, x + width - 2);
			this.rightAlign(editButton, deleteButton);
			this.rightAlign(tpButton, editButton);
			
			editButton.y = y + 1;
			tpButton.y = y + 1;
			deleteButton.y = y + 1;
		}		
		
		public void render(int mouseX, int mouseY, float delta) {
			TextRenderer font = minecraft.textRenderer;
			
			boolean hover = isMouseOver(mouseX, mouseY);
			int bgColor = hover ? 0x88AAAAAA : 0x88333333;
			fill(x, y, x + width, y + height, bgColor);
			
			int iconSize = height - 2;
			Icon icon = waypoint.getIcon();
			if (icon != null) {
				icon.draw(x, y + 1, iconSize, iconSize);
			} else {
				DrawHelper.drawDiamond(x, y + 1, iconSize, iconSize, waypoint.color);
			}
			
			int stringY = y + 7;			
			int nameX = x + iconSize + 2;

			DrawHelper.DRAWER.drawString(font, waypoint.name, nameX, stringY, Colors.WHITE);
			
			int posX = tpButton.x - 5;
			DrawHelper.DRAWER.drawRightAlignedString(font, waypoint.pos.toShortString(), posX, stringY, Colors.WHITE);
			
			editButton.render(mouseX, mouseY, delta);
			tpButton.render(mouseX, mouseY, delta);
			deleteButton.render(mouseX, mouseY, delta);
		}
	
		@Override
		public boolean mouseClicked(double double_1, double double_2, int int_1) {
			return editButton.mouseClicked(double_1, double_2, int_1) ||
				   deleteButton.mouseClicked(double_1, double_2, int_1) ||
				   tpButton.mouseClicked(double_1, double_2, int_1);
		}
	
		@Override
		public boolean mouseReleased(double double_1, double double_2, int int_1) {
			return editButton.mouseReleased(double_1, double_2, int_1) ||
				   deleteButton.mouseReleased(double_1, double_2, int_1) ||
				   tpButton.mouseReleased(double_1, double_2, int_1);
		}
	
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
	
		private void rightAlign(ButtonWidget toAlign, ButtonWidget from) {
			toAlign.x = from.x - toAlign.getWidth() - 1;
		}
	
		private void rightAlign(ButtonWidget toAlign, int right) {
			toAlign.x = right - toAlign.getWidth();
		}
	}
	
	private static final Text title = new LiteralText("Waypoints");
	
	private WaypointKeeper keeper = WaypointKeeper.getInstance();
	private int currentDim = 0;
	private int currentDimIndex = 0;
	private List<Integer> dimensions;
	private List<Waypoint> wayPoints;
	private List<Entry> entries = new ArrayList<>();

	private int scrollAmount = 0;
	private int maxScroll = 0;
	private int screenWidth;
	
	private ButtonWidget prevDimensionButton, nextDimensionButton;
	private ButtonWidget addButton, closeButton;
	
	public WaypointsList(Screen parent) {
		super(title, parent);
		if (minecraft == null) {
			minecraft = MinecraftClient.getInstance();
		}
		
		dimensions = keeper.getDimensions();
		
		int overworld = DimensionType.OVERWORLD.getRawId();
		int nether = DimensionType.THE_NETHER.getRawId();
		int theEnd = DimensionType.THE_END.getRawId();
		if (!dimensions.contains(overworld)) {
			dimensions.add(overworld);
		}		
		if (!dimensions.contains(nether)) {
			dimensions.add(nether);
		}		
		if (!dimensions.contains(theEnd)) {
			dimensions.add(theEnd);
		}		
		
		currentDim = minecraft.player.dimension.getRawId();
		currentDimIndex = getDimIndex(currentDim);
	}

	@Override
	protected void init() {
		this.center = width / 2;		
		this.screenWidth = Math.max(480, center);		
		this.x = center - screenWidth / 2;		
		this.prevDimensionButton = new ButtonWidget(x + 10, 10, 20, 20, "<", (b) -> cycleDimension(-1));
		this.nextDimensionButton = new ButtonWidget(x + screenWidth - 30, 10, 20, 20, ">", (b) -> cycleDimension(1));		
		this.addButton = new ButtonWidget(center - 62, height - 26, 60, 20, lang("create"), (b) -> add());
		this.closeButton = new ButtonWidget(center + 2, height - 26, 60, 20, lang("close"), (b) -> onClose());
	
		reset();
	}
	
	private void createEntries() {
		entries.clear();
	
		int y = 40;
		for (Waypoint wp : wayPoints) {
			Entry entry = new Entry(this, x + 10, scrollAmount + y, screenWidth - 20, 20, wp);
			entries.add(entry);
			
			y += entry.height;
		}
	}
	
	private void updateEntries() {
		int y = 40;
		for (Entry entry : entries) {
			entry.setPosition(x + 10, scrollAmount + y);
			y += entry.height;
		}
	}
	
	
	private void cycleDimension(int i) {
		currentDimIndex += i;
		if (currentDimIndex >= dimensions.size()) {
			currentDimIndex = 0;
		}
		else if (currentDimIndex < 0) {
			currentDimIndex = dimensions.size() - 1;
		}
		
		currentDim = dimensions.get(currentDimIndex);
		reset();
	}
	
	private int getDimIndex(int dim) {
		return dimensions.indexOf(dim);
	}
	
	public void reset() {
		info = getDimensionInfo(currentDim);
		
		wayPoints = keeper.getWaypoints(currentDim, false);
		createEntries();
		maxScroll = wayPoints.size() * 20;
		
		children.clear();
		children.addAll(entries);
		children.add(addButton);
		children.add(closeButton);
		children.add(prevDimensionButton);
		children.add(nextDimensionButton);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		
		entries.forEach(e -> e.render(mouseX, mouseY, delta));
		
		String dimensionName = info == null ? lang("unknown") : I18n.translate(info.getFirst());
		drawCenteredString(font, dimensionName, center, 15, Colors.WHITE);
		
		drawScrollBar();
	}
	
	private Pair<String, Identifier> getDimensionInfo(int dim) {
		DimensionType type = DimensionType.byRawId(dim);
		String key = "unknown";
		if (type != null) {
			 key = type.toString();
		}
		
		return DIMENSION_INFO.getOrDefault(key, null);
	}
	
	private void drawScrollBar() {}
	
	private void edit(Waypoint waypoint) {
		minecraft.openScreen(new WaypointEditor(waypoint, this, null));
	}
	
	private void add() {
		Waypoint waypoint = new Waypoint();
		waypoint.dimension = currentDim;
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = minecraft.player.getBlockPos();
		waypoint.name = "Waypoint";
		
		minecraft.openScreen(new WaypointEditor(waypoint, this, keeper::addNew));
	}
	
	private void delete(Waypoint waypoint) {
		keeper.remove(waypoint);
		keeper.saveWaypoints();
		reset();
	}
	
	public void teleport(Waypoint waypoint) {
		if (minecraft.player.dimension.getRawId() != currentDim) return;
		int y = waypoint.pos.getY() > 0 ? waypoint.pos.getY() : (this.minecraft.player.dimension != DimensionType.THE_NETHER ? 128 : 64);
		this.minecraft.player.sendChatMessage("/tp " + this.minecraft.player.getName().asString() + " " + waypoint.pos.getX() + " " + y + " " + waypoint.pos.getZ());
		if (!this.minecraft.isIntegratedServerRunning()) {
			this.minecraft.player.sendChatMessage("/tppos " + waypoint.pos.getX() + " " + y + " " + waypoint.pos.getZ());
		}
		this.onClose();
	}
	
	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		scrollAmount = MathUtil.clamp(scrollAmount + (int) (double_3 * 12), -maxScroll + 80, 0);
		updateEntries();
		
		return true;
	}
}
