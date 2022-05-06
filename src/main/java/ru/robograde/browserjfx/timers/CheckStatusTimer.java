package ru.robograde.browserjfx.timers;

import ru.robograde.browserjfx.controllers.MainController;

import java.util.TimerTask;

public class CheckStatusTimer extends TimerTask {
    @Override
    public void run() {
        MainController.checkStatus();
    }
}
