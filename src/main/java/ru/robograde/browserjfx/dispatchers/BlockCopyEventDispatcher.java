package ru.robograde.browserjfx.dispatchers;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.robograde.browserjfx.Utils;

public class BlockCopyEventDispatcher implements EventDispatcher {

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
            Utils.clearClipboard();

        }
        return oldDispatcher.dispatchEvent(event, tail);
    }
}
