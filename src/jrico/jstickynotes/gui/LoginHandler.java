package jrico.jstickynotes.gui;

import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import jrico.jstickynotes.model.Preferences;
import jrico.jstickynotes.util.Pair;

public class LoginHandler implements Runnable {
    private Preferences preferences;
    private Pair<String, String> credentials;
    private CountDownLatch signal;

    public LoginHandler(Preferences preferences) {
        this.preferences = preferences;
    }

    public Pair<String, String> login() {
        signal = new CountDownLatch(1);
        SwingUtilities.invokeLater(this);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return credentials;
    }

    @Override
    public void run() {
        credentials = LoginDialog.showDialog(preferences.getUsername(), preferences.getPassword());
        signal.countDown();
    }
}
