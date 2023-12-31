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

public class App extends GameApplication {
	
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
	
	@Override
	protected void initInput() {
		getInput().addAction(new UserAction("left") {
			@Override
			protected void onAction() {
				player.getComponent(PlayerComponent.class).left();
			}
			
			@Override
			protected void onActionEnd() {
				player.getComponent(PlayerComponent.class).stop();
			}
		}, KeyCode.A);
		
		getInput().addAction(new UserAction("Right") {
			@Override
			protected void onAction() {
				player.getComponent(PlayerComponent.class).right();
			}
			
			@Override
			protected void onActionEnd() {
				player.getComponent(PlayerComponent.class).stop();
			}
		}, KeyCode.D);
		
		getInput().addAction(new UserAction("Jump") {
			@Override
			public void onActionBegin() {
				player.getComponent(PlayerComponent.class).jump();
			}
		}, KeyCode.W);
		
		getInput().addAction(new UserAction("Use") {
			@Override
			protected void onActionBegin() {
				getGameWorld().getEntitiesByType(EntityType.BUTTON)
						.stream()
						.filter(btn -> btn.hasComponent(CollidableComponent.class) && player.isColliding(btn))
						.forEach(btn -> {
							btn.removeComponent(CollidableComponent.class);
							
							Entity keyEntity = btn.getObject("keyEntity");
							keyEntity.setProperty("activated", true);
							
							KeyView view = (KeyView) keyEntity.getViewComponent().getChildren().get(0);
							view.setKeyColor(Color.RED);
							
							makeExitDoor();
						});
			}
		}, KeyCode.E);
	}
	
	@Override
	protected void initGameVars(Map<String, Object> vars) {
		vars.put("level", STARTING_LEVEL);
		vars.put("levelTime", 0.0);
	}
	
	@Override
	protected void initGame() {
		getGameWorld().addEntityFactory(new PlatformerFactory());
		
		player = null;
		nextLevel();
		
		// player must be spawned after call to nextLevel, otherwise player gets removed
		// before the update tick _actually_ adds the player to game world
		player = spawn("player", 50, 50);
		
		set("player", player);
		
		spawn("background");
		
		
		Viewport viewport = getGameScene().getViewport();
		viewport.setBounds(-1500, 0, 250 * 70, getAppHeight());
		viewport.bindToEntity(player, getAppWidth() / 2, getAppHeight() / 2);
		viewport.setLazy(true);
	}
	
	@Override
	protected void initPhysics() {
		getPhysicsWorld().setGravity(0, 760);
		getPhysicsWorld().addCollisionHandler(new PlayerButtonHandler());
		onCollisionOneTimeOnly(EntityType.PLAYER, EntityType.EXIT_SIGN, (player, sign) -> {
			var texture = texture("exit_sign.png").brighter();
			texture.setTranslateX(sign.getX() + 9);
			texture.setTranslateY(sign.getY() + 13);
			
			var gameView = new GameView(texture, 150);
			
			getGameScene().addGameView(gameView);
			runOnce(() -> getGameScene().removeGameView(gameView), Duration.seconds(1.6));
		});
		
		onCollisionOneTimeOnly(EntityType.PLAYER, EntityType.EXIT_TRIGGER, (player, trigger) -> {
			makeExitDoor();
		});
		
		onCollisionOneTimeOnly(EntityType.PLAYER, EntityType.DOOR_BOT, (player, door) -> {
			levelEndScene.get().onLevelFinish();
			
			//the above runs in its own scene, so fade will wait until user exits that scene
			getGameScene().getViewport().fade(() -> {
				nextLevel();
			});
		});
		
		onCollisionBegin(EntityType.PLAYER, EntityType.KEY_PROMPT, (player, prompt) -> {
			String key = prompt.getString("key");
			var entity = getGameWorld().create("keyCode", new SpawnData(prompt.getX(), prompt.getY()).put("key", key));
			spawnWithScale(entity, Duration.seconds(1), Interpolators.ELASTIC.EASE_OUT());
			
			runOnce(() -> {
				despawnWithScale(entity, Duration.seconds(1), Interpolators.ELASTIC.EASE_IN());
			}, Duration.seconds(2.5));
		});
	}
	
	private void makeExitDoor() {
		var doorTop = getGameWorld().getSingleton(EntityType.DOOR_TOP);
		var doorBot = getGameWorld().getSingleton(EntityType.DOOR_BOT);
		
		doorBot.getComponent(CollidableComponent.class).setValue(true);
		
		doorTop.setOpacity(1);
		doorBot.setOpacity(1);
	}
	
	@Override
	protected void onUpdate(double tpf) {
		inc("levelTime", tpf);
		
		if (player.getY() > getAppHeight()) {
			setLevel(geti("level"));
		}
	}
	
	private void nextLevel() {
		if (geti("level") == MAX_LEVEL) {
			showMessage("You finished the demo!");
			return;
		}
		
		inc("level", +1);
		
		setLevel(geti("level"));
	}
	
	private void setLevel(int levelNum) {
		if (player != null) {
			player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(50, 50));
			player.setZIndex(Integer.MAX_VALUE);
		}
		
		set("levelTime", 0.0);
		
		Level level = setLevelFromMap("tmx/level" + levelNum + ".tmx");
		
		var shortestTime = level.getProperties().getDouble("star1time");
		
		var levelTimeData = new LevelEndScene.LevelTimeData(shortestTime * 2.4, shortestTime * 1.3, shortestTime);
		
		set("levelTimeData", levelTimeData);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
