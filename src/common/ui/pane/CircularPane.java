package common.ui.pane;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Pane;

public class CircularPane extends Pane {

    private int radius = 50;

    @Override
    protected void layoutChildren() {
        final double increment = 360.0 / getChildren().size();
        double angle = (increment / 4) * (getChildren().size() % 2);
        for (javafx.scene.Node node : getChildren()) {
            double x = radius * Math.cos(Math.toRadians(angle)) + getWidth() / 2;
            double y = radius * Math.sin(Math.toRadians(angle)) + getHeight() / 2;
            layoutInArea(node,
                    x - node.getBoundsInLocal().getWidth() / 2,
                    y - node.getBoundsInLocal().getHeight() / 2,
                    getWidth(),
                    getHeight(),
                    0,
                    HPos.LEFT,
                    VPos.TOP);
            angle += increment;
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
