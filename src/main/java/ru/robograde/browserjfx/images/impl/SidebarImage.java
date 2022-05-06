package ru.robograde.browserjfx.images.impl;

import javafx.scene.Scene;
import ru.robograde.browserjfx.images.CustomImage;

public class SidebarImage extends CustomImage {
    public SidebarImage() {
        super("sidebar", "sideBar");
    }

    @Override
    public void relocate(Scene scene) {
        this.view.relocate(3, 53);
    }
}
