package ru.robograde.browserjfx;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


class BlockRightButtonDispatcher implements EventDispatcher {

    private final EventDispatcher originalDispatcher;

    public BlockRightButtonDispatcher(EventDispatcher originalDispatcher) {
        this.originalDispatcher = originalDispatcher;
    }

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        if (event instanceof MouseEvent mouseEvent) {
            if (MouseButton.SECONDARY == mouseEvent.getButton()) {
                mouseEvent.consume();
            }
        }
        return originalDispatcher.dispatchEvent(event, tail);
    }
}

class BlockCopyEventDispatcher implements EventDispatcher {

    private final EventDispatcher oldDispatcher;

    public BlockCopyEventDispatcher(EventDispatcher oldDispatcher) {
        this.oldDispatcher = oldDispatcher;
    }

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        if (event instanceof KeyEvent k && event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
            if ((k.getCode().equals(KeyCode.C) || k.getCode().equals(KeyCode.INSERT)) && k.isControlDown()) {
                event.consume();
            }
            if (k.isShiftDown() && (k.getCode().equals(KeyCode.RIGHT) || k.getCode().equals(KeyCode.LEFT) ||
                    k.getCode().equals(KeyCode.UP) || k.getCode().equals(KeyCode.DOWN))) {
                event.consume();
            }
        }
        return oldDispatcher.dispatchEvent(event, tail);
    }
}

public class MainController implements Initializable {
    @FXML
    private WebView webView;
    static Document pdfActivity, docActivity;
    Document doc;
    private static WebEngine webEngine;
    Timer tm = new java.util.Timer();
    Timer reloadTimer = new java.util.Timer();
    boolean pdfActFinded = false, docActFinded = false;
    boolean timerStarted=false;
    boolean reloadTimerStarted=false;
    int del = 5,reloadDel=20;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();
        loadPage();
    }

    private class subtimer extends TimerTask {
        //run method
        @Override
        public void run() {
            Platform.runLater(() -> {
                    if (doc != null && doc .getElementById("content")!=  null&&doc.getElementById("content").getAttribute("class").equals("app-dashboard")&&doc.getElementById("content").getAttribute("role").equals("main")) {
                        System.out.println("pdfViewerLoaded");
                        pdfActivity = doc;
                        pdfActFinded = true;
                    }
                    if (doc != null && doc.getElementById("onlyofficeViewerFrame") != null) {
                        System.out.println("docViewerLoaded");
                        docActivity = doc;
                        docActFinded = true;
                    }
                timerStarted=false;
                    check();
            });
        }
    }

    private class timerForReload extends TimerTask {
        //run method
        @Override
        public void run() {
            Platform.runLater(() -> {
            loadPage();
            reloadTimerStarted=false;
            reLoad();
            });
        }
    }
    public String getStringFromElement(Element el) {
        try {
            DOMSource domSource = new DOMSource(el);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
            public void check(){
                if(!timerStarted){
                    if(!pdfActFinded&&!docActFinded){
                        tm.schedule(new subtimer(), del*1000);
                        System.out.println("timerStart");
                        timerStarted=true;
                    }
                }
            }
            public void reLoad(){
                 if(!reloadTimerStarted){
                     reloadTimer.schedule(new timerForReload(), reloadDel*1000);
                }
            }
    public void loadPage() {
        webEngine.load("https://sphinixcloud.ru/login");
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Atom/19.0.0.25 Safari/537.36");
        webView.setContextMenuEnabled(false);
        EventDispatcher originalDispatcher = webView.getEventDispatcher();
        webView.setEventDispatcher(new BlockCopyEventDispatcher(new BlockRightButtonDispatcher(originalDispatcher)));
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                doc = webEngine.getDocument();
                check();
            } else if (newState == Worker.State.FAILED) {
                // через таймер перезагружать
                System.out.println("Not loaded");
                reLoad();
            }
        });
        webView.setOnDragEntered(event -> {
            System.out.println("ent");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Перенос ссылки запрещен");
            alert.setHeaderText(null);
            alert.setContentText("Перенос ссылки запрещен!");
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString("Не надо,дядя)");
            clipboard.setContent(content);
            alert.showAndWait();
            alert.close();
            webView.toFront();
            event.consume();
        });
    }
}