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

package jrico.jstickynotes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.plaf.basic.BasicTextPaneUI;

import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.util.Pair;
import jrico.jstickynotes.util.Screen;

public class StickyNote extends JWindow implements PropertyChangeListener {

    private Note note;
    private StickyNoteTextPane text;
    private JScrollPane scroll;
    private StickyNoteHeader header;
    private MoveController textMoveController;
    private MoveController scrollMoveController;
    private ResizeController scrollResizeController;
    private MoveController headerMoveController;

    public StickyNote(JDialog parent, Note note) {
        super(parent);
        this.note = note;
        init();
    }

    private void init() {
        Color color = note.getColor();
        ToFrontListener toFrontListener = new ToFrontListener();

        // create text pane
        text = new StickyNoteTextPane();
        text.setText(note.getText());
        text.setUI(new BasicTextPaneUI());
        text.setBackground(color);
        text.setFont(note.getFont());
        text.setForeground(note.getFontColor());
        text.addMouseListener(toFrontListener);
        text.addPropertyChangeListener(this);
        textMoveController = new MoveController(text, this);
        textMoveController.addPropertyChangeListener(this);

        // create scroll pane
        int borderSize = ResizeController.RESIZE_THRESHOLD;
        scroll = new JScrollPane() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g2d);
            }
        };
        scroll.setBorder(BorderFactory.createEmptyBorder(0, borderSize,
                borderSize, borderSize));
        scroll.setBackground(color);
        scroll.setForeground(color);
        scroll.setViewportView(text);
        scroll.addMouseListener(toFrontListener);
        scrollMoveController = new MoveController(scroll, this);
        scrollResizeController = new ResizeController(scroll, this);
        scrollMoveController.addPropertyChangeListener(this);
        scrollResizeController.addPropertyChangeListener(this);
        add(scroll, BorderLayout.CENTER);

        // create the header
        header = new StickyNoteHeader();
        header.setFont(note.getFont());
        header.setForeground(note.getFontColor());
        header.setBackground(color);
        header.setAlwaysOnTop(note.isAlwaysOnTop());
        header.addMouseListener(toFrontListener);
        header.addPropertyChangeListener(this);
        headerMoveController = new MoveController(header, this);
        headerMoveController.addPropertyChangeListener(this);
        add(header, BorderLayout.NORTH);

        // window properties
        setSize(note.getSize());
        setAlwaysOnTop(note.isAlwaysOnTop());
        setLocationRelativeTo(null);
        setLocation(Screen.getLocation(note));
        setVisible(true);
    }

    public Note getNote() {
        return note;
    }

    public void startEditingText() {
        text.startEditing();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        String property = pce.getPropertyName();

        if (property.equals(MoveController.RELATIVE_LOCATION_PROPERTY)) {
            // process the events fired by MoveControllers attached to the
            // components
            Point relativeLocation = (Point) pce.getNewValue();
            note.setRelativeLocation(relativeLocation);
        } else if (source == text) {
            processTextEvents(pce);
        } else if (source == scrollResizeController) {
            processScrollResizeControllerEvents(pce);
        } else if (source == header) {
            processHeaderEvents(pce);
        }
    }

    private void processTextEvents(PropertyChangeEvent pce) {
        String property = pce.getPropertyName();

        if (property.equals(StickyNoteTextPane.EDITING_PROPERTY)) {
            boolean isEditing = (Boolean) pce.getNewValue();
            // if the text component is in editing state, disable the
            // MoveContoller associated to the text component, enable it
            // otherwise
            textMoveController.setIgnoreEvents(isEditing);
            if (isEditing) {
                // request the focus for the window
                setFocusable(true);
                setFocusableWindowState(true);
            } else {
                // request the focus for the parent window
                setFocusable(false);
                getParent().requestFocus();
            }
        } else if (property.equals(StickyNoteTextPane.TEXT_PROPERTY)) {
            String text = (String) pce.getNewValue();
            note.setText(text);
        }
    }

    private void processScrollResizeControllerEvents(PropertyChangeEvent pce) {
        String property = pce.getPropertyName();

        if (property.equals(ResizeController.DRAGGING_PROPERTY)) {
            // if the scroll component is in resizing state, disable the
            // MoveContoller associated to the scroll component, enable it
            // otherwise
            scrollMoveController.setIgnoreEvents((Boolean) pce.getNewValue());
        } else if (property.equals(ResizeController.SIZE_PROPERTY)) {
            Dimension size = (Dimension) pce.getNewValue();
            note.setSize(size);
        }
    }

    private void processHeaderEvents(PropertyChangeEvent pce) {
        String property = pce.getPropertyName();

        if (property.equals(StickyNoteHeader.DELETE_PROPERTY)) {
            note.setStatus(Note.DELETED_STATUS);
            dispose();
        } else if (property.equals(StickyNoteHeader.MAIL_PROPERTY)) {
            note.setType(Note.REMOTE_TYPE);
        } else if (property.equals(StickyNoteHeader.FONT_PROPERTY)) {
            Pair<Font, Color> pair = (Pair<Font, Color>) pce.getNewValue();
            text.setFont(pair.getObjectA());
            text.setForeground(pair.getObjectB());
            note.setFont(pair.getObjectA());
            note.setFontColor(pair.getObjectB());
        } else if (property.equals(StickyNoteHeader.COLOR_PROPERTY)) {
            Color color = (Color) pce.getNewValue();
            text.setBackground(color);
            scroll.setForeground(color);
            header.setBackground(color);
            note.setColor(color);
        } else if (property.equals(StickyNoteHeader.ALWAYS_ON_TOP_PROPERTY)) {
            boolean isAlwaysOnTop = (Boolean) pce.getNewValue();
            setAlwaysOnTop(isAlwaysOnTop);
            note.setAlwaysOnTop(isAlwaysOnTop);
        } else if (property.equals(StickyNoteHeader.HIDE_PROPERTY)) {
            setVisible(false);
            note.setVisible(false);
        }
    }

    private class ToFrontListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            toFront();
        }
    }
}
