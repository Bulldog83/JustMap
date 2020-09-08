package ru.bulldog.justmap.map.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class PlayerIcon extends MapIcon<PlayerIcon> {
	
	private PlayerEntity player;
	private int color = Colors.GREEN;
	
	public PlayerIcon(PlayerEntity player) {
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
		if (ClientSettings.showPlayerHeads) {
			MapPlayerManager.getPlayer(player).getIcon().draw(matrices, x, y, size, true);
		} else {
			int darken = ColorUtil.colorBrigtness(color, -3);
			RenderUtil.fill(x - 0.5, y - 0.5, size + 1, size + 1, darken);
			RenderUtil.fill(x, y, size, size, color);
		}
		this.drawPlayerName(matrices, x, y);
	}

	@Override
	public void draw(MatrixStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, int mapW, int mapH, float rotation) {
		int size = ClientSettings.entityIconSize;
		this.updatePos(mapX, mapY, mapW, mapH, size);
		if (!allowRender) return;
		if (ClientSettings.renderEntityModel) {
			EntityModelRenderer.renderModel(matrices, consumerProvider, player, iconPos.x, iconPos.y);
		} else if (ClientSettings.showPlayerHeads) {
			MapPlayerManager.getPlayer(player).getIcon().draw(matrices, iconPos.x, iconPos.y);
		} else {
			int darken = ColorUtil.colorBrigtness(color, -3);
			RenderUtil.fill(iconPos.x - 0.5, iconPos.y - 0.5, size + 1, size + 1, darken);
			RenderUtil.fill(iconPos.x, iconPos.y, size, size, color);
		}
		this.drawPlayerName(matrices, iconPos.x, iconPos.y);
	}
	
	private void drawPlayerName(MatrixStack matrices, double x, double y) {
		if (!ClientSettings.showPlayerNames) return;
		MinecraftClient minecraft = MinecraftClient.getInstance();
		Window window = minecraft.getWindow();
		double sf = window.getScaleFactor();
		float scale = (float) (1.0 / sf);
		matrices.push();
		if (sf > 1.0 && !minecraft.options.forceUnicodeFont) {
			matrices.scale(scale, scale, 1.0F);
			matrices.translate(x * (sf - 1), y * (sf - 1), 0.0);
		}
		RenderUtil.drawCenteredText(matrices, player.getName(), x, y + 12, Colors.WHITE);
		matrices.pop();
	}
}
