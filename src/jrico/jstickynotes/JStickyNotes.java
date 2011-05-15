/*
 * JStickyNotes, Copyright (C) Feb 13, 2009 - Jonatan Rico (jrico) jnrico@gmail.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package jrico.jstickynotes;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jrico.jstickynotes.gui.IconRepository;
import jrico.jstickynotes.gui.PreferencesDialog;
import jrico.jstickynotes.gui.StickyNoteManager;

public class JStickyNotes implements Runnable {

    public static final ResourceBundle BUNDLE = ResourceBundle
            .getBundle("jrico.jstickynotes.resource.jstickynotes");

    public static final String JSTICKYNOTES_TEXT = BUNDLE
            .getString("JStickyNotes.text");
    public static final String CREATE_NOTE_TEXT = BUNDLE
            .getString("JStickyNotes.createNoteItem.text");
    public static final String SHOW_ALL_NOTES_TEXT = BUNDLE
            .getString("JStickyNotes.showAllItem.text");
    public static final String HIDE_ALL_NOTES_TEXT = BUNDLE
            .getString("JStickyNotes.hideAllItem.text");
    public static final String PREFERENCES_TEXT = BUNDLE
            .getString("JStickyNotes.preferencesItem.text");
    public static final String ABOUT_TEXT = BUNDLE
            .getString("JStickyNotes.aboutItem.text");
    public static final String ABOUT_DIALOG_TEXT = BUNDLE
            .getString("JStickyNotes.aboutDialog.text");
    public static final String ABOUT_DIALOG_TITLE_TEXT = BUNDLE
            .getString("JStickyNotes.aboutDialogTitle.text");
    public static final String EXIT_TEXT = BUNDLE
            .getString("JStickyNotes.exitItem.text");

    private StickyNoteManager stickyNoteManager;

    private IconRepository iconRepository;

    public JStickyNotes() {
        stickyNoteManager = StickyNoteManager.getInstance();
        iconRepository = IconRepository.getInstance();
    }

    @Override
    public void run() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            ActionListener createNoteListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stickyNoteManager.createNote();
                }
            };
            ActionListener showAllListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stickyNoteManager.showNotes(true);
                }
            };
            ActionListener hideAllListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stickyNoteManager.hideNotes();
                }
            };
            ActionListener preferencesListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new PreferencesDialog(null).setVisible(true);
                }
            };
            ActionListener aboutListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JLabel about = new JLabel(ABOUT_DIALOG_TEXT);
                    about.setIcon(iconRepository.getJStickyNotesIcon(48));
                    JOptionPane.showMessageDialog(null, about,
                            ABOUT_DIALOG_TITLE_TEXT, JOptionPane.PLAIN_MESSAGE);
                }
            };
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

            PopupMenu popup = new PopupMenu();

            MenuItem createNoteItem = new MenuItem(CREATE_NOTE_TEXT);
            createNoteItem.addActionListener(createNoteListener);
            popup.add(createNoteItem);
            popup.addSeparator();
            MenuItem showAllItem = new MenuItem(SHOW_ALL_NOTES_TEXT);
            showAllItem.addActionListener(showAllListener);
            popup.add(showAllItem);
            MenuItem hideAllItem = new MenuItem(HIDE_ALL_NOTES_TEXT);
            hideAllItem.addActionListener(hideAllListener);
            popup.add(hideAllItem);
            popup.addSeparator();
            MenuItem preferencesItem = new MenuItem(PREFERENCES_TEXT);
            preferencesItem.addActionListener(preferencesListener);
            popup.add(preferencesItem);
            popup.addSeparator();
            MenuItem aboutItem = new MenuItem(ABOUT_TEXT);
            aboutItem.addActionListener(aboutListener);
            popup.add(aboutItem);
            popup.addSeparator();
            MenuItem exitItem = new MenuItem(EXIT_TEXT);
            exitItem.addActionListener(exitListener);
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(iconRepository
                    .getJStickyNotesIcon(16).getImage(), JSTICKYNOTES_TEXT,
                    popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    if (me.getButton() == MouseEvent.BUTTON1) {
                        stickyNoteManager.showNotes(false);
                    }
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

            stickyNoteManager.init();
        } else {
            System.err.println("System tray is currently not supported.");
        }
    }

    public static void main(String[] args) throws Exception {
        for (LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
            if (lafi.getName().equals("Nimbus")) {
                UIManager.setLookAndFeel(lafi.getClassName());
            }
        }
        SwingUtilities.invokeLater(new JStickyNotes());
    }
}
