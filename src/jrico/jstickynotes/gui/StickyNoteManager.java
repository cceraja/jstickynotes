/*
 * JStickyNotes, Copyright (C) Feb 14, 2009 - Jonatan Rico (jrico) jnrico@gmail.com
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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

import jrico.jstickynotes.JStickyNotes;
import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.persistence.NoteManager;

/**
 * Manages the screen's windows representing the notes on the desktop.
 * 
 * @author Jonatan Rico (jrico) jnrico@gmail.com
 * 
 */
public class StickyNoteManager implements PropertyChangeListener {
    private static final StickyNoteManager INSTANCE = new StickyNoteManager();

    private NoteManager noteManager;

    private IconRepository iconRepository;

    private Map<Note, StickyNote> stickyNotes;

    private JDialog parentFrame;

    private boolean initialized;

    private StickyNoteManager() {
        noteManager = NoteManager.getInstance();
        iconRepository = IconRepository.getInstance();
        stickyNotes = new HashMap<Note, StickyNote>();
    }

    public static StickyNoteManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (!initialized) {
            initialized = true;

            // create the parent window for all sticky notes
            parentFrame = new JDialog();
            parentFrame.setTitle(JStickyNotes.JSTICKYNOTES_TEXT);
            parentFrame.setIconImages(iconRepository.getJStickyNotesImages());
            parentFrame.setUndecorated(true);
            parentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            parentFrame.setSize(0, 0);
            parentFrame.setLocation(-1, -1);
            parentFrame.setVisible(true);

            parentFrame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    if (ke.getKeyCode() == KeyEvent.VK_S && ke.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
                        showNotes(true);
                    } else if (ke.getKeyCode() == KeyEvent.VK_H && ke.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
                        hideNotes();
                    } else if (ke.getKeyCode() == KeyEvent.VK_N && ke.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
                        createNote();
                    }
                }
            });

            // retrieve the stored notes
            new StoredNotesRetriever().execute();
        }
    }

    public void createNote() {
        Note note = noteManager.createNote();
        note.addPropertyChangeListener(this);
        StickyNote stickyNote = new StickyNote(parentFrame, note);
        stickyNotes.put(note, stickyNote);
        stickyNote.startEditingText();
        note.setVisible(true);
    }

    public void showNotes(boolean showAllNotes) {
        for (Note note : stickyNotes.keySet()) {
            boolean isVisible = note.isVisible() || showAllNotes;
            note.setVisible(isVisible);
            stickyNotes.get(note).setVisible(isVisible);
        }
    }

    public void hideNotes() {
        for (Note note : stickyNotes.keySet()) {
            stickyNotes.get(note).setVisible(false);
            note.setVisible(false);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        String property = pce.getPropertyName();
        if (source instanceof Note) {
            Note note = (Note) source;
            if (property.equals(Note.STATUS_PROPERTY) && note.getStatus() == Note.DELETED_STATUS) {
                stickyNotes.remove(note);
            }
        }
    }

    private class StoredNotesRetriever extends SwingWorker<List<Note>, Note> {

        private Note[] emptyArray = new Note[] {};

        @Override
        protected List<Note> doInBackground() throws Exception {
            List<Note> storedNotes = new ArrayList<Note>();
            List<Note> localStoredNotes = noteManager.getLocalStoredNotes();
            System.out.println("StoredNotesRetriever.doInBackground() - local stored notes retrieved");
            publish(localStoredNotes.toArray(emptyArray));
            List<Note> remoteStoredNotes = noteManager.getRemoteStoredNotes();
            System.out.println("StoredNotesRetriever.doInBackground() - remote stored notes retrieved");
            publish(remoteStoredNotes.toArray(emptyArray));
            storedNotes.addAll(localStoredNotes);
            storedNotes.addAll(remoteStoredNotes);
            return storedNotes;
        }

        @Override
        protected void process(List<Note> notes) {
            for (Note note : notes) {
                if (stickyNotes.containsKey(note)) {
                    StickyNote oldStickyNote = stickyNotes.get(note);
                    oldStickyNote.dispose();
                }
                note.setVisible(true);
                note.addPropertyChangeListener(StickyNoteManager.this);
                stickyNotes.put(note, new StickyNote(parentFrame, note));
            }
        }
    }
}
