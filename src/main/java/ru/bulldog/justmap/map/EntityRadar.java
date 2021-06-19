package ru.bulldog.justmap.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.MathUtil;

public class EntityRadar {
	private final Set<Player> players;
	private final Set<Mob> creatures;
	private final List<MapIcon<?>> drawableIcons;
	
	public EntityRadar() {
		this.players = Sets.newHashSet();
		this.creatures = Sets.newHashSet();
		this.drawableIcons = Lists.newArrayList();
	}
	
	public void addPlayer(Player player) {
		if (players.contains(player)) return;
		players.add(player);
	}
	
	public void addCreature(Mob creature) {
		if (creatures.contains(creature)) return;
		creatures.add(creature);
	}
	
	public List<MapIcon<?>> getDrawableIcons(double worldX, double worldZ, double screenX, double screenY, double scale, float delta) {
		drawableIcons.clear();
		players.forEach(player -> {
			double iconX = MathUtil.screenPos(DataUtil.doubleX(player, delta), worldX, screenX, scale);
			double iconY = MathUtil.screenPos(DataUtil.doubleZ(player, delta), worldZ, screenY, scale);
			PlayerIcon icon = new PlayerIcon(player);
			icon.setPosition(iconX, iconY, (int) icon.getY());
			drawableIcons.add(icon);
		});
		creatures.forEach(mob -> {
			double iconX = MathUtil.screenPos(DataUtil.doubleX(mob, delta), worldX, screenX, scale);
			double iconY = MathUtil.screenPos(DataUtil.doubleZ(mob, delta), worldZ, screenY, scale);
			EntityIcon icon = new EntityIcon(mob);
			icon.setPosition(iconX, iconY, (int) mob.getY());
			drawableIcons.add(icon);
		});
		return drawableIcons;
	}
	
	public void clear(BlockPos center, int radius) {
		if (players.size() > 0) {
			List<Player> playersToClear = new ArrayList<>();
			players.forEach(player -> {
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
			creatures.forEach(mob -> {
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
		players.clear();
		creatures.clear();
	}
}
