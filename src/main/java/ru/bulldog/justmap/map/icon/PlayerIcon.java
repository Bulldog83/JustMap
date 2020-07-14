package ru.bulldog.justmap.map.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.math.Line.Point;

public class PlayerIcon extends MapIcon<PlayerIcon> {
	
	private PlayerEntity player;	
	private int color = Colors.GREEN;
	
	public PlayerIcon(IMap map, PlayerEntity player, boolean self) {
		super(map);
		this.player = player;
	}

	@Override
	public void draw(MatrixStack matrixStack, int mapX, int mapY, double offX, double offY, float rotation) {
		int size = ClientParams.entityIconSize;
		
		Point pos = new Point(mapX + x, mapY + y);
		
		if (ClientParams.rotateMap) {
			this.rotatePos(pos, map.getWidth(), map.getHeight(), mapX, mapY, rotation);
		}
		
		pos.x -= size / 2 + offX;
		pos.y -= size / 2 + offY;
		
		if (pos.x < mapX + size || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY + size || pos.y > (mapY + map.getHeight()) - size) return;
		
		MatrixStack matrix = new MatrixStack();
		 if (ClientParams.renderEntityModel) {
			EntityModelRenderer.renderModel(player, pos.x, pos.y);
		} else if (ClientParams.showPlayerHeads) {
			MapPlayerManager.getPlayer(player).getIcon().draw(matrix, pos.x, pos.y);
		} else {
			int darken = ColorUtil.colorBrigtness(this.color, -3);
			RenderUtil.fill(pos.x - 0.5, pos.y - 0.5, size + 1, size + 1, darken);
			RenderUtil.fill(pos.x, pos.y, size, size, this.color);
		}
			
		if (ClientParams.showPlayerNames) {
			MinecraftClient client = MinecraftClient.getInstance();
			Window window = client.getWindow();
			double sf = window.getScaleFactor();
			float scale = (float) (1.0 / sf);
			matrix.push();
			if (sf > 1.0 && !client.options.forceUnicodeFont) {
				matrix.scale(scale, scale, 1.0F);
				matrix.translate(pos.x * (sf - 1), pos.y * (sf - 1), 0.0);
			}
			RenderUtil.drawCenteredText(matrix, player.getName(), pos.x, pos.y + 12, Colors.WHITE);
			matrix.pop();
		}
	}
}
