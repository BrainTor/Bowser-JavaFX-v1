package ru.robograde.browserjfx;

import javafx.application.Application;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.web.WebView;

import java.io.IOException;

public class Main extends Application {
    boolean conLos=false;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        stage.setTitle("RGCloud");
        stage.minWidthProperty().set(626);
        stage.getIcons().add(new Image("file:icon.png"));
//        stage.maximizedProperty().a//ddListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
//                System.out.println("maximized:" + ov.getValue().booleanValue());
//                if(ov.getValue().booleanValue()==false){
//                    stage.setMaximized(true);
//                    isMaximazedT=true;
//                }
//            }
//        });
//        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
//            if(isMaximazedT==true){
//                stage.ximized(true);
//            }
//        });
        stage.focusedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean lostFocus, Boolean gainFocus) -> {
            if (lostFocus){
                conLos=true;
            }
            if (conLos) {
                   Clipboard clipboard = Clipboard.getSystemClipboard();
                   ClipboardContent content = new ClipboardContent();
                   content.putString("Не надо,дядя)");
                   clipboard.setContent(content);
            }
        });
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.PRINTSCREEN) {
                System.out.println("Нажат принт скрин");
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString("Не надо,дядя)");
                clipboard.setContent(content);
            }
        });
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}