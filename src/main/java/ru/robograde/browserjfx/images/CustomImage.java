package ru.robograde.browserjfx.images;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.robograde.browserjfx.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public abstract class CustomImage {
    public String name;
    public String fxmlId;
    public ImageView view;

    public CustomImage(String name, String fxmlId) {
        this.name = name;
        this.fxmlId = fxmlId;
    }

    public File getFile() throws URISyntaxException, FileNotFoundException {
        return Utils.getFileFromResource("images/" + this.name + ".png");
    }

    public void configureImage(Scene scene) throws URISyntaxException, FileNotFoundException {
        this.view = (ImageView) scene.lookup("#" + this.fxmlId);
        Image image = new Image(this.getFile().toURI().toString());
        view.setImage(image);
        this.relocate(scene);
    }

    public abstract void relocate(Scene scene);
}
