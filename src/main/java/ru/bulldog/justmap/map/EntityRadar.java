package ru.bulldog.justmap.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.MathUtil;

public class EntityRadar {
	private final List<Player> players;
	private final List<Mob> creatures;
	private final List<MapIcon<?>> drawableIcons;
	
	public EntityRadar() {
		this.players = new LinkedList<>();
		this.creatures = new LinkedList<>();
		this.drawableIcons = new ArrayList<>();
	}
	
	public void addPlayer(Player player) {
		if (players.contains(player)) return;
		this.players.add(player);
	}
	
	public void addCreature(Mob creature) {
		if (creatures.contains(creature)) return;
		this.creatures.add(creature);
	}
	
	public List<MapIcon<?>> getDrawableIcons(double worldX, double worldZ, double screenX, double screenY, double scale, float delta) {
		this.drawableIcons.clear();
		this.players.forEach(player -> {
			double iconX = MathUtil.screenPos(DataUtil.doubleX(player, delta), worldX, screenX, scale);
			double iconY = MathUtil.screenPos(DataUtil.doubleZ(player, delta), worldZ, screenY, scale);
			PlayerIcon icon = new PlayerIcon(player);
			icon.setPosition(iconX, iconY, (int) icon.getY());
			this.drawableIcons.add(icon);
		});
		this.creatures.forEach(mob -> {
			double iconX = MathUtil.screenPos(DataUtil.doubleX(mob, delta), worldX, screenX, scale);
			double iconY = MathUtil.screenPos(DataUtil.doubleZ(mob, delta), worldZ, screenY, scale);
			EntityIcon icon = new EntityIcon(mob);
			icon.setPosition(iconX, iconY, (int) mob.getY());
			this.drawableIcons.add(icon);
		});
		return this.drawableIcons;
	}
	
	public void clear(BlockPos center, int radius) {
		if (players.size() > 0) {
			List<Player> playersToClear = new ArrayList<>();
			this.players.forEach(player -> {
				if (player.isRemoved()) {
					playersToClear.add(player);
				} else if (MathUtil.getDistance(center, player.blockPosition()) > radius) {
					playersToClear.add(player);
				}
			});
			playersToClear.forEach(players::remove);
		}
		if (creatures.size() > 0) {
			List<Mob> mobsToClear = new ArrayList<>();
			this.creatures.forEach(mob -> {
				boolean tooFar = MathUtil.getDistance(center, mob.blockPosition()) > radius ||
						         Math.abs(mob.getY() - center.getY()) > 24;
				if (mob.isDeadOrDying() || mob.isRemoved()) {
					mobsToClear.add(mob);
				} else if (tooFar) {
					mobsToClear.add(mob);
				}
			});
			mobsToClear.forEach(creatures::remove);
		}
	}
	
	public void clearAll() {
		this.players.clear();
		this.creatures.clear();
	}
}
