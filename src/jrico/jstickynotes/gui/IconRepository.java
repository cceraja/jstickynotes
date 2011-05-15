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

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import jrico.jstickynotes.JStickyNotes;

public class IconRepository {

    private static final IconRepository INSTANCE = new IconRepository();

    private static final ImageIcon JSTICKY_NOTES_ICON_48 = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/icon48.png"));
    private static final ImageIcon JSTICKY_NOTES_ICON_32 = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/icon32.png"));
    private static final ImageIcon JSTICKY_NOTES_ICON_24 = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/icon24.png"));
    private static final ImageIcon JSTICKY_NOTES_ICON_16 = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/icon16.png"));
    private static final ImageIcon JSTICKY_NOTES_ICON_11 = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/icon11.png"));

    private static final ImageIcon DELETE_ICON = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/delete.png"));
    private static final ImageIcon MAIL_ICON = new ImageIcon(JStickyNotes.class
            .getResource("/jrico/jstickynotes/resource/icons/mail.png"));
    private static final ImageIcon HIDE_ICON = new ImageIcon(JStickyNotes.class
            .getResource("/jrico/jstickynotes/resource/icons/minimize.png"));
    private static final ImageIcon ALWAYS_ON_TOP_UNSET_ICON = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/alwaysOnTopUnset.png"));
    private static final ImageIcon ALWAYS_ON_TOP_SET_ICON = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/alwaysOnTopSet.png"));
    private static final ImageIcon COLOR_ICON = new ImageIcon(
            JStickyNotes.class
                    .getResource("/jrico/jstickynotes/resource/icons/color.png"));
    private static final ImageIcon FONT_ICON = new ImageIcon(JStickyNotes.class
            .getResource("/jrico/jstickynotes/resource/icons/font.png"));

    public static final int DELETE_ICON_TYPE = 1;
    public static final int MAIL_ICON_TYPE = 2;
    public static final int HIDE_ICON_TYPE = 3;
    public static final int ALWAYS_ON_TOP_UNSET_ICON_TYPE = 4;
    public static final int ALWAYS_ON_TOP_SET_ICON_TYPE = 5;
    public static final int COLOR_ICON_TYPE = 6;
    public static final int FONT_ICON_TYPE = 7;

    private IconRepository() {
    }

    public static IconRepository getInstance() {
        return INSTANCE;
    }

    public List<ImageIcon> getJStickyNotesIcons() {
        List<ImageIcon> icons = new ArrayList<ImageIcon>(5);
        icons.add(JSTICKY_NOTES_ICON_11);
        icons.add(JSTICKY_NOTES_ICON_16);
        icons.add(JSTICKY_NOTES_ICON_24);
        icons.add(JSTICKY_NOTES_ICON_32);
        icons.add(JSTICKY_NOTES_ICON_48);
        return icons;
    }

    public List<Image> getJStickyNotesImages() {
        List<Image> images = new ArrayList<Image>(5);
        images.add(JSTICKY_NOTES_ICON_11.getImage());
        images.add(JSTICKY_NOTES_ICON_16.getImage());
        images.add(JSTICKY_NOTES_ICON_24.getImage());
        images.add(JSTICKY_NOTES_ICON_32.getImage());
        images.add(JSTICKY_NOTES_ICON_48.getImage());
        return images;
    }

    public ImageIcon getJStickyNotesIcon(int size) {
        ImageIcon icon = null;
        if (size > 0 && size <= 13) {
            icon = JSTICKY_NOTES_ICON_11;
        } else if (size > 13 && size <= 20) {
            icon = JSTICKY_NOTES_ICON_16;
        } else if (size > 20 && size <= 28) {
            icon = JSTICKY_NOTES_ICON_24;
        } else if (size > 28 && size <= 42) {
            icon = JSTICKY_NOTES_ICON_32;
        } else if (size > 42) {
            icon = JSTICKY_NOTES_ICON_48;
        }
        return icon;
    }

    public ImageIcon getIcon(int iconType) {
        ImageIcon icon = null;
        if (iconType == DELETE_ICON_TYPE) {
            icon = DELETE_ICON;
        } else if (iconType == MAIL_ICON_TYPE) {
            icon = MAIL_ICON;
        } else if (iconType == HIDE_ICON_TYPE) {
            icon = HIDE_ICON;
        } else if (iconType == ALWAYS_ON_TOP_UNSET_ICON_TYPE) {
            icon = ALWAYS_ON_TOP_UNSET_ICON;
        } else if (iconType == ALWAYS_ON_TOP_SET_ICON_TYPE) {
            icon = ALWAYS_ON_TOP_SET_ICON;
        } else if (iconType == COLOR_ICON_TYPE) {
            icon = COLOR_ICON;
        } else if (iconType == FONT_ICON_TYPE) {
            icon = FONT_ICON;
        }
        return icon;
    }
}
