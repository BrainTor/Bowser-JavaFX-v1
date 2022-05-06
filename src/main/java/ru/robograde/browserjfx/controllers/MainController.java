package ru.robograde.browserjfx.controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import ru.robograde.browserjfx.BrowserStatus;
import ru.robograde.browserjfx.Config;
import ru.robograde.browserjfx.dispatchers.BlockCopyEventDispatcher;
import ru.robograde.browserjfx.dispatchers.BlockRightButtonDispatcher;
import ru.robograde.browserjfx.images.CustomImage;
import ru.robograde.browserjfx.images.impl.*;
import ru.robograde.browserjfx.timers.CheckStatusTimer;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    private static WebView webView;
    public static Scene scene;

    private static final CustomImage[] images = {
            new FindImage(),
            new NextImage(),
            new PresentationModeImage(),
            new PreviousImage(),
            new SecondaryToolbarToggleImage(),
            new SidebarImage(),
    };

    private static WebEngine webEngine;

    private static BrowserStatus status = BrowserStatus.NONE;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            webView = (WebView) scene.lookup("#webView");
            webEngine = webView.getEngine();

            webEngine.load(Config.url);
            webEngine.setUserAgent(Config.userAgent);
            webView.setContextMenuEnabled(false);
            webEngine.setJavaScriptEnabled(true);
            webView.setEventDispatcher(
                    new BlockCopyEventDispatcher(
                            new BlockRightButtonDispatcher(
                                    webView.getEventDispatcher()
                            )
                    )
            );

            webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    new java.util.Timer().schedule(new CheckStatusTimer(), 2500);
                } else if (newState == Worker.State.FAILED) {
                    webEngine.load(Config.url);
                }
            });

            webView.setOnDragEntered(event -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Перенос ссылки запрещен!");

                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString("");
                clipboard.setContent(content);
                alert.showAndWait();
                alert.close();
                webView.toFront();
                event.consume();
            });

            webView.prefHeightProperty().bind(webView.getScene().heightProperty());
            webView.prefWidthProperty().bind(webView.getScene().widthProperty());
        });
    }

    public static void checkStatus() {
        status = BrowserStatus.NONE;
        Document document = webEngine.getDocument();
        if (document != null) {
            // Check is PDF
            org.w3c.dom.Element iframe_ = (org.w3c.dom.Element) document.getElementsByTagName("iframe").item(0);
            if (iframe_ != null) {
                status = BrowserStatus.PDF;
            }

            // Check is OnlyOffice
            if (document.getElementById("onlyofficeViewerFrame") != null) {
                status = BrowserStatus.ONLYOFFICE;
            }
            tick();
        }
        new java.util.Timer().schedule(new CheckStatusTimer(), 2500);
    }

    private static void tick() {
        if (status == BrowserStatus.PDF) {
            for (CustomImage image : images) {
                try {
                    image.configureImage(webView.getScene());
                } catch (URISyntaxException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (status == BrowserStatus.ONLYOFFICE) {
            // TODO
        }
        else if (status == BrowserStatus.NONE) {
            for (CustomImage image : images) {
                if (image.view != null)
                    image.view.setVisible(false);
            }
        }
    }

}