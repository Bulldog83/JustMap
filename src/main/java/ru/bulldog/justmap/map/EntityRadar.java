package ru.bulldog.justmap.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.util.math.MathUtil;

public class EntityRadar {
	private final List<PlayerEntity> players;
	private final List<MobEntity> creatures;
	
	public EntityRadar() {
		this.players = new LinkedList<>();
		this.creatures = new LinkedList<>();
	}
	
	public void addPlayer(PlayerEntity player) {
		if (players.contains(player)) return;
		this.players.add(player);
	}
	
	public void addCreature(MobEntity creature) {
		if (creatures.contains(creature)) return;
		this.creatures.add(creature);
	}
	
	public List<MapIcon<?>> getDrawedIcons(IMap map, double worldX, double worldZ, double screenX, double screenZ, double scale) {
		List<MapIcon<?>> icons = new ArrayList<>();
		this.players.forEach(player -> {
			double iconX = MathUtil.screenPos(player.getX(), worldX, screenX, scale);
			double iconY = MathUtil.screenPos(player.getZ(), worldZ, screenZ, scale);
			PlayerIcon icon = new PlayerIcon(map, player);
			icon.setPosition(iconX, iconY);
			icons.add(icon);
		});
		this.creatures.forEach(mob -> {
			double iconX = MathUtil.screenPos(mob.getX(), worldX, screenX, scale);
			double iconY = MathUtil.screenPos(mob.getZ(), worldZ, screenZ, scale);
			boolean hostile = mob instanceof HostileEntity;
			EntityIcon icon = new EntityIcon(map, mob, hostile);
			icon.setPosition(iconX, iconY);
			icons.add(icon);
		});
		return icons;
	}
	
	public void clear(BlockPos center, int radius) {
		if (players.size() > 0) {
			List<PlayerEntity> playersToClear = new ArrayList<>();
			this.players.forEach(player -> {
				if (player.removed) {
					playersToClear.add(player);
				} else if (MathUtil.getDistance(center, player.getBlockPos()) > radius) {
					playersToClear.add(player);
				}
			});
			playersToClear.forEach(player -> this.players.remove(player));
		}
		if (creatures.size() > 0) {
			List<MobEntity> mobsToClear = new ArrayList<>();
			this.creatures.forEach(mob -> {
				if (mob.isDead() || mob.removed) {
					mobsToClear.add(mob);
				} else if (MathUtil.getDistance(center, mob.getBlockPos()) > radius) {
					mobsToClear.add(mob);
				}
			});
			mobsToClear.forEach(mob -> this.creatures.remove(mob));
		}
	}
	
	public void clearAll() {
		this.players.clear();
		this.creatures.clear();
	}
}
