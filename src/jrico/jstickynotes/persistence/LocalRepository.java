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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jrico.jstickynotes.JStickyNotes;
import jrico.jstickynotes.model.Note;
import jrico.jstickynotes.util.XmlReaderWriter;

/**
 * Manages the persistence of notes in files.
 * 
 * @author Jonatan Rico (jrico) jnrico@gmail.com
 * 
 */
public class LocalRepository implements NoteRepository {

    public static final String DIRECTORY = JStickyNotes.DIRECTORY + File.separator + "notes";

    public LocalRepository() {
        File notesDirectory = new File(DIRECTORY);
        if (!notesDirectory.exists()) {
            System.out.println("Creating the directory: " + DIRECTORY);
            notesDirectory.mkdir();
        }
    }

    @Override
    public boolean add(Note note) {
        String file = DIRECTORY + File.separator + note.getId();
        XmlReaderWriter.writeObjectsToFile(file, note, note.getCategories());
        //TODO this shouldn't be returning always true, above code should throw exceptions
        return true;
    }

    @Override
    public boolean delete(Note note) {
        File file = new File(DIRECTORY + File.separator + note.getId());
        if (file.exists()) {
        	return file.delete();
        }
        return true;
    }

    @Override
    public List<Note> retrieve() {
        File directory = new File(DIRECTORY);
        List<Note> notes = new ArrayList<Note>();
        for (File file : directory.listFiles()) {
            Note note = XmlReaderWriter.readObjectFromFile(file);
            if (note != null) {
                notes.add(note);
            }
        }
        return notes;
    }

    @Override
    public boolean update(Note note) {
        if ( delete(note) ) {
        	return add(note);
        }
    	return false;
    }
}
