package ru.bulldog.justmap.map.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
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
	public void draw(MatrixStack matrixStack, int mapX, int mapY, double offX, double offY, float rotation) {
		int size = ClientParams.entityIconSize;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		pos.x -= size / 2 + offX;
		pos.y -= size / 2 + offY;
		
		if (pos.x < mapX + size || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY + size || pos.y > (mapY + map.getHeight()) - size) return;
		
		MatrixStack matrix = new MatrixStack();
		if (ClientParams.showPlayerHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(player, pos.x, pos.y);
			} else {
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
			MinecraftClient client = MinecraftClient.getInstance();
			Window window = client.getWindow();
			double sf = window.getScaleFactor();
			float scale = (float) (1.0 / sf);
			matrix.push();
			if (sf > 1.0 && !client.options.forceUnicodeFont) {
				matrix.scale(scale, scale, 1.0F);
				matrix.translate(pos.x * (sf - 1), pos.y * (sf - 1), 0.0);
			}
			DrawHelper.drawCenteredText(matrix, player.getName(), pos.x, pos.y + 12, Colors.WHITE);
			matrix.pop();
		}
	}
}
