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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.model.Preferences;

/**
 * Manages the notes and their local (files) or remote (emails) persistence.
 * 
 * @author Jonatan Rico (jrico) jnrico@gmail.com
 * 
 */
public class NoteManager implements PropertyChangeListener {

    private static Logger logger = Logger.getLogger(NoteManager.class.getName());

    private final LocalRepository localRepository;

    private final RemoteRepository remoteRepository;

    private final Map<Note, Note> notes;

    private final BlockingQueue<Note> transactions;

    private final Preferences preferences;

    private List<Note> remoteNoteCopies;

    /**
     * Notes that had changes comparing them to the local repo (from the last sync).
     */
    private List<Note> lastSyncRemoteNotes;

    private final Lock lock;

    public NoteManager(Preferences preferences) {
        this.preferences = preferences;
        localRepository = new LocalRepository();
        remoteRepository = new RemoteRepository();
        notes = new HashMap<Note, Note>();
        transactions = new LinkedBlockingQueue<Note>();
        lock = new ReentrantLock();
        Thread thread = Executors.defaultThreadFactory().newThread(new TransactionCommiter());
        thread.setDaemon(true);
        thread.start();
    }

    private void initializeLocalNotes() {
        logger.entering(this.getClass().getName(), "initializeLocalNotes");
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
        logger.exiting(this.getClass().getName(), "initializeLocalNotes");
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        logger.entering(this.getClass().getName(), "propertyChange", pce);
        Note note = (Note) pce.getSource();
        logger.finer("Fired by property: " + pce.getPropertyName());
        if (!pce.getPropertyName().equals(Note.STATUS_PROPERTY) && !pce.getPropertyName().equals(Note.VERSION_PROPERTY)) {
            note.setStatus(Note.MODIFIED_STATUS);
        } else if (note.getStatus() == Note.MODIFIED_STATUS || note.getStatus() == Note.DELETED_STATUS
                || note.getStatus() == Note.LOCAL_OUTDATED_STATUS) {
            this.optimizeTransactions(note);
            transactions.offer(note);
        }
        logger.exiting(this.getClass().getName(), "propertyChange");
    }

    public Note createNote() {
        logger.entering(this.getClass().getName(), "createNote");
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
        this.optimizeTransactions(note);
        transactions.offer(note);
        logger.exiting(this.getClass().getName(), "createNote", note);
        return note;
    }

    public List<Note> getLocalStoredNotes() {
        logger.entering(this.getClass().getName(), "getLocalStoredNotes");
        List<Note> onlyLocalNotes = new ArrayList<Note>();
        initializeLocalNotes();
        for (Note note : notes.values()) {
            if (note.getType() == Note.LOCAL_TYPE) {
                onlyLocalNotes.add(note);
            }
        }
        logger.exiting(this.getClass().getName(), "getLocalStoredNotes", onlyLocalNotes);
        return onlyLocalNotes;
    }

    public List<Note> getRemoteStoredNotes() {
        logger.entering(this.getClass().getName(), "getRemoteStoredNotes");
        // if local notes haven't been retrieved yet...
        if (remoteNoteCopies == null) {
            initializeLocalNotes();
        }
        if (remoteRepository.isConnected()) {
            syncRepositories();
        }
        logger.exiting(this.getClass().getName(), "getRemoteStoredNotes", remoteNoteCopies);
        return remoteNoteCopies;
    }

    /**
     * Sync the local and remote repositories
     * 
     * @return a list of Notes with changes
     */
    public List<Note> syncRemoteNotes() {
        logger.entering(this.getClass().getName(), "syncRemoteNotes");
        if (remoteRepository.isConnected()) {
            this.syncRepositories();
        }
        logger.exiting(this.getClass().getName(), "syncRemoteNotes", lastSyncRemoteNotes);
        return this.lastSyncRemoteNotes;
    }

