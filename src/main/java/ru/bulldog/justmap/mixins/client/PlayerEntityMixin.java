package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.Colors;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("RETURN"), method = "dropInventory")
	public void onDropInventory(CallbackInfo ci) {
		Waypoint waypoint = new Waypoint();
		waypoint.dimension = this.dimension.getRawId();
		waypoint.name = "Player Death";
		waypoint.pos = this.getBlockPos();
		waypoint.setIcon(Waypoint.getIcon(Waypoint.Icons.CROSS), Colors.RED);
		
		JustMap.LOGGER.logInfo("Create Death waypoint at " + this.getBlockPos());
		
		WaypointKeeper.getInstance().addNew(waypoint);
		WaypointKeeper.getInstance().saveWaypoints();
	}
}
