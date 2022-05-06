package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class NextImage extends CustomImage {
    public NextImage() {
        super("next", "next");
    }

    @Override
    public void relocate(Scene scene) {
        if (scene.getWidth() >= 665)
            this.view.relocate(124, 53);
        else
            this.view.setVisible(false);
    }
}
