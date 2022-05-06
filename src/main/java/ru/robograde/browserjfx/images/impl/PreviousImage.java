package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class PreviousImage extends CustomImage {
    public PreviousImage() {
        super("previous", "previous");
    }

    @Override
    public void relocate(Scene scene) {
        if (scene.getWidth() >= 665)
            this.view.relocate(94, 53);
        else
            this.view.setVisible(false);
    }
}
