package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class PresentationModeImage extends CustomImage {
    public PresentationModeImage() {
        super("presentation", "presentationMode");
    }

    @Override
    public void relocate(Scene scene) {
        if (scene.getWidth() >= 665)
            this.view.relocate(scene.getWidth() - 93, 53);
        else
            this.view.setVisible(false);
    }
}
