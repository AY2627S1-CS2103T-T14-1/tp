package seedu.address.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Shared dumbbell mark for the header and window icons. */
final class FittixIcon {
    private static final Color LIME = Color.web("#d4f25a");

    private FittixIcon() {
    }

    static StackPane createNode() {
        Group dumbbell = new Group(
                plate(7.4, 10.7, 9.2, 2.6, 0.4),
                plate(4.4, 6.6, 3.4, 10.8, 1),
                plate(16.2, 6.6, 3.4, 10.8, 1),
                plate(1.8, 8.6, 2, 6.8, 0.8),
                plate(20.2, 8.6, 2, 6.8, 0.8));
        dumbbell.setRotate(-45);
        StackPane node = new StackPane(dumbbell);
        node.setMinSize(30, 30);
        node.setPrefSize(30, 30);
        node.setMaxSize(30, 30);
        return node;
    }

    static Image createImage() {
        StackPane node = createNode();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, 30, 30));
        return node.snapshot(parameters, null);
    }

    private static Rectangle plate(double x, double y, double width, double height, double radius) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setArcWidth(radius * 2);
        rectangle.setArcHeight(radius * 2);
        rectangle.setFill(LIME);
        return rectangle;
    }
}
