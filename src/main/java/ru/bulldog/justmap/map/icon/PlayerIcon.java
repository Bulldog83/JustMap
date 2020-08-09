package ru.bulldog.justmap.map.icon;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;

public class PlayerIcon extends MapIcon<PlayerIcon> {
	
	private PlayerEntity player;
	private int color = Colors.GREEN;
	
	public PlayerIcon(IMap map, PlayerEntity player) {
		super(map);
		this.player = player;
	}
	
	public double getX() {
		return this.player.getX();
	}
	
	public double getZ() {
		return this.player.getZ();
	}
	
	public void draw(MatrixStack matrices, int size) {
		double x = this.x - size / 2;
		double y = this.y - size / 2;
		if (ClientParams.showPlayerHeads) {
			MapPlayerManager.getPlayer(player).getIcon().draw(matrices, x, y, size, true);
		} else {
			int darken = ColorUtil.colorBrigtness(color, -3);
			RenderUtil.fill(x - 0.5, y - 0.5, size + 1, size + 1, darken);
			RenderUtil.fill(x, y, size, size, color);
		}
		this.drawPlayerName(x, y);
	}

	@Override
	public void draw(MatrixStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, double offX, double offY, float rotation) {
		int size = ClientParams.entityIconSize;
		this.updatePos(size, mapX, mapY, offX, offY, rotation);
		if (!allowRender) return;
		if (ClientParams.renderEntityModel) {
			EntityModelRenderer.renderModel(matrices, consumerProvider, player, iconPos.x, iconPos.y);
		} else if (ClientParams.showPlayerHeads) {
			MapPlayerManager.getPlayer(player).getIcon().draw(matrices, iconPos.x, iconPos.y);
		} else {
			int darken = ColorUtil.colorBrigtness(color, -3);
			RenderUtil.fill(iconPos.x - 0.5, iconPos.y - 0.5, size + 1, size + 1, darken);
			RenderUtil.fill(iconPos.x, iconPos.y, size, size, color);
		}
		this.drawPlayerName(pos.x, pos.y);
	}
	
	private void drawPlayerName(double x, double y) {
		if (!ClientParams.showPlayerNames) return;
		RenderUtil.drawCenteredText(player.getName(), x, y + 12, Colors.WHITE);
	}
}
