package ru.robograde.browserjfx.dispatchers;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.*;
import ru.robograde.browserjfx.Utils;

public class PrintScreenEventDispatcher implements EventDispatcher {

    private final EventDispatcher originalDispatcher;

    public PrintScreenEventDispatcher(EventDispatcher originalDispatcher) {
        this.originalDispatcher = originalDispatcher;
    }

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        if (event instanceof KeyEvent keyEvent) {
            if (keyEvent.getCode() == KeyCode.PRINTSCREEN) {
                Utils.clearClipboard();
            }
            else if (
                            keyEvent.isMetaDown() &&
                            keyEvent.isShiftDown() &&
                            keyEvent.getCode() == KeyCode.S
            ) {
                Utils.clearClipboard();
            }
        }
        return originalDispatcher.dispatchEvent(event, tail);
    }



}