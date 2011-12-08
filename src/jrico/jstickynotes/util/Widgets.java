package jrico.jstickynotes.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


public class Widgets {
    private static final String ESC_ACTION = "ESC_ACTION";

    /**
     * 
     * @param component
     * @param action
     */
    public static void installEscAction(JComponent component, Action action) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            ESC_ACTION);
        component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESC_ACTION);
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESC_ACTION);
        component.getActionMap().put(ESC_ACTION, action);
    }

    /**
     * 
     * @param component
     * @param callbackTarget
     * @param methodName
     */
    public static void installEscAction(JComponent component, Object callbackTarget, String methodName) {
        installEscAction(component, new ReflectiveAction(callbackTarget, methodName));
    }

    /*
     * Utility class, no instances.
     */
    private Widgets() {
    }

    private static class ReflectiveAction extends AbstractAction {
        private Object callbackTarget;
        private String methodName;

        public ReflectiveAction(Object callbackTarget, String methodName) {
            this.callbackTarget = callbackTarget;
            this.methodName = methodName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Classes.invokeMethod(callbackTarget, methodName, Classes.ALL_RECURSIVE);
        }
    }
}
