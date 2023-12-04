package com.whhs.platformer;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.GameView;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class PlatformerApp extends GameApplication {
	
	private static final int MAX_LEVEL = 5;
	private static final int STARTING_LEVEL = 0;
	
	private LazyValue<LevelEndScene> levelEndScene = new LazyValue<>(() -> new LevelEndScene());
	private Entity player;
	
	@Override
	protected void initSettings(GameSettings settings) {
		settings.setWidth(1280);
		settings.setHeight(720);
		settings.setSceneFactory(new SceneFactory() {
			@Override
			public LoadingScene newLoadingScene() {
				return new MainLoadingScene();
			}
		});
	}
}
