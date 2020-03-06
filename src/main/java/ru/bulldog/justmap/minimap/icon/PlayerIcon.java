package ru.bulldog.justmap.minimap.icon;

import net.minecraft.entity.player.PlayerEntity;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.EntityModelRenderer;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.MathUtil;

public class PlayerIcon extends MapIcon<PlayerIcon> {
	
	protected boolean self;
	protected PlayerEntity player;
	
	public PlayerIcon(Minimap map, PlayerEntity player, boolean self) {
		super(map);
		this.self = self;
		this.player = player;
	}

	@Override
	public void draw(int mapX, int mapY, float rotation) {
		
		double drawX = mapX + x;
		double drawY = mapY + y;
		
		int mapSize = JustMapClient.MAP.getMapSize();
		if (ClientParams.rotateMap) {
			double centerX = mapX + mapSize / 2;
			double centerY = mapY + mapSize / 2;
			
			rotation = MathUtil.correctAngle(rotation) + 180;
			
			double angle = Math.toRadians(-rotation);
			
			double posX = (int) (centerX + (drawX - centerX) * Math.cos(angle) - (drawY - centerY) * Math.sin(angle));
			double posY = (int) (centerY + (drawY - centerY) * Math.cos(angle) + (drawX - centerX) * Math.sin(angle));
			
			drawX = posX;
			drawY = posY;
		}
		
		int size = ClientParams.entityIconSize;
		if (ClientParams.showPlayerHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(player, drawX, drawY);
			} else {
				PlayerHeadIcon.getIcon(player).draw(drawX, drawY);
			}
		} else {
			DrawHelper.fill(drawX, drawY, drawX + size, drawY + size, Colors.GREEN);
		}
			
		if (ClientParams.showPlayerNames) {
			DrawHelper.drawBoundedString(client.textRenderer, player.getName().getString(), (int) drawX + size / 2, (int) drawY - size / 2 - 10, 0, client.getWindow().getScaledWidth(), Colors.WHITE);
		}
	}
}
