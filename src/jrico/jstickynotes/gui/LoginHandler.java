package jrico.jstickynotes.gui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import jrico.jstickynotes.JStickyNotes;
import jrico.jstickynotes.model.Preferences;
import jrico.jstickynotes.util.Pair;

public class LoginHandler implements Runnable {
    private Preferences preferences;
    private Pair<String, String> credentials;

    public LoginHandler(Preferences preferences) {
        this.preferences = preferences;
    }

    public Pair<String, String> login() {
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            try {
                SwingUtilities.invokeAndWait(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return credentials;
    }

    @Override
    public void run() {
        credentials = LoginDialog.showDialog(JStickyNotes.getInstance().getFrame(), preferences.getUsername(),
            preferences.getPassword());
    }
}
