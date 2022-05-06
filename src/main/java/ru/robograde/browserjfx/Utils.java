package ru.robograde.browserjfx;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

public class Utils {
    public static File getFileFromResource(String fileName) throws URISyntaxException, FileNotFoundException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    public static void clearClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString("");
        clipboard.setContent(content);
    }
}