    private void syncRepositories() {
        logger.entering(this.getClass().getName(), "syncRepositories");
        if (remoteNoteCopies == null) {
            initializeLocalNotes();
        }
        if (remoteRepository.isConnected()) {
            lastSyncRemoteNotes = new ArrayList<Note>();
            List<Note> remoteNotes = remoteRepository.retrieve();
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
                            lastSyncRemoteNotes.add(note);
                        } else {
                            // else, overwrite locally only
                            note.setStatus(Note.LOCAL_OUTDATED_STATUS);
                            remoteNoteCopies.set(remoteNoteCopies.indexOf(note), note);
                            lastSyncRemoteNotes.add(note);
                        }
                    } else if (note.compareVersionTo(remoteNoteCopy) == 0
                            && remoteNoteCopy.getStatus() == Note.MODIFIED_STATUS) {
                        // remote version outdated
                        remoteNoteCopy.setStatus(Note.MODIFIED_STATUS);
                    }
                } else {
                    // remote note does not exist in local
                    note.setStatus(Note.LOCAL_OUTDATED_STATUS);
                    remoteNoteCopies.add(note);
                    lastSyncRemoteNotes.add(note);
                }
                if (notes.containsKey(note)) {
                    Note oldNote = notes.remove(note);
                    oldNote.removePropertyChangeListeners();
                }
                notes.put(note, note);
            }

        }
        logger.exiting(this.getClass().getName(), "syncRepositories");
    }

    public boolean connectRemote() {
        remoteRepository.setHost(preferences.getHost());
        remoteRepository.setUsername(preferences.getUsername());
        remoteRepository.setPassword(preferences.getPassword());
        return this.remoteRepository.openSession();
    }

    public void disconnectRemote() {
        if (this.remoteRepository.isConnected()) {
            this.remoteRepository.closeSession();
        }
    }

    // This helps avoiding unneccesary updates to remote repository + prevents
    // duplicates
    private void optimizeTransactions(Note note) {
        logger.entering(this.getClass().getName(), "optimizeTransactions", note);
        if (transactions.size() > 0 && remoteRepository.isConnected()) {
            lock.lock();
            int counter = 0;
            while (transactions.contains(note)) {
                counter++;
                transactions.remove(note);
            }
            logger.finer(counter + " transactions discarded.");
            lock.unlock();
        }
        logger.exiting(this.getClass().getName(), "optimizeTransactions");
    }

    private class TransactionCommiter implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    lock.lock();
                    Note note = transactions.take();
                    lock.unlock();
                    logger.finer("START Transaction: " + note);
                    if (note.getStatus() == Note.CREATED_STATUS) {
                        note.setStatus(Note.STORED_STATUS);
                        logger.finer("status=CREATED");
                        localRepository.add(note);
                        // TODO we need to implement a way to leave "pending" the "remote" notes that were
                        // created while the app is offline. This sync will happen when the app is closed
                        // and open again, OR if the stickynote is modified, but anyway this sync should
                        // happen in the same run and without the need of modifying the stickynote.
                        if (note.getType() == Note.REMOTE_TYPE && remoteRepository.isConnected()) {
                            // sync version
                            note.setVersion(note.getVersion() + 1);
                            if (!remoteRepository.add(note)) {
                                note.setVersion(note.getVersion() - 1);
                            }
                        }
                    } else if (note.getStatus() == Note.MODIFIED_STATUS) {
                        logger.finer("status=UPDATED");
                        note.setStatus(Note.STORED_STATUS);
                        localRepository.update(note);
                        if (note.getType() == Note.REMOTE_TYPE && remoteRepository.isConnected()) {
                            // sync version
                            note.setVersion(note.getVersion() + 1);
                            if (!remoteRepository.update(note)) {
                                note.setVersion(note.getVersion() - 1);
                            }
                        }
                    } else if (note.getStatus() == Note.DELETED_STATUS) {
                        logger.finer("status=DELETED");
                        notes.remove(note);
                        if (remoteRepository.delete(note)) {
                            // delete from local only if it was successfully deleted from remote
                            localRepository.delete(note);
                        }
                        if (note.getType() == Note.REMOTE_TYPE && remoteRepository.isConnected()) {
                            remoteRepository.delete(note);
                        }
                    } else if (note.getStatus() == Note.LOCAL_OUTDATED_STATUS) {
                        logger.finer("status=LOCAL_OUTDATED");
                        if (localRepository.update(note)) {
                            note.setStatus(Note.STORED_STATUS);
                        }
                    }
                    logger.finer("END Transaction");
                } catch (InterruptedException e) {
                    logger.throwing(this.getClass().getName(), "run", e);
                    e.printStackTrace();
                }

            }
        }
    }
}
