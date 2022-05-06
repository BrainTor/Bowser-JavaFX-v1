package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class FindImage extends CustomImage {
    public FindImage() {
        super("find", "find");
    }

    @Override
    public void relocate(Scene scene) {
        if (scene.getWidth() >= 665)
            this.view.relocate(63, 53);
        else
            this.view.relocate(33, 53);
    }
}
