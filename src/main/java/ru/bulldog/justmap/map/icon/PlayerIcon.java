package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;

public class PlayerIcon extends MapIcon<PlayerIcon> {
	
	private PlayerEntity player;
	
	public PlayerIcon(IMap map, PlayerEntity player, boolean self) {
		super(map);
		this.player = player;
	}

	@Override
	public void draw(int mapX, int mapY, double offX, double offY, float rotation) {
		int size = ClientParams.entityIconSize;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		pos.x -= size / 2 + offX;
		pos.y -= size / 2 + offY;
		
		if (pos.x < mapX + size || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY + size || pos.y > (mapY + map.getHeight()) - size) return;
		
		if (ClientParams.rotateMap) {
			this.rotatePos(pos, map.getWidth(), map.getHeight(), mapX, mapY, rotation);
		}
		
		if (ClientParams.showPlayerHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(player, pos.x, pos.y);
			} else {
				MatrixStack matrix = new MatrixStack();			
				PlayerHeadIcon.getIcon(player).draw(matrix, pos.x, pos.y);
			}
		} else {
			DrawHelper.fill(pos.x, pos.y, pos.x, pos.y, Colors.GREEN);
		}
			
		if (ClientParams.showPlayerNames) {
			DrawHelper.drawBoundedString(player.getName().getString(), (int) pos.x, (int) pos.y + 12, 0, client.getWindow().getScaledWidth(), Colors.WHITE);
		}
	}
}
