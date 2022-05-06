package ru.robograde.browserjfx.dispatchers;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class BlockRightButtonDispatcher implements EventDispatcher {

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