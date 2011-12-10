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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import jrico.jstickynotes.JStickyNotes;
import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.util.XmlReaderWriter;

/**
 * Manages the persistence of notes in emails.
 * 
 * @author Jonatan Rico (jrico) jnrico@gmail.com
 * 
 */
public class RemoteRepository implements NoteRepository {

    private static final Logger logger = Logger.getLogger(RemoteRepository.class.getName());
	
	private static final String FOLDER_NAME = "JStickyNotes";

    private Session session;
    private Store store;
    private Folder folder;

    private String host = "imap.gmail.com";
    private String username = "";
    private String password = "";
    private boolean connected;

    @Override
    public boolean add(Note note) {
        logger.entering(this.getClass().getName(), "add", note);
        boolean success = true;
        if ( this.isConnected() ) {
        	try {
        		delete(note);

        		MimeMessage message = new MimeMessage(session);
        		String xml = XmlReaderWriter.writeObjectsToString(note, note.getCategories());
        		message.setSubject(String.valueOf(note.getId()));
        		message.setText(xml);
        		message.saveChanges();
        		folder.appendMessages(new Message[] { message });
        	} catch (Exception e) {
        		logger.throwing(this.getClass().getName(), "delete", e);
        		success = false;
        		this.turnOffline(e);
        	}
        } else {
        	success = false;
        }
        logger.exiting(this.getClass().getName(), "add", success);
        return success;
    }

    @Override
    public boolean delete(Note note) {
    	logger.entering(this.getClass().getName(), "delete", note);
    	
        boolean success = true;
        if ( this.isConnected() ) {
        	try {
        		Message messages[] = folder.getMessages();
        		boolean deleted = false;
        		for (int i = 0; i < messages.length; i++) {
        			Message message = messages[i];
        			String subject = message.getSubject();
        			if (subject != null && !subject.trim().equals("")) {
        				long id = Long.parseLong(subject);
        				if (id == note.getId()) {
        					message.setFlag(Flag.DELETED, true);
        					deleted = true;
        				}
        			}
        		}
        		if (deleted) {
        			Message[] deletedMsgs = folder.expunge();
        			logger.log(Level.FINER, "deleted:", deletedMsgs);
        		}
        	} catch (Exception e) {
        		logger.throwing(this.getClass().getName(), "delete", e);
        		success = false;
        		this.turnOffline(e);
        	} 
        } else {
        	success = false;
        }
        logger.exiting(this.getClass().getName(), "delete", success);
        return success;
    }

    @Override
    public List<Note> retrieve() {
        List<Note> notes = new ArrayList<Note>();
        if ( this.isConnected() ) {
        	try {
        		Message messages[] = folder.getMessages();
        		for (int i = 0, n = messages.length; i < n; i++) {
        			Note note = XmlReaderWriter.readObjectFromString(messages[i].getContent().toString());
        			if (note != null) {
        				notes.add(note);
        			}
        		}
        	} catch (Exception e) {
        		e.printStackTrace();
        		this.turnOffline(e);
        	} 
        }
        return notes;
    }

    @Override
    public boolean update(Note note) {
    	logger.entering(this.getClass().getName(), "update", note); 
    	boolean success = add(note);
    	logger.exiting(this.getClass().getName(), "update", success);
        return success;
    }

    public boolean openSession() {
        try {
        	// Get session
        	if (session == null) {
        		session = Session.getDefaultInstance(new Properties(), null);
        	}

        	// Get the store
        	if (store == null) {
        		store = session.getStore("imaps");
        		store.connect(host, username, password);
        	}

        	// Get folder
        	if (folder == null) {
        		folder = store.getFolder(FOLDER_NAME);

        		if (!folder.exists()) {
        			logger.finer("Creating folder " + Folder.HOLDS_MESSAGES + "on server." );
        			folder.create(Folder.HOLDS_MESSAGES);
        		}
        		folder.open(Folder.READ_WRITE);
        		connected = true;
        	}
        } catch (Exception e) {
        	logger.throwing(this.getClass().getName(), "openSession", e);
        	turnOffline(e);
        }
        return connected;
    }

    public void closeSession() {
    	connected = false;
    	try {
            if (folder != null) {
                folder.close(false);
            }
            if (store != null) {
                store.close();
            }
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "closeSession", e);
        } finally {
            folder = null;
            store = null;
            session = null;
        }
    }
    
    public boolean isConnected(){
    	if ( connected && store != null && store.isConnected()) {
    		connected = true;
    	} else if (connected) {
    		//the connection got lost, let know the app
    		logger.finer("connected=" + connected + ", store=" + store + ", isConnected=" + store.isConnected());
    		this.turnOffline(new Exception("Couldn't reach the " +
    				"imap server, connection lost?"));
    	}
    	return connected;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    private void turnOffline(Exception e){
    	connected = false;
    	closeSession();
    	JStickyNotes.getInstance().pushMessage(e);
    	
    }
}
