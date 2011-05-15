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

import static jrico.jstickynotes.JStickyNotes.BUNDLE;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jrico.jstickynotes.util.Pair;

public class StickyNoteHeader extends JPanel {

    public static final String DELETE_PROPERTY = "StickyNoteHeader.delete";
    public static final String MAIL_PROPERTY = "StickyNoteHeader.mail";
    public static final String FONT_PROPERTY = "StickyNoteHeader.font";
    public static final String COLOR_PROPERTY = "StickyNoteHeader.color";
    public static final String ALWAYS_ON_TOP_PROPERTY = "StickyNoteHeader.alwaysOnTop";
    public static final String HIDE_PROPERTY = "StickyNoteHeader.hide";

    public static final String DELETE_TEXT = BUNDLE
            .getString("StickyNoteHeader.deleteTooltip.text");
    public static final String DELETE_DIALOG_TEXT = BUNDLE
            .getString("StickyNoteHeader.deleteDialog.text");
    public static final String DELETE_DIALOG_TITLE_TEXT = BUNDLE
            .getString("StickyNoteHeader.deleteDialogTitle.text");
    public static final String MAIL_TEXT = BUNDLE
            .getString("StickyNoteHeader.mailTooltip.text");
    public static final String CHANGE_FONT_TEXT = BUNDLE
            .getString("StickyNoteHeader.changeFontTooltip.text");
    public static final String CHANGE_COLOR_TEXT = BUNDLE
            .getString("StickyNoteHeader.changeColorTooltip.text");
    public static final String CHANGE_COLOR_DIALOG_TITLE_TEXT = BUNDLE
            .getString("StickyNoteHeader.changeColorDialogTitle.text");
    public static final String ALWAYS_ON_TOP_TEXT = BUNDLE
            .getString("StickyNoteHeader.alwaysOnTopTooltip.text");
    public static final String HIDE_TEXT = BUNDLE
            .getString("StickyNoteHeader.hideTooltip.text");

    private IconRepository iconRepository;
    private boolean alwaysOnTop;
    private JLabel deleteLabel;
    private JLabel mailLabel;
    private JLabel fontLabel;
    private JLabel changeColorLabel;
    private JLabel alwaysOnTopLabel;
    private JLabel hideLabel;

    public StickyNoteHeader() {
        super(new FlowLayout(FlowLayout.RIGHT));
        iconRepository = IconRepository.getInstance();
        initComponents();
    }

    private void initComponents() {
        // create header actions
        deleteLabel = new JLabel(iconRepository
                .getIcon(IconRepository.DELETE_ICON_TYPE));
        deleteLabel.setToolTipText(DELETE_TEXT);
        deleteLabel.setBorder(BorderFactory.createEmptyBorder());
        deleteLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int option = JOptionPane.showConfirmDialog(null,
                        DELETE_DIALOG_TEXT, DELETE_DIALOG_TITLE_TEXT,
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    firePropertyChange(DELETE_PROPERTY, false, true);
                }
            }
        });
        add(deleteLabel);

        mailLabel = new JLabel(iconRepository
                .getIcon(IconRepository.MAIL_ICON_TYPE));
        mailLabel.setToolTipText(MAIL_TEXT);
        mailLabel.setBorder(BorderFactory.createEmptyBorder());
        mailLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                firePropertyChange(MAIL_PROPERTY, false, true);
            }
        });
        // TODO uncomment the next line to add mail storage support
        // add(mailLabel);

        fontLabel = new JLabel(iconRepository
                .getIcon(IconRepository.FONT_ICON_TYPE));
        fontLabel.setToolTipText(CHANGE_FONT_TEXT);
        fontLabel.setBorder(BorderFactory.createEmptyBorder());
        fontLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Pair<Font, Color> pair = FontChooser.showDialog(null,
                        getFont(), getForeground());
                if (pair != null) {
                    setFont(pair.getObjectA());
                    setForeground(pair.getObjectB());
                    firePropertyChange(FONT_PROPERTY, null, pair);
                }
            }
        });
        add(fontLabel);

        changeColorLabel = new JLabel(iconRepository
                .getIcon(IconRepository.COLOR_ICON_TYPE));
        changeColorLabel.setToolTipText(CHANGE_COLOR_TEXT);
        changeColorLabel.setBorder(BorderFactory.createEmptyBorder());
        changeColorLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color color = JColorChooser.showDialog(null,
                        CHANGE_COLOR_DIALOG_TITLE_TEXT, getBackground());
                if (color != null) {
                    firePropertyChange(COLOR_PROPERTY, null, color);
                }
            }
        });
        add(changeColorLabel);

        alwaysOnTopLabel = new JLabel(iconRepository
                .getIcon(IconRepository.ALWAYS_ON_TOP_UNSET_ICON_TYPE));
        alwaysOnTopLabel.setToolTipText(ALWAYS_ON_TOP_TEXT);
        alwaysOnTopLabel.setBorder(BorderFactory.createEmptyBorder());
        alwaysOnTopLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                alwaysOnTop = !alwaysOnTop;
                setAlwaysOnTopIcon();
                firePropertyChange(ALWAYS_ON_TOP_PROPERTY, !alwaysOnTop,
                        alwaysOnTop);
            }
        });
        add(alwaysOnTopLabel);

        hideLabel = new JLabel(iconRepository
                .getIcon(IconRepository.HIDE_ICON_TYPE));
        hideLabel.setToolTipText(HIDE_TEXT);
        hideLabel.setBorder(BorderFactory.createEmptyBorder());
        hideLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                firePropertyChange(HIDE_PROPERTY, false, true);
            }
        });
        add(hideLabel);
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
        setAlwaysOnTopIcon();
    }

    private void setAlwaysOnTopIcon() {
        alwaysOnTopLabel.setIcon(alwaysOnTop ? iconRepository
                .getIcon(IconRepository.ALWAYS_ON_TOP_SET_ICON_TYPE)
                : iconRepository
                        .getIcon(IconRepository.ALWAYS_ON_TOP_UNSET_ICON_TYPE));
    }
}
