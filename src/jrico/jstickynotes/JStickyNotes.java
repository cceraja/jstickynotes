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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import jrico.jstickynotes.gui.Icon;
import jrico.jstickynotes.gui.LoginHandler;
import jrico.jstickynotes.gui.PreferencesDialog;
import jrico.jstickynotes.gui.StickyNote;
import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.persistence.NoteManager;
import jrico.jstickynotes.persistence.PreferencesManager;
import jrico.jstickynotes.util.Pair;
import jrico.jstickynotes.util.Screen;

public class JStickyNotes implements Runnable, PropertyChangeListener, ActionListener {

	private static Logger logger = Logger.getLogger(JStickyNotes.class.getName());
	
	public static final String DIRECTORY = System.getProperty("user.home") + File.separator + ".jstickynotes";

    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("jrico.jstickynotes.resource.jstickynotes");

    public static final String JSTICKYNOTES_TEXT = BUNDLE.getString("JStickyNotes.text");
    public static final String CREATE_NOTE_TEXT = BUNDLE.getString("JStickyNotes.createNoteItem.text");
    public static final String SHOW_ALL_NOTES_TEXT = BUNDLE.getString("JStickyNotes.showAllItem.text");
    public static final String HIDE_ALL_NOTES_TEXT = BUNDLE.getString("JStickyNotes.hideAllItem.text");
    public static final String PREFERENCES_TEXT = BUNDLE.getString("JStickyNotes.preferencesItem.text");
    public static final String ABOUT_TEXT = BUNDLE.getString("JStickyNotes.aboutItem.text");
    public static final String ABOUT_DIALOG_TEXT = BUNDLE.getString("JStickyNotes.aboutDialog.text");
    public static final String ABOUT_DIALOG_TITLE_TEXT = BUNDLE.getString("JStickyNotes.aboutDialogTitle.text");
    public static final String EXIT_TEXT = BUNDLE.getString("JStickyNotes.exitItem.text");

    public static final int NONE = 0;
    public static final int ALWAYS_ON_TOP = 1;
    public static final int VISIBLE = 2;
    public static final int ALL = 3;
    
    private static final JStickyNotes INSTANCE = new JStickyNotes();

    private PreferencesManager preferencesManager;
    private NoteManager noteManager;
    private Map<Note, StickyNote> stickyNotes;
    private JFrame frame;
    private StickyNote childWindowParent;
    private int showMode;
    private boolean online;

    private JStickyNotes() {
        File directory = new File(DIRECTORY);
        if (!directory.exists()) {
            System.out.println("Creating the directory: " + DIRECTORY);
            directory.mkdir();
        }
        preferencesManager = new PreferencesManager();
        noteManager = new NoteManager(preferencesManager.getPreferences());
        stickyNotes = new HashMap<Note, StickyNote>();
    }

    @Override
    public void run() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popup = new PopupMenu();
            MenuItem createNoteItem = new MenuItem(CREATE_NOTE_TEXT);
            createNoteItem.addActionListener(this);
            popup.add(createNoteItem);
            popup.addSeparator();
            MenuItem showAllItem = new MenuItem(SHOW_ALL_NOTES_TEXT);
            showAllItem.addActionListener(this);
            popup.add(showAllItem);
            MenuItem hideAllItem = new MenuItem(HIDE_ALL_NOTES_TEXT);
            hideAllItem.addActionListener(this);
            popup.add(hideAllItem);
            popup.addSeparator();
            MenuItem preferencesItem = new MenuItem(PREFERENCES_TEXT);
            preferencesItem.addActionListener(this);
            popup.add(preferencesItem);
            popup.addSeparator();
            MenuItem aboutItem = new MenuItem(ABOUT_TEXT);
            aboutItem.addActionListener(this);
            popup.add(aboutItem);
            popup.addSeparator();
            MenuItem exitItem = new MenuItem(EXIT_TEXT);
            exitItem.addActionListener(this);
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(Icon.getJStickyNotesImageIcon(tray.getTrayIconSize().width).getImage(),
                JSTICKYNOTES_TEXT, popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
            	@Override
            	public void mousePressed(MouseEvent me) {
                    if (me.getButton() == MouseEvent.BUTTON1) {
                        frame.toFront();
                    }
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        } else {
            System.err.println("System tray is currently not supported.");
        }

        // create the parent window for all sticky notes
        frame = new JFrame(JStickyNotes.JSTICKYNOTES_TEXT);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImages(Icon.getJStickyNotesImages());
        frame.setUndecorated(true);
        frame.setSize(0, 0);
        frame.setLocation(0, 0);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                showNotes(VISIBLE);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                showNotes(ALWAYS_ON_TOP);
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                boolean controlDown = e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK;
                if (controlDown && keyCode == KeyEvent.VK_S && showMode < ALL) {
                    showNotes(showMode + 1);
                } else if (controlDown && keyCode == KeyEvent.VK_H && showMode > ALWAYS_ON_TOP) {
                    showNotes(showMode - 1);
                } else if (controlDown && keyCode == KeyEvent.VK_N) {
                    createNote();
                }
            }
        });
        frame.setVisible(true);

        // retrieve the stored notes
        new StoredNotesRetriever().execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source instanceof Note && pce.getNewValue().equals(Note.DELETED_STATUS)) {
            stickyNotes.remove(source);
        } else if (source instanceof StickyNote) {
            childWindowParent = ((Boolean) pce.getNewValue()) ? (StickyNote) source : null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String label = ((MenuItem) e.getSource()).getLabel();
        if (CREATE_NOTE_TEXT.equals(label)) {
            createNote();
        } else if (SHOW_ALL_NOTES_TEXT.equals(label)) {
            showNotes(ALL);
        } else if (HIDE_ALL_NOTES_TEXT.equals(label)) {
            showNotes(NONE);
        } else if (PREFERENCES_TEXT.equals(label)) {
            new PreferencesDialog(frame, preferencesManager.getPreferences()).setVisible(true);
        } else if (ABOUT_TEXT.equals(label)) {
            JLabel about = new JLabel(ABOUT_DIALOG_TEXT);
            about.setIcon(Icon.getJStickyNotesImageIcon(48));
            JOptionPane.showMessageDialog(frame, about, ABOUT_DIALOG_TITLE_TEXT, JOptionPane.PLAIN_MESSAGE);
        } else if (EXIT_TEXT.equals(label)) {
            System.exit(0);
        }
    }

    public void createNote() {
        Note note = noteManager.createNote();
        note.setVisible(true);
        note.addPropertyChangeListener(Note.STATUS_PROPERTY, this);
        StickyNote stickyNote = new StickyNote(frame, note);
        stickyNote.addPropertyChangeListener(StickyNote.CHILD_WINDOW_OPENED, this);
        stickyNotes.put(note, stickyNote);
        stickyNote.startEditingText();
    }

    public void showNotes(int mode) {
        if (childWindowParent == null) {
            showMode = mode;

            for (Note note : stickyNotes.keySet()) {
                StickyNote stickyNote = stickyNotes.get(note);
                if (mode == NONE) {
                    stickyNote.setVisible(false);
                } else if (mode == ALWAYS_ON_TOP) {
                    stickyNote.setVisible(note.isAlwaysOnTop());
                } else if (mode == VISIBLE) {
                    stickyNote.setVisible(note.isVisible());
                } else if (mode == ALL) {
                    stickyNote.setVisible(true);
                }
            }
        } else {
            for (Note note : stickyNotes.keySet()) {
                StickyNote stickyNote = stickyNotes.get(note);

                if (stickyNote != childWindowParent) {
                    stickyNote.setVisible(false);
                }
            }
        }
    }
    
    public JFrame getFrame() {
        return frame;
    }

    public static JStickyNotes getInstance() {
        return INSTANCE;
    }

    public boolean isOnline(){
    	return online;
    }
    
    public boolean connectRemote(boolean forceReconnection){
    	logger.entering(this.getClass().getName(), "connectRemote", forceReconnection);
    	if ( forceReconnection) {
    		this.disconnectRemote();
    	}
    	// TODO get/set the autoLogin property from GUI
    	if ( this.login( preferencesManager.getPreferences().isAutoLogin() 
    			&& preferencesManager.getPreferences().getUsername() != null 
    			&& preferencesManager.getPreferences().getPassword() != null ) ) {
    		// TODO show some GUI as ONLINE
    		online = true;
    	}
    	logger.exiting(this.getClass().getName(), "connectRemote", online);
    	return online;
    }
    
    public void disconnectRemote(){
    	logger.entering(this.getClass().getName(), "disconnectRemote");
    	noteManager.disconnectRemote();
    	online = false;
    	// TODO show some GUI as OFFLINE
    	logger.exiting(this.getClass().getName(), "disconnectRemote");
    }
    
    private void showErrorMessage(String msg){
    	final JOptionPane optionPane = new JOptionPane(msg,
				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
		JDialog dialog = optionPane.createDialog(frame, "Error");
		Screen.center(dialog);
		dialog.setVisible(true);
		optionPane.getValue();
    }
    
    public void pushMessage(Object msg) {
    	logger.entering(this.getClass().getName(), "pushMessage");
    	if ( msg instanceof Exception) {
    		Exception e = (Exception) msg;
    		logger.finer("Message received: " + e.getMessage());
    		this.disconnectRemote();
			this.showErrorMessage(e.getMessage());
			// TODO we need a GUI element for re-connect
			// TODO it would be good to have a re-connection handler in case of
			// disconnection
    	}
    	logger.exiting(this.getClass().getName(), "pushMessage");
    }
    
    private boolean login(boolean silent){
    	logger.entering(this.getClass().getName(), "login", silent);
    	boolean logged = false;
    	login_until_connect_or_cancel:
    	while (true) {
    		// silent is valid only the first time, if an error occurs then the attended login
    		// will be activated
    		if (!silent) {
    			Pair<String, String> credentials = new LoginHandler(preferencesManager.getPreferences()).login();
        		if ( credentials != null) {
        			preferencesManager.getPreferences().setUsername(credentials.getObjectA());
            		preferencesManager.getPreferences().setPassword(credentials.getObjectB());
        		} else {
        			//login cancelled
        			break login_until_connect_or_cancel;
        		}
    		}
    		if ( !noteManager.connectRemote()) {
    			silent = false; // deactivate silent
    		} else {
    			logged = true;
    			break login_until_connect_or_cancel;
    		}
    	}
    	logger.exiting(this.getClass().getName(), "login", logged);
    	return logged;
    }
    
    private class StoredNotesRetriever extends SwingWorker<Void, List<Note>> {
        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground() throws Exception {
            publish(noteManager.getLocalStoredNotes());
            logger.finer("Local stored notes retrieved");
            JStickyNotes.this.connectRemote(false);
            publish(noteManager.getRemoteStoredNotes());
            logger.finer("Remote stored notes retrieved");
            return null;
        }

        @Override
        protected void process(List<List<Note>> chunks) {
            for (List<Note> notes : chunks) {
                for (Note note : notes) {
                    if (stickyNotes.containsKey(note)) {
                        StickyNote oldStickyNote = stickyNotes.get(note);
                        oldStickyNote.dispose();
                    }
                    note.addPropertyChangeListener(Note.STATUS_PROPERTY, JStickyNotes.this);
                    StickyNote stickyNote = new StickyNote(frame, note);
                    stickyNote.addPropertyChangeListener(StickyNote.CHILD_WINDOW_OPENED, JStickyNotes.this);
                    stickyNotes.put(note, stickyNote);
                }
            }
        }
    }
    
   private static void configureLogger() {
	   // TODO for debugging purposes..., set to LEVEL.info for public releases
	   Logger logger = Logger.getLogger(JStickyNotes.class.getPackage().getName());
	   ConsoleHandler cHandler = new ConsoleHandler();
   		cHandler.setFormatter(new Formatter() {
   		  private Format formatter = new SimpleDateFormat("yyyy/MM/dd h:mm a");
   	      public String format(LogRecord record) {
   	    	 String parameters = "";
   	    	 if ( record.getParameters() != null) {
   	    		 if (record.getParameters().length > 1) {
   	    			 parameters += "[";
   	    		 }
   	    		 for ( Object param : record.getParameters()) {
   	    			 parameters += param.toString();
   	    		 }
   	    		if (record.getParameters().length > 1) {
   	    			parameters += "]";
   	    		}
   	    	 }
   	    	  
   	    	 return formatter.format(new Date(record.getMillis())) 
   	    	    + " " + record.getLevel() + " "
   	            + record.getSourceClassName() + " "
   	            + record.getSourceMethodName() + " : "
   	            + record.getMessage() + " " + parameters + "\n";
   	      }
   	    });
   		cHandler.setLevel(Level.FINER);
   		logger.addHandler(cHandler);
   		logger.setLevel(Level.FINER);
   }

    public static void main(String[] args) throws Exception {
    	configureLogger();
        logger.finer("Starting app...");
    	SwingUtilities.invokeLater(getInstance());
    }
}
