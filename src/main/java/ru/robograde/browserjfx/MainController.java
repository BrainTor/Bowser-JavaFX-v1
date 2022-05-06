package ru.robograde.browserjfx;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import jdk.swing.interop.SwingInterOpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;


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
    @FXML
    private ImageView sideBar, find, privious, next, zoomOut, zoomIn, presentationMode, viewBookmark, secondaryToolbarToggle;
    Document doc;
    private static WebEngine webEngine;
    static Document pdfActivity, docActivity;
    boolean pdfActFound = false, docActFound = false;
    Timer tm = new java.util.Timer();
    Timer reloadTimer = new java.util.Timer();
    boolean timerStarted = false;
    boolean reloadTimerStarted = false;
    int delay = 1, reloadDelay = 20;
    String iframeSrc;
    boolean printed = false, imageCreated = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();
        loadPage();
    }

    org.jsoup.nodes.Document document;
    String str = "";
    org.jsoup.nodes.Document finalPdf;
    org.jsoup.nodes.Document newIframe;
    org.jsoup.nodes.Element newPdfIframe;

    private class SubTimer extends TimerTask {
        //run method
        public String getStringFromElement(org.w3c.dom.Element el) {
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

        public String getStringFromDocument(Document doc) {
            try {
                DOMSource domSource = new DOMSource(doc);
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

        @Override
        public void run() {
            Platform.runLater(() -> {
                org.w3c.dom.Element iframePDf = (org.w3c.dom.Element) doc.getElementsByTagName("iframe").item(0);
                if (iframePDf != null) {
                    str = iframePDf.getAttribute("src");
                    if (iframePDf.getAttribute("src").equals(str) && doc.getElementById("onlyofficeViewerFrame") == null) {
                        System.out.println("pdfViewerLoaded");
                        String[] nameOfImages = {"sidebar", "find", "previous", "next", "zoomOut", "zoomIn", "presentation", "bookmark", "secondaryToolbar"};
                        ImageView[] images={sideBar, find, privious, next, zoomOut, zoomIn, presentationMode, viewBookmark, secondaryToolbarToggle};
                        if (!imageCreated) {
                            for (int i = 0; i < nameOfImages.length; i++) {
                                File file = new File("src/images/" + nameOfImages[i] + ".png");
                                Image image = new Image(file.toURI().toString());
                                images[i].setImage(image);
                            }
                            imageCreated = true;
                        }
                        else{
                            if(webView.getScene().getWidth()>=665){
                                sideBar.relocate(3, 53);
                                find.relocate(63, 53);
                                privious.relocate(94, 53);
                                next.relocate(124, 53);
                                secondaryToolbarToggle.relocate(webView.getScene().getWidth()-28,53);
                                viewBookmark.relocate(webView.getScene().getWidth()-65,53);
                                presentationMode.relocate(webView.getScene().getWidth()-93,53);
                            }else{
                                sideBar.relocate(3, 53);
                                find.relocate(33, 53);
                                privious.setVisible(false);
                                next.setVisible(false);
                                viewBookmark.setVisible(false);
                                presentationMode.setVisible(false);
                                secondaryToolbarToggle.relocate(webView.getScene().getWidth()-28,53);
                            }

                              if(webView.getScene().getWidth()>=626&&webView.getScene().getWidth()<=665){

                              }
                           if(!sideBar.isVisible()){
                               for (int i=0;i< images.length;i++){
                                   images[i].setVisible(true);
                               }
                           }
                        }
                        pdfActivity = doc;
                        ArrayList<String> styles = new ArrayList<>();
                        NodeList style = doc.getElementsByTagName("style");
                        for (int i = 0; i < style.getLength(); i++) {
                            org.w3c.dom.Node stl = style.item(i);
                            styles.add(stl.getTextContent());
                        }
                        org.w3c.dom.Element iframe = (org.w3c.dom.Element) pdfActivity.getElementsByTagName("iframe").item(0);
//                        if (iframe != null) {
//                            iframeSrc = "https://sphinixcloud.ru" + iframe.getAttribute("src");
//                        }
//                        if (iframe != null) {
//                            try {
////                            System.out.println(Jsoup.connect(iframeSrc).get());
//                                String[] elementIDs = {
//                                        "sidebarToggle",
//                                        "viewFind",
//                                        "previous",
//                                        "next",
//                                        "zoomOut",
//                                        "zoomIn",
//                                        "presentationMode",
//                                        "viewBookmark",
//                                        "secondaryToolbarToggle"
//                                };
//                                String newElementString[] = {
//                                        "<button id=\"sidebarToggle\" class=\"toolbarButton\" title=\"Toggle Sidebar\" tabindex=\"11\" data-l10n-id=\"toggle_sidebar\"><svg version=\"1.1\" id=\"Capa_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"20.062px\" height=\"20.062px\" viewBox=\"0 0 468.062 468.062\" style=\"enable-background:new 0 0 468.062 468.062;\" xml:space=\"preserve\"><g><path d=\"M431.379,0.222h-394.7C16.458,0.222,0,16.671,0,36.895v394.268c0,20.221,16.458,36.677,36.679,36.677h394.7 c20.228,0,36.683-16.456,36.683-36.677V36.895C468.062,16.665,451.606,0.222,431.379,0.222z M406.519,41.966 c8.689,0,15.723,7.04,15.723,15.72c0,8.683-7.033,15.717-15.723,15.717c-8.688,0-15.723-7.04-15.723-15.717 C390.796,49.006,397.83,41.966,406.519,41.966z M350.189,41.966c8.688,0,15.723,7.04,15.723,15.72 c0,8.683-7.034,15.717-15.723,15.717c-8.684,0-15.711-7.04-15.711-15.717C334.479,49.006,341.506,41.966,350.189,41.966z M262.278,425.921H41.913V112.426h220.365V425.921z M426.148,425.921H278.421V112.426h147.728V425.921z M409.557,203.765H297.583 v-69.605h111.974V203.765z M409.557,300.606H297.583v-69.605h111.974V300.606z M409.557,398.962H297.583v-69.605h111.974V398.962z\"/></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g></svg></button>",
//                                        "<button id=\"viewFind\" class=\"toolbarButton\" title=\"Find in Document\" tabindex=\"12\" data-l10n-id=\"findbar\"><svg width=\"21px\" height=\"21px\" viewBox=\"0 0 24 24\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M13.707 2.293A.996.996 0 0 0 13 2H6c-1.103 0-2 .897-2 2v16c0 1.103.897 2 2 2h12c1.103 0 2-.897 2-2V9a.996.996 0 0 0-.293-.707l-6-6zM6 4h6.586L18 9.414l.002 9.174-2.568-2.568c.35-.595.566-1.281.566-2.02 0-2.206-1.794-4-4-4s-4 1.794-4 4 1.794 4 4 4c.739 0 1.425-.216 2.02-.566L16.586 20H6V4zm6 12c-1.103 0-2-.897-2-2s.897-2 2-2 2 .897 2 2-.897 2-2 2z\"/></svg></button>",
//                                        "<button class=\"toolbarButton pageUp\" title=\"Previous Page\" id=\"previous\" tabindex=\"13\" data-l10n-id=\"previous\" disabled=\"\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 18.28125 9.023438 L 10.808594 0.136719 C 10.65625 -0.046875 10.34375 -0.046875 10.191406 0.136719 L 2.71875 9.023438 C 2.617188 9.144531 2.59375 9.3125 2.660156 9.457031 C 2.726562 9.597656 2.871094 9.6875 3.027344 9.6875 L 6.453125 9.6875 L 8.152344 19.867188 C 8.257812 20.503906 8.84375 21 9.488281 21 L 11.511719 21 C 12.15625 21 12.742188 20.503906 12.847656 19.867188 L 14.542969 9.6875 L 17.972656 9.6875 C 18.128906 9.6875 18.273438 9.597656 18.339844 9.457031 C 18.40625 9.3125 18.382812 9.144531 18.28125 9.023438 Z M 14.203125 8.878906 C 14.003906 8.878906 13.835938 9.023438 13.804688 9.21875 L 12.050781 19.734375 C 12.011719 19.976562 11.757812 20.191406 11.511719 20.191406 L 9.488281 20.191406 C 9.242188 20.191406 8.988281 19.976562 8.949219 19.734375 L 7.195312 9.21875 C 7.164062 9.023438 6.992188 8.878906 6.796875 8.878906 L 3.894531 8.878906 L 10.5 1.023438 L 17.105469 8.878906 Z M 14.203125 8.878906 \"/></g></svg></button>",
//                                        "<button class=\"toolbarButton pageDown\" title=\"Next Page\" id=\"next\" tabindex=\"14\" data-l10n-id=\"next\" disabled=\"\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0.392157%,0%,0.784314%);fill-opacity:1;\" d=\"M 10.671875 21 C 10.214844 21 9.902344 20.644531 9.867188 20.605469 L 3.574219 13 C 3.40625 12.84375 2.976562 12.378906 3.140625 11.96875 C 3.234375 11.726562 3.488281 11.605469 3.886719 11.605469 L 7.132812 11.605469 L 7.132812 0.867188 C 7.117188 0.765625 7.101562 0.53125 7.265625 0.324219 C 7.429688 0.109375 7.714844 0 8.105469 0 L 13.09375 0 C 13.449219 0 13.652344 0.132812 13.761719 0.246094 C 13.964844 0.457031 13.96875 0.710938 13.964844 0.757812 L 13.964844 11.378906 L 16.941406 11.378906 C 17.125 11.378906 17.683594 11.378906 17.855469 11.792969 C 18.03125 12.226562 17.601562 12.703125 17.464844 12.835938 C 17.273438 13.09375 12.117188 19.933594 11.367188 20.683594 C 11.105469 20.945312 10.847656 21 10.671875 21 Z M 3.722656 12.21875 C 3.769531 12.316406 3.890625 12.472656 4.011719 12.589844 L 10.328125 20.21875 C 10.371094 20.265625 10.519531 20.398438 10.671875 20.398438 C 10.761719 20.398438 10.847656 20.351562 10.941406 20.261719 C 11.664062 19.539062 16.957031 12.515625 17.011719 12.441406 C 17.164062 12.285156 17.289062 12.09375 17.292969 12.019531 C 17.277344 12.019531 17.191406 11.980469 16.941406 11.980469 L 13.367188 11.980469 L 13.367188 0.75 C 13.359375 0.652344 13.269531 0.601562 13.09375 0.601562 L 8.105469 0.601562 C 7.890625 0.601562 7.777344 0.648438 7.742188 0.691406 C 7.714844 0.722656 7.726562 0.769531 7.726562 0.769531 L 7.734375 0.839844 L 7.734375 12.207031 L 3.886719 12.207031 C 3.8125 12.207031 3.757812 12.210938 3.722656 12.21875 Z M 3.722656 12.21875 \"/></g></svg>",
//                                        "<button id=\"zoomOut\" class=\"toolbarButton zoomOut\" title=\"Zoom Out\" tabindex=\"21\" data-l10n-id=\"zoom_out\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 17.246094 9.371094 C 17.757812 7.453125 17.496094 5.445312 16.5 3.722656 C 15.175781 1.425781 12.703125 0 10.046875 0 C 8.746094 0 7.460938 0.34375 6.332031 1 C 4.609375 1.992188 3.375 3.597656 2.863281 5.519531 C 2.347656 7.441406 2.613281 9.445312 3.605469 11.167969 C 4.933594 13.464844 7.40625 14.890625 10.058594 14.890625 C 11.0625 14.890625 12.054688 14.6875 12.976562 14.292969 L 16.847656 21 L 18.394531 20.105469 L 14.523438 13.398438 C 15.851562 12.40625 16.808594 11.003906 17.246094 9.371094 Z M 12.882812 12.347656 C 12.023438 12.84375 11.046875 13.105469 10.058594 13.105469 C 8.042969 13.105469 6.160156 12.019531 5.152344 10.273438 C 4.398438 8.964844 4.195312 7.441406 4.589844 5.980469 C 4.980469 4.523438 5.914062 3.300781 7.222656 2.546875 C 8.085938 2.050781 9.0625 1.785156 10.046875 1.785156 C 12.066406 1.785156 13.945312 2.871094 14.953125 4.617188 C 15.710938 5.925781 15.910156 7.449219 15.519531 8.910156 C 15.128906 10.371094 14.191406 11.589844 12.882812 12.347656 Z M 7.074219 6.550781 L 13.03125 6.550781 L 13.03125 8.339844 L 7.074219 8.339844 Z M 7.074219 6.550781 \"/></g></svg></button>",
//                                        "<button id=\"zoomIn\" class=\"toolbarButton zoomIn\" title=\"Zoom In\" tabindex=\"22\" data-l10n-id=\"zoom_in\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 17.246094 9.371094 C 17.757812 7.453125 17.496094 5.445312 16.5 3.722656 C 15.175781 1.425781 12.703125 0 10.046875 0 C 8.746094 0 7.460938 0.34375 6.332031 1 C 4.609375 1.992188 3.375 3.597656 2.863281 5.519531 C 2.347656 7.441406 2.613281 9.445312 3.605469 11.167969 C 4.933594 13.464844 7.40625 14.890625 10.058594 14.890625 C 11.0625 14.890625 12.054688 14.6875 12.976562 14.292969 L 16.847656 21 L 18.394531 20.105469 L 14.523438 13.398438 C 15.851562 12.40625 16.808594 11.003906 17.246094 9.371094 Z M 12.882812 12.347656 C 12.023438 12.84375 11.046875 13.105469 10.058594 13.105469 C 8.042969 13.105469 6.160156 12.019531 5.152344 10.273438 C 4.398438 8.964844 4.195312 7.441406 4.589844 5.980469 C 4.980469 4.523438 5.914062 3.300781 7.222656 2.546875 C 8.085938 2.050781 9.0625 1.785156 10.046875 1.785156 C 12.066406 1.785156 13.945312 2.871094 14.953125 4.617188 C 15.710938 5.925781 15.910156 7.449219 15.519531 8.910156 C 15.128906 10.371094 14.191406 11.589844 12.882812 12.347656 Z M 10.945312 6.550781 L 13.03125 6.550781 L 13.03125 8.339844 L 10.945312 8.339844 L 10.945312 10.425781 L 9.160156 10.425781 L 9.160156 8.339844 L 7.074219 8.339844 L 7.074219 6.550781 L 9.160156 6.550781 L 9.160156 4.46875 L 10.945312 4.46875 Z M 10.945312 6.550781 \"/></g></svg></button>",
//                                        "<button id=\"presentationMode\" class=\"toolbarButton presentationMode hiddenLargeView\" title=\"Switch to Presentation Mode\" tabindex=\"31\" data-l10n-id=\"presentation_mode\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 20.546875 1.15625 L 10.925781 1.15625 L 10.925781 0.453125 L 10.070312 0.453125 L 10.070312 1.15625 L 0.453125 1.15625 L 0.453125 16.679688 L 6.800781 16.679688 L 4.679688 20.074219 L 5.398438 20.554688 L 7.816406 16.683594 L 13.195312 16.683594 L 15.613281 20.546875 L 16.332031 20.070312 L 14.210938 16.679688 L 20.546875 16.679688 Z M 19.738281 15.800781 L 1.308594 15.800781 L 1.308594 1.992188 L 19.738281 1.992188 Z M 19.738281 15.800781 \"/><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 9.089844 4.421875 L 2.933594 4.421875 L 2.933594 13.417969 L 9.089844 13.417969 Z M 8.230469 12.535156 L 3.785156 12.535156 L 3.785156 5.257812 L 8.230469 5.257812 Z M 8.230469 12.535156 \"/><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 11.824219 6.449219 L 17.640625 6.449219 L 17.640625 7.328125 L 11.824219 7.328125 Z M 11.824219 6.449219 \"/><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 11.824219 10.507812 L 17.640625 10.507812 L 17.640625 11.390625 L 11.824219 11.390625 Z M 11.824219 10.507812 \"/></g></svg></button>",
//                                        "<a href=\"#page=1&amp;zoom=auto,-109,848\" id=\"viewBookmark\" class=\"toolbarButton bookmark hiddenSmallView\" title=\"Current view (copy or open in new window)\" tabindex=\"35\" data-l10n-id=\"bookmark\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 11.8125 1.03125 C 11.550781 0.425781 10.949219 0 10.25 0 L 3.414062 0 L 3.414062 0.0585938 C 3.964844 0.164062 4.417969 0.53125 4.640625 1.03125 L 1.816406 1.03125 L 1.816406 21 L 19.183594 21 L 19.183594 1.03125 Z M 17.964844 19.777344 C 16.875 19.777344 4.125 19.777344 3.039062 19.777344 C 3.039062 18.671875 3.039062 3.359375 3.039062 2.253906 C 3.246094 2.253906 3.878906 2.253906 4.773438 2.253906 L 4.773438 14.265625 L 8.269531 10.503906 L 11.953125 14.265625 L 11.953125 2.253906 C 14.878906 2.253906 17.496094 2.253906 17.964844 2.253906 C 17.964844 3.359375 17.964844 18.671875 17.964844 19.777344 Z M 17.964844 19.777344 \"/></g></svg></a>",
//                                        "<button id=\"secondaryToolbarToggle\" class=\"toolbarButton toggled\" title=\"Tools\" tabindex=\"36\" data-l10n-id=\"tools\" aria-expanded=\"true\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"21px\" height=\"21px\" viewBox=\"0 0 21 21\" version=\"1.1\"><g id=\"surface1\"><path style=\" stroke:none;fill-rule:nonzero;fill:rgb(0%,0%,0%);fill-opacity:1;\" d=\"M 19.234375 8.734375 C 17.964844 7.777344 16.550781 7.019531 15.050781 6.496094 C 14.957031 6.464844 14.882812 6.542969 14.863281 6.628906 C 14.824219 6.617188 14.78125 6.628906 14.746094 6.671875 C 14.542969 6.917969 14.28125 7.105469 14.085938 7.355469 C 13.886719 7.613281 13.902344 7.976562 13.898438 8.285156 C 12.097656 7.953125 10.238281 7.898438 8.417969 7.753906 C 6.050781 7.558594 3.679688 7.382812 1.304688 7.296875 C 1.261719 7.292969 1.230469 7.308594 1.207031 7.332031 C 1.15625 7.320312 1.097656 7.335938 1.066406 7.398438 C 1.039062 7.449219 0.125 8.226562 0.0390625 8.921875 C -0.0898438 9.941406 0.132812 13.851562 0.324219 13.960938 C 0.390625 13.996094 5.632812 13.628906 8.167969 13.304688 C 8.410156 13.273438 8.734375 13.246094 9.109375 13.21875 C 9.238281 13.21875 9.367188 13.21875 9.5 13.226562 C 9.546875 13.226562 9.582031 13.210938 9.605469 13.1875 C 11.183594 13.089844 13.25 13.027344 13.660156 13 C 13.675781 13.304688 13.648438 13.660156 13.785156 13.941406 C 13.921875 14.21875 14.179688 14.355469 14.433594 14.5 C 14.492188 14.53125 14.652344 14.484375 14.683594 14.476562 C 14.726562 14.472656 20.777344 11.277344 20.789062 11.269531 C 21.570312 10.734375 19.992188 9.304688 19.234375 8.734375 Z M 0.816406 10.378906 C 0.796875 10.402344 0.78125 10.421875 0.761719 10.445312 C 0.765625 10.328125 0.773438 10.210938 0.785156 10.09375 C 0.800781 10.058594 0.820312 10.027344 0.839844 9.996094 C 0.832031 10.125 0.824219 10.253906 0.816406 10.378906 Z M 20.414062 10.132812 C 20.398438 10.136719 20.386719 10.144531 20.371094 10.15625 C 20.03125 9.679688 19.46875 9.242188 19.132812 8.976562 C 19.089844 8.945312 19.050781 8.914062 19.007812 8.882812 C 19.527344 9.234375 20.101562 9.683594 20.414062 10.132812 Z M 14.542969 7.269531 C 14.511719 7.59375 14.453125 7.921875 14.492188 8.238281 C 14.410156 8.007812 14.480469 7.730469 14.492188 7.496094 C 14.496094 7.4375 14.46875 7.402344 14.4375 7.378906 C 14.46875 7.339844 14.507812 7.304688 14.542969 7.269531 Z M 1.175781 13.554688 C 1.355469 13.363281 1.597656 13.074219 1.597656 13.050781 C 1.742188 13.035156 1.886719 13.023438 2.035156 13.007812 C 1.960938 13.0625 1.609375 13.359375 1.398438 13.546875 C 1.324219 13.546875 1.25 13.550781 1.175781 13.554688 Z M 1.882812 13.527344 C 2.066406 13.34375 2.402344 13.027344 2.386719 12.972656 C 2.566406 12.957031 2.742188 12.9375 2.921875 12.917969 C 2.75 13.105469 2.589844 13.300781 2.441406 13.5 C 2.253906 13.511719 2.066406 13.519531 1.882812 13.527344 Z M 3.527344 13.4375 C 3.292969 13.453125 3.058594 13.46875 2.824219 13.480469 C 3.03125 13.230469 3.324219 12.898438 3.328125 12.878906 C 3.511719 12.859375 3.695312 12.839844 3.878906 12.820312 C 3.75 13.019531 3.65625 13.238281 3.527344 13.4375 Z M 3.882812 13.410156 C 4.027344 13.164062 4.230469 12.8125 4.230469 12.785156 C 4.480469 12.757812 4.730469 12.730469 4.980469 12.707031 C 4.816406 12.898438 4.683594 13.113281 4.597656 13.351562 C 4.359375 13.375 4.121094 13.394531 3.882812 13.410156 Z M 5.574219 13.238281 C 5.363281 13.265625 5.144531 13.292969 4.929688 13.316406 C 5.039062 13.085938 5.199219 12.882812 5.378906 12.699219 C 5.390625 12.6875 5.398438 12.675781 5.40625 12.660156 C 5.546875 12.648438 5.683594 12.632812 5.824219 12.621094 C 5.769531 12.773438 5.636719 13.074219 5.574219 13.238281 Z M 5.902344 13.199219 C 6.003906 13.003906 6.066406 12.792969 6.144531 12.589844 C 6.398438 12.5625 6.652344 12.539062 6.910156 12.515625 C 6.804688 12.726562 6.648438 12.90625 6.535156 13.113281 C 6.324219 13.140625 6.113281 13.167969 5.902344 13.199219 Z M 7.546875 13 C 7.328125 13.019531 7.109375 13.042969 6.890625 13.070312 C 7.003906 12.890625 7.140625 12.726562 7.222656 12.527344 C 7.230469 12.511719 7.230469 12.5 7.230469 12.484375 C 7.453125 12.464844 7.679688 12.445312 7.902344 12.425781 C 7.847656 12.515625 7.796875 12.605469 7.746094 12.695312 C 7.683594 12.800781 7.59375 12.882812 7.546875 13 Z M 8.59375 12.929688 C 8.363281 12.9375 8.128906 12.949219 7.898438 12.96875 C 7.941406 12.914062 7.984375 12.859375 8.019531 12.804688 C 8.082031 12.703125 8.136719 12.59375 8.207031 12.496094 C 8.226562 12.460938 8.234375 12.429688 8.230469 12.402344 C 8.441406 12.382812 8.652344 12.367188 8.863281 12.355469 C 8.785156 12.550781 8.699219 12.746094 8.59375 12.929688 Z M 9.480469 12.84375 C 9.296875 12.867188 9.117188 12.890625 8.933594 12.914062 C 9.023438 12.734375 9.101562 12.546875 9.175781 12.355469 C 9.179688 12.347656 9.175781 12.339844 9.179688 12.332031 C 9.390625 12.320312 9.597656 12.308594 9.808594 12.296875 C 9.707031 12.480469 9.558594 12.640625 9.480469 12.84375 Z M 10.890625 12.253906 C 10.894531 12.339844 10.839844 12.40625 10.796875 12.472656 C 10.742188 12.550781 10.695312 12.628906 10.65625 12.710938 C 10.375 12.738281 10.097656 12.769531 9.820312 12.804688 C 9.917969 12.625 10.058594 12.46875 10.136719 12.28125 C 10.136719 12.28125 10.136719 12.277344 10.136719 12.277344 C 10.390625 12.265625 10.640625 12.257812 10.894531 12.246094 C 10.894531 12.25 10.890625 12.25 10.890625 12.253906 Z M 11.714844 12.609375 C 11.714844 12.613281 11.710938 12.617188 11.710938 12.625 C 11.570312 12.632812 11.433594 12.644531 11.292969 12.652344 C 11.195312 12.660156 11.097656 12.671875 11 12.679688 C 11.082031 12.550781 11.1875 12.433594 11.183594 12.257812 C 11.183594 12.25 11.179688 12.246094 11.179688 12.238281 C 11.429688 12.230469 11.679688 12.226562 11.925781 12.222656 C 11.871094 12.359375 11.773438 12.472656 11.714844 12.609375 Z M 12.765625 12.601562 C 12.523438 12.59375 12.28125 12.597656 12.039062 12.605469 C 12.101562 12.5 12.175781 12.398438 12.21875 12.277344 C 12.226562 12.257812 12.230469 12.242188 12.230469 12.222656 C 12.453125 12.222656 12.679688 12.222656 12.902344 12.226562 C 12.855469 12.324219 12.796875 12.414062 12.761719 12.515625 C 12.75 12.546875 12.753906 12.574219 12.765625 12.601562 Z M 13.660156 12.621094 C 13.457031 12.640625 13.246094 12.628906 13.03125 12.617188 C 13.035156 12.609375 13.039062 12.605469 13.039062 12.601562 C 13.085938 12.480469 13.15625 12.375 13.207031 12.257812 C 13.210938 12.25 13.214844 12.242188 13.214844 12.234375 C 13.40625 12.238281 13.875 12.25 14.0625 12.257812 C 14.011719 12.328125 13.664062 12.613281 13.660156 12.621094 Z M 20.453125 11.109375 C 20.398438 11.164062 15.1875 13.898438 14.902344 13.894531 C 14.882812 13.894531 14.867188 13.898438 14.847656 13.90625 C 14.847656 13.902344 14.851562 13.902344 14.847656 13.902344 C 14.734375 13.359375 14.761719 12.632812 14.785156 12.085938 C 14.792969 12.007812 14.542969 11.996094 14.515625 11.992188 C 10.160156 11.71875 5.835938 12.367188 1.507812 12.765625 C 1.515625 12.75 1.519531 12.730469 1.519531 12.710938 C 1.578125 11.007812 1.597656 9.304688 1.589844 7.601562 C 3.773438 7.6875 5.949219 7.847656 8.128906 8.023438 C 10.347656 8.199219 12.742188 8.253906 14.910156 8.808594 C 15.007812 8.832031 15.082031 7.09375 15.105469 6.949219 C 16.160156 7.144531 21.253906 10.308594 20.453125 11.109375 Z M 20.453125 11.109375 \"/></g></svg></button>"
//                                };
//                                org.jsoup.nodes.Document soup = Jsoup.connect(iframeSrc).get();
//                                //тут твоя попытка Пшено
////
////                            if (soup.selectFirst("body").hasClass("loadingInProgress")) {
////                                System.out.println("drochim drochim "+counter);
////                                counter++;
////                                if(counter==10){
////                                    //Тестовый вывод - если получится то победа
////                                    org.jsoup.nodes.Document documentSoup = Jsoup.parse(Objects.requireNonNull(this.getStringFromElement(doc.getDocumentElement())));
////                                    final org.jsoup.nodes.Document document = Jsoup.parse("<p id=\"sidebarToggle\">XUI!!!</p>");
////                                    final org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
////                                    settings.prettyPrint(false);
////                                    document.outputSettings(settings);
////                                    final Element button = document.getElementsByTag("p").get(0);
////                                    System.out.println(button);
////                                    soup.selectFirst("#sidebarToggle").replaceWith(button);
////                                    ((org.w3c.dom.Element) pdfActivity.getElementsByTagName("iframe").item(0)).setAttribute("innerHTML",  soup.html());
////                                    documentSoup.selectFirst("iframe").replaceWith(soup);
////                                    //
////                                    for (String sty : styles) {
////                                        System.out.println(sty);
////                                        Element styl = documentSoup.createElement("style");
////
////                                        styl.appendText(sty);
////
////                                        documentSoup.selectFirst("body").appendChild(styl);
////                                    }
////                                    //debug???
////                                    webEngine.loadContent(documentSoup.html());
////                                }
////                            } else {
////                                Map<String, Element> elements = MainController.getManyElementsByIDs(soup, elementIDs);
////                                //Преобразования стринги в элемент
////                            }
//                                //Создание новых кнопок
//                                Element newElementsPDF[] = new Element[newElementString.length];
//                                final org.jsoup.nodes.Document.OutputSettings settings = new org.jsoup.nodes.Document.OutputSettings();
//                                settings.prettyPrint(false);
//                                for (int i = 0; i < newElementString.length; i++) {
//                                    try {
//                                        if (i != 7) {
//                                            document = Jsoup.parseBodyFragment(newElementString[i]);
//                                            document.outputSettings(settings);
//                                            newElementsPDF[i] = document.getElementsByTag("button").get(0);
//                                        } else {
//                                            document = Jsoup.parseBodyFragment(newElementString[i]);
//                                            document.outputSettings(settings);
//                                            newElementsPDF[i] = document.getElementsByTag("a").get(0);
//                                        }
//                                    } catch (Exception e) {
//                                        System.out.println(e.getMessage());
//                                        System.out.println(i);
//                                    }
//
//                                }
//                                //replace новых кнопок
//                                for (int i = 0; i < newElementsPDF.length; i++) {
//                                    soup.selectFirst("#" + elementIDs[i]).replaceWith(newElementsPDF[i]);
//                                }
//                                //Короче Пшено я затрахался,тут было 35 попыток загрузить эту залупу в html Время 2:27,но я знаю как это сделать
//                                //1.Парсим pdfActivity в jsoup.document
//                                //2.Меняем iframe на soup после replace
//                                //3. Делаем WebEngine.loadContent("Zalupa.html")-ту которую мы получили после replace
//                                //4. Выдыхаем
//                                //Сделай это плиз,я бы сделал,но я не вдупляю нихуя
//                                //Это победа я нашел html pdf с нераспарсенным iframe
//                                String htmlPdf =  getStringFromDocument(pdfActivity);
//                                finalPdf = Jsoup.parseBodyFragment(htmlPdf);
//                                finalPdf.outputSettings(settings);
//                                String newPdfFrame="<iframe>"+soup.html()+"<iframe>";
//                                newIframe=Jsoup.parseBodyFragment(newPdfFrame);
//                                newIframe.outputSettings(settings);
//                                if(!printed){
//                                  //  System.out.println(newIframe.getAllElements().get(0));
//                                     // System.out.println(finalPdf.getElementsByTag("link").get(0));
//                                      //webEngine.loadContent("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Document</title></head><body><button class=\"zalupa\"></button><style>.zalupa{display: flex;justify-content:center; border: 3px solid green; width: 20px; height: 20px;}</style></body></html>");
//                                    //finalPdf.getElementsByTag("iframe").get(0).replaceWith(soup);
//                                    System.out.println(finalPdf.outerHtml());
//                                    //webEngine.loadContent(finalPdf.html());
//                                    printed=true;
//                                }
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                System.out.println(e.getMessage() + e.getCause());
//                            }
//                        }
                        pdfActFound = true;
                        docActFound = false;
                    }
                }
                if (doc != null && doc.getElementById("onlyofficeViewerFrame") != null) {
                    System.out.println("docViewerLoaded");
                    ImageView[] images={sideBar, find, privious, next, zoomOut, zoomIn, presentationMode, viewBookmark, secondaryToolbarToggle};
                    for (int i=0;i<images.length;i++){
                        images[i].setVisible(false);
                    }
                    docActivity = doc;
                    docActFound = true;
                    pdfActFound = false;
                }
                if(doc != null && iframePDf==null && doc.getElementById("onlyofficeViewerFrame") == null){
                    System.out.println("Main menu/login");
                    ImageView[] images={sideBar, find, privious, next, zoomOut, zoomIn, presentationMode, viewBookmark, secondaryToolbarToggle};
                    for (int i=0;i<images.length;i++){
                        if(images[i]!=null){
                            images[i].setVisible(false);
                        }
                    }
                }
                timerStarted = false;
                check();
            });
        }
    }
//тут мапа
//    public static Map<String, Element> getManyElementsByIDs(org.jsoup.nodes.Document soup, String[] ids) {
//        Map<String, Element> map = new HashMap<>();
//
//        for (String elementID : ids) {
//            Element el = soup.selectFirst("#" + elementID);
//
//            if (el == null) {
//                System.out.println("Element " + elementID + "not found");
//            }
//
//            map.put(elementID, el);
//        }
//        return map;
//    }

    private class ReloadTimer extends TimerTask {
        //run method
        @Override
        public void run() {
            Platform.runLater(() -> {
                loadPage();
                reloadTimerStarted = false;
                reLoad();
            });
        }
    }

    public void check() {
        if (!timerStarted) {
            if (!pdfActFound || !docActFound) {
                tm.schedule(new SubTimer(), delay * 1000L);
                timerStarted = true;
            }
        }
    }

    public void reLoad() {
        if (!reloadTimerStarted) {
            reloadTimer.schedule(new ReloadTimer(), reloadDelay * 1000L);
        }
    }

    public void loadPage() {
        webEngine.load("https://sphinixcloud.ru");
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Atom/19.0.0.25 Safari/537.36");
        webView.setContextMenuEnabled(false);
        webEngine.setJavaScriptEnabled(true);
        EventDispatcher originalDispatcher = webView.getEventDispatcher();
        webView.setEventDispatcher(new BlockCopyEventDispatcher(new BlockRightButtonDispatcher(originalDispatcher)));
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                doc = webEngine.getDocument();
                check();
            } else if (newState == Worker.State.FAILED) {
                // через таймер перезагружать
                System.out.println("Not loaded");
                //кривой таймер
                // reLoad();
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
        Platform.runLater(() -> {
            webView.prefHeightProperty().bind(webView.getScene().heightProperty());
            webView.prefWidthProperty().bind(webView.getScene().widthProperty());
        });

    }
}