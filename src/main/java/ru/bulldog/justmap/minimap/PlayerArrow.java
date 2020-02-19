package ru.bulldog.justmap.minimap;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.Matrix3f;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.ImageUtil;

public class PlayerArrow extends Sprite {
	
	private final static SpriteAtlasTexture ATLAS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/player_arrow.png"));
	private final static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	private final static VertexFormat vertexFormat = new VertexFormat(ImmutableList.of(VertexFormats.POSITION_ELEMENT, VertexFormats.TEXTURE_ELEMENT, VertexFormats.NORMAL_ELEMENT, VertexFormats.PADDING_ELEMENT));
	
	private static PlayerArrow PLAYER_ARROW = new PlayerArrow(new Identifier(JustMap.MODID, "textures/icon/player_arrow.png"), 20, 20);
	
	private PlayerArrow(Identifier texture, int w, int h) {
		super(ATLAS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	}
	
	public static void draw(int x, int y, float rotation) {
		MatrixStack matrix = new MatrixStack();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
		builder.begin(7, vertexFormat);
		
		VertexConsumer vertexConsumer = PLAYER_ARROW.getTextureSpecificVertexConsumer(builder);
		
		textureManager.bindTexture(PLAYER_ARROW.getId());
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		matrix.push();
		matrix.translate(x, y, 0);
		matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotation));
		
		Matrix4f m4f = matrix.peek().getModel();
		Matrix3f m3f = matrix.peek().getNormal();
		
		render(m4f, m3f, vertexConsumer);
		tessellator.draw();
		
		matrix.pop();
	}
	
	private static void render(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer) {
		vertexConsumer.vertex(m4f, 5.0F, 5.0F, 0.0F).texture(0.0F, 0.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
		vertexConsumer.vertex(m4f, 5.0F, -5.0F, 0.0F).texture(0.0F, 1.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
		vertexConsumer.vertex(m4f, -5.0F, -5.0F, 0.0F).texture(1.0F, 1.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
		vertexConsumer.vertex(m4f, -5.0F, 5.0F, 0.0F).texture(1.0F, 0.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
	}
	
}
