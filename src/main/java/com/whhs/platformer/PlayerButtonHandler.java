package com.whhs.platformer;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;


public class PlayerButtonHandler extends CollisionHandler {
	
	public PlayerButtonHandler() {
		super(EntityType.PLAYER, EntityType.BUTTON);
	}
	
	@Override
	protected void onCollisionBegin(Entity player, Entity btn) {
		Entity keyEntity = btn.getObject("keyEntity");
		
		if (!keyEntity.isActive()) {
			keyEntity.setProperty("activated", false);
			getGameWorld().addEntity(keyEntity);
		}
		
		keyEntity.setOpacity(1);
	}
	
	@Override
	protected void onCollisionEnd(Entity player, Entity btn) {
		Entity keyEntity = btn.getObject("keyEntity");
		
		if (!keyEntity.getBoolean("activated")) {
			keyEntity.setOpacity(0);
		}
	}
}
