package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class ViewBookmarkImage extends CustomImage {
    public ViewBookmarkImage() {
        super("bookmark", "viewBookmark");
    }

    @Override
    public void relocate(Scene scene) {
        if (scene.getWidth() >= 665)
            this.view.relocate(scene.getWidth() - 65, 53);
        else
            this.view.setVisible(false);
    }
}
