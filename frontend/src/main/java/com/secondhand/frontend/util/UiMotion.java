package com.secondhand.frontend.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuButton;
import javafx.util.Duration;

/**
 * Small, reusable motion system for JavaFX.
 * JavaFX CSS has no browser-like transition property, so these timelines give
 * buttons and cards a natural hover/press response without touching each FXML.
 */
public final class UiMotion {

    private static final String INSTALLED_KEY = "secondhand.motion.installed";
    private static final Duration HOVER_DURATION = Duration.millis(150);
    private static final Duration PRESS_DURATION = Duration.millis(85);

    private UiMotion() {
    }

    public static void install(Parent root) {
        installNode(root);
    }

    public static void playPageEntrance(Parent root) {
        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(230), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        fade.play();
    }

    private static void installNode(Node node) {
        if (node instanceof MenuButton) {
            installMenuButtonMotion(node);
        } else if (node instanceof ButtonBase) {
            installButtonMotion(node);
        } else if (hasInteractiveCardStyle(node)) {
            installCardMotion(node);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                installNode(child);
            }
        }
    }

    /** Can also be called for cards created dynamically in a ListCell. */
    public static void installCardMotion(Node node) {
        if (alreadyInstalled(node)) {
            return;
        }
        node.setOnMouseEntered(event -> animate(node, 1.008, -1.5, HOVER_DURATION));
        node.setOnMouseExited(event -> animate(node, 1.0, 0, HOVER_DURATION));
    }

    /**
     * Do not set onMousePressed/onMouseReleased on MenuButton. Those handlers
     * are used by JavaFX itself to open the dropdown popup.
     */
    private static void installMenuButtonMotion(Node menuButton) {
        if (alreadyInstalled(menuButton)) {
            return;
        }
        menuButton.setOnMouseEntered(event -> animate(menuButton, 1.015, -0.5, HOVER_DURATION));
        menuButton.setOnMouseExited(event -> animate(menuButton, 1.0, 0, HOVER_DURATION));
    }

    private static void installButtonMotion(Node button) {
        if (alreadyInstalled(button)) {
            return;
        }
        button.setOnMouseEntered(event -> animate(button, 1.025, -1.0, HOVER_DURATION));
        button.setOnMouseExited(event -> animate(button, 1.0, 0, HOVER_DURATION));
        button.setOnMousePressed(event -> animate(button, 0.975, 0, PRESS_DURATION));
        button.setOnMouseReleased(event -> animate(button, 1.025, -1.0, PRESS_DURATION));
    }

    private static boolean alreadyInstalled(Node node) {
        if (Boolean.TRUE.equals(node.getProperties().get(INSTALLED_KEY))) {
            return true;
        }
        node.getProperties().put(INSTALLED_KEY, true);
        return false;
    }

    private static boolean hasInteractiveCardStyle(Node node) {
        return node.getStyleClass().contains("surface-card")
                || node.getStyleClass().contains("metric-card")
                || node.getStyleClass().contains("ad-card")
                || node.getStyleClass().contains("row-card");
    }

    private static void animate(Node node, double scale, double translateY, Duration duration) {
        Timeline animation = new Timeline(
                new KeyFrame(duration,
                        new KeyValue(node.scaleXProperty(), scale, Interpolator.EASE_BOTH),
                        new KeyValue(node.scaleYProperty(), scale, Interpolator.EASE_BOTH),
                        new KeyValue(node.translateYProperty(), translateY, Interpolator.EASE_BOTH)
                )
        );
        animation.play();
    }
}
