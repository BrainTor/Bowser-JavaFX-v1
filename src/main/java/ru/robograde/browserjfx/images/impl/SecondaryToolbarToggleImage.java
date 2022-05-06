package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class SecondaryToolbarToggleImage extends CustomImage {
    public SecondaryToolbarToggleImage() {
        super("secondaryToolbar", "secondaryToolbarToggle");
    }

    @Override
    public void relocate(Scene scene) {
        this.view.relocate(scene.getWidth() - 28, 53);
    }
}
