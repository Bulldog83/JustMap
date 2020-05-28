package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
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
	public void draw(int mapX, int mapY, double offX, double offY, float rotation) {
		int size = ClientParams.entityIconSize;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		pos.x -= size / 2 + offX;
		pos.y -= size / 2 + offY;
		
		if (pos.x < mapX + size || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY + size || pos.y > (mapY + map.getHeight()) - size) return;
		
		if (ClientParams.showPlayerHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(player, pos.x, pos.y);
			} else {
				MatrixStack matrix = new MatrixStack();			
				matrix.push();
				if (ClientParams.rotateMap) {
					double moveX = pos.x + size / 2;
					double moveY = pos.y + size / 2;
					matrix.translate(moveX, moveY, 0.0);
					matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotation + 180));
					matrix.translate(-moveX, -moveY, 0.0);
				}
				PlayerHeadIcon.getIcon(player).draw(matrix, pos.x, pos.y);
				matrix.pop();
			}
		} else {
			DrawHelper.fill(pos.x, pos.y, pos.x, pos.y, Colors.GREEN);
		}
			
		if (ClientParams.showPlayerNames) {
			DrawHelper.drawBoundedString(player.getName().getString(), (int) pos.x, (int) pos.y + 12, 0, client.getWindow().getScaledWidth(), Colors.WHITE);
		}
	}
}
