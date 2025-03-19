package common.ui;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


public class ToggleSwitch extends StackPane {
    private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);
    private final TranslateTransition translateAnimation = new TranslateTransition(Duration.seconds(0.25));

    public ToggleSwitch() {
        Rectangle background = new Rectangle(60, 30);
        background.setArcWidth(30); // arrondir les coins
        background.setArcHeight(30);
        background.getStyleClass().add("toggle-switch-background-disabled");

        Circle trigger = new Circle(12);
        trigger.getStyleClass().add("toggle-switch-trigger");

        getChildren().addAll(background, trigger);

        switchedOn.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // activer
                background.getStyleClass().add("toggle-switch-background-enabled");
                translateAnimation.setToX(15);
            } else {
                // désactiver
                background.getStyleClass().remove("toggle-switch-background-enabled");
                translateAnimation.setToX(-15);
            }
            translateAnimation.play();  // lancer l'animation
        });

        setOnMouseClicked(event -> switchedOn.set(!switchedOn.get()));  // détection des clics
        translateAnimation.setNode(trigger);

        // Initialiser la position du trigger de départ
        if (switchedOn.get()) {
            background.getStyleClass().add("toggle-switch-background-enabled");
            trigger.setTranslateX(15);
        } else {
            background.getStyleClass().remove("toggle-switch-background-enabled");
            trigger.setTranslateX(-15);
        }
    }

    public BooleanProperty selectedProperty() {
        return switchedOn;
    }

    public boolean isSelected() {
        return switchedOn.get();
    }

    public void setSelected(boolean selected) {
        this.switchedOn.set(selected);
    }
}
