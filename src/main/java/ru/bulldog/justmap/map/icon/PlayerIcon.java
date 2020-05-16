package ru.bulldog.justmap.map.icon;

import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;

public class PlayerIcon extends MapIcon<PlayerIcon> {
	
	protected boolean self;
	protected PlayerEntity player;
	
	public PlayerIcon(IMap map, PlayerEntity player, boolean self) {
		super(map);
		this.self = self;
		this.player = player;
	}

	@Override
	public void draw(int mapX, int mapY, float rotation) {
		int size = ClientParams.entityIconSize;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		if (ClientParams.rotateMap) {
			this.rotatePos(pos, map.getWidth(), map.getHeight(), mapX, mapY, rotation);
		}
		
		pos.x -= size / 2;
		pos.y -= size / 2;
		
		if (pos.x < mapX + size || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY + size || pos.y > (mapY + map.getHeight()) - size) return;
		
		if (ClientParams.showPlayerHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(player, pos.x, pos.y);
			} else {
				PlayerHeadIcon.getIcon(player).draw(pos.x, pos.y);
			}
		} else {
			DrawHelper.fill(pos.x, pos.y, pos.x, pos.y, Colors.GREEN);
		}
			
		if (ClientParams.showPlayerNames) {
			DrawHelper.drawBoundedString(client.textRenderer, player.getName().getString(), (int) pos.x, (int) pos.y + 12, 0, client.getWindow().getScaledWidth(), Colors.WHITE);
		}
	}
}
