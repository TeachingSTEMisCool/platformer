package com.whhs.platformer;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;

public class MainLoadingScene extends LoadingScene{
	
	public MainLoadingScene() {
		var bg = new Rectangle(getAppWidth(), getAppHeight(), Color.AZURE);
		
		var text = getUIFactoryService().newText("Loading Level", Color.BLACK, 46.0);
		centerText(text, getAppWidth() / 2, getAppHeight() / 3 + 25);
		
		var hbox = new HBox(5);
		
		for (int i = 0; i < 3; i++) {
			var textDot = getUIFactoryService().newText(".", Color.BLACK, 46.0);
			
			hbox.getChildren().add(textDot);
			
			animationBuilder(this)
					.autoReverse(true)
					.delay(Duration.seconds(i * 0.5))
					.repeatInfinitely()
					.fadeIn(textDot)
					.buildAndPlay();
		}
		
		hbox.setTranslateX(getAppWidth() / 2 - 20);
		hbox.setTranslateY(getAppHeight() / 2);
		
		var t = texture("player.png").subTexture(new Rectangle2D(0, 0, 32, 42));
	}
}