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

package jrico.jstickynotes.persistence;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import jrico.jstickynotes.gui.LoginHandler;
import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.model.Preferences;
import jrico.jstickynotes.util.Pair;

/**
 * Manages the notes and their local (files) or remote (emails) persistence.
 * 
 * @author Jonatan Rico (jrico) jnrico@gmail.com
 * 
 */
public class NoteManager implements PropertyChangeListener {

    private LocalRepository localRepository;

    private RemoteRepository remoteRepository;

    private Map<Note, Note> notes;

    private BlockingQueue<Note> transactions;

    private Preferences preferences;

    private List<Note> remoteNoteCopies;

    public NoteManager(Preferences preferences) {
        this.preferences = preferences;
        localRepository = new LocalRepository();
        remoteRepository = new RemoteRepository();
        notes = new HashMap<Note, Note>();
        transactions = new LinkedBlockingQueue<Note>();
        Thread thread = Executors.defaultThreadFactory().newThread(new TransactionCommiter());
        thread.setDaemon(true);
        thread.start();
    }

    private void initializeLocalNotes() {
        remoteNoteCopies = new ArrayList<Note>();
        for (Note note : localRepository.retrieve()) {
            note.addPropertyChangeListener(this);
            if (note.getType() == Note.LOCAL_TYPE) {
                if (notes.containsKey(note)) {
                    Note oldNote = notes.remove(note);
                    oldNote.removePropertyChangeListeners();
                }
                notes.put(note, note);
            } else {
                remoteNoteCopies.add(note);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        Note note = (Note) pce.getSource();
        if (!pce.getPropertyName().equals(Note.STATUS_PROPERTY)) {
            note.setStatus(Note.MODIFIED_STATUS);
        } else if (note.getStatus() == Note.MODIFIED_STATUS || note.getStatus() == Note.DELETED_STATUS
                || note.getStatus() == Note.LOCAL_OUTDATED_STATUS) {
            transactions.offer(note);
        }
    }

    public Note createNote() {
        Note note = new Note();
        note.setId(System.currentTimeMillis());
        note.setType(Note.LOCAL_TYPE);
        note.setCategories(new ArrayList<String>());
        note.setStatus(Note.CREATED_STATUS);
        note.setRelativeLocation(new Point(10, 10));
        note.setSize(new Dimension(150, 150));
        note.setColor(preferences.getDefaultNoteColor());
        note.setFont(preferences.getDefaultFont());
        note.setFontColor(preferences.getDefaultFontColor());
        note.addPropertyChangeListener(this);
        notes.put(note, note);
        transactions.offer(note);
        return note;
    }

    public List<Note> getLocalStoredNotes() {
        List<Note> onlyLocalNotes = new ArrayList<Note>();
        initializeLocalNotes();
        for (Note note : notes.values()) {
            if (note.getType() == Note.LOCAL_TYPE) {
                onlyLocalNotes.add(note);
            }
        }
        return onlyLocalNotes;
    }

    public List<Note> getRemoteStoredNotes() {
        // if local notes haven't been retrieved yet...
        if (remoteNoteCopies == null) {
            initializeLocalNotes();
        }

        List<Note> remoteNotes = remoteNoteCopies;

        if (preferences.isEmailEnabled()) {
            Pair<String, String> credentials = new LoginHandler(preferences).login();

            if (credentials != null) {
                remoteRepository.setHost(preferences.getHost());
                remoteRepository.setUsername(credentials.getObjectA());
                remoteRepository.setPassword(credentials.getObjectB());

                remoteNotes = remoteRepository.retrieve();
                String nLine = System.getProperty("line.separator");
                for (Note note : remoteNotes) {
                    note.addPropertyChangeListener(this);
                    if (remoteNoteCopies.contains(note)) {
                        // sync local copies
                        Note remoteNoteCopy = remoteNoteCopies.get(remoteNoteCopies.indexOf(note));
                        if (note.compareVersionTo(remoteNoteCopy) > 0) {
                            // if remote version is greater than local and...
                            if (remoteNoteCopy.getStatus() == Note.MODIFIED_STATUS) {
                                // local copy is modified, then conflict
                                note.setStatus(Note.CONFLICT_STATUS);
                                note.setText("REMOTE:" + nLine + note.getText() + nLine + nLine + "LOCAL:" + nLine
                                        + remoteNoteCopy.getText());
                                remoteNoteCopies.set(remoteNoteCopies.indexOf(note), note);
                            } else {
                                // else, overwrite locally only
                                note.setStatus(Note.LOCAL_OUTDATED_STATUS);
                                remoteNoteCopies.set(remoteNoteCopies.indexOf(note), note);
                            }
                        } else if (note.compareVersionTo(remoteNoteCopy) == 0
                                && remoteNoteCopy.getStatus() == Note.MODIFIED_STATUS) {
                            // remote version outdated
                            remoteNoteCopy.setStatus(Note.MODIFIED_STATUS);
                        }
                    }
                    if (notes.containsKey(note)) {
                        Note oldNote = notes.remove(note);
                        oldNote.removePropertyChangeListeners();
                    }
                    notes.put(note, note);
                }
            }
        }

        return remoteNotes;
    }

    private class TransactionCommiter implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Note note = transactions.take();
                    if (note.getStatus() == Note.CREATED_STATUS) {
                        note.setStatus(Note.STORED_STATUS);
                        System.out.println("TransactionCommiter.run() - creating the note " + note);
                        localRepository.add(note);
                        if (note.getType() == Note.REMOTE_TYPE) {
                            // sync version
                            note.setVersion(note.getVersion() + 1);
                            if (!remoteRepository.add(note)) {
                                note.setVersion(note.getVersion() - 1);
                            }
                        }
                    } else if (note.getStatus() == Note.MODIFIED_STATUS) {
                        System.out.println("TransactionCommiter.run() - updating the note " + note);
                        note.setStatus(Note.STORED_STATUS);
                        localRepository.update(note);
                        if (note.getType() == Note.REMOTE_TYPE) {
                            // sync version
                            note.setVersion(note.getVersion() + 1);
                            if (!remoteRepository.update(note)) {
                                note.setVersion(note.getVersion() - 1);
                            }
                        }
                    } else if (note.getStatus() == Note.DELETED_STATUS) {
                        System.out.println("TransactionCommiter.run() - removing the note " + note);
                        notes.remove(note);
                        if (remoteRepository.delete(note)) {
                            // delete from local only if it was successfully deleted from remote
                            localRepository.delete(note);
                        }
                        if (note.getType() == Note.REMOTE_TYPE) {
                            remoteRepository.delete(note);
                        }
                    } else if (note.getStatus() == Note.LOCAL_OUTDATED_STATUS) {
                        System.out.println("TransactionCommiter.run() - updating *only local* the note " + note);
                        if (localRepository.update(note)) {
                            note.setStatus(Note.STORED_STATUS);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
