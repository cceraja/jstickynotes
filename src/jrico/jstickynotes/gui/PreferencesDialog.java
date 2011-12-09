/*
 * JStickyNotes, Copyright (C) Feb 23, 2009 - Jonatan Rico (jrico) jnrico@gmail.com
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
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jrico.jstickynotes.model.Preferences;
import jrico.jstickynotes.util.Pair;
import jrico.jstickynotes.util.Widgets;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author Jonatan Rico
 */
@SuppressWarnings("serial")
public class PreferencesDialog extends JDialog implements ChangeListener {

    private JPanel dialogPane;
    private JPanel buttonBar;
    private JSeparator buttonsSeparator;
    private JButton okButton;
    private JButton cancelButton;
    private JTabbedPane preferencesTabbedPane;
    private JScrollPane generalScroll;
    private JPanel generalPanel;
    private JLabel generalDescriptionLabel;
    private JButton colorButton;
    private JLabel colorLabel;
    private JButton fontButton;
    private JLabel fontLabel;

    private Preferences preferences;
    private JPanel emailPanel;
    private JLabel usernameLabel;
    private JTextField usernameText;
    private JLabel passwordLabel;
    private JPasswordField passwordText;
    private JLabel emailDescriptionLabel;
    private JCheckBox emailEnabledCheckbox;
    private JCheckBox passwordStoredCheckbox;
    private JCheckBox showPasswordCheckbox;
    private char echoChar;
    private JLabel hostLabel;
    private JTextField hostText;

    public PreferencesDialog(JFrame owner, Preferences preferences) {
        super(owner);
        this.preferences = preferences;
        initComponents();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == emailEnabledCheckbox) {
            hostLabel.setEnabled(emailEnabledCheckbox.isSelected());
            hostText.setEnabled(emailEnabledCheckbox.isSelected());
            usernameLabel.setEnabled(emailEnabledCheckbox.isSelected());
            usernameText.setEnabled(emailEnabledCheckbox.isSelected());
            passwordLabel.setEnabled(emailEnabledCheckbox.isSelected());
            passwordText.setEnabled(emailEnabledCheckbox.isSelected());
            showPasswordCheckbox.setEnabled(emailEnabledCheckbox.isSelected());
            passwordStoredCheckbox.setEnabled(emailEnabledCheckbox.isSelected());
        } else if (e.getSource() == showPasswordCheckbox) {
            passwordText.setEchoChar(showPasswordCheckbox.isSelected() ? 0 : echoChar);
        }
    }

    private void fontButtonActionPerformed(ActionEvent e) {
        Pair<Font, Color> pair = FontChooser.showDialog(this, fontLabel.getFont(), fontLabel.getForeground());

        if (pair != null) {
            fontLabel.setFont(pair.getObjectA());
            fontLabel.setForeground(pair.getObjectB());
        }
    }

    private void colorButtonActionPerformed(ActionEvent e) {
        Color color = ColorChooser.showDialog(this, preferences.getDefaultNoteColor());

        if (color != null) {
            colorLabel.setBackground(color);
        }
    }

    private void okButtonActionPerformed(ActionEvent e) {
        preferences.setDefaultFont(fontLabel.getFont());
        preferences.setDefaultFontColor(fontLabel.getForeground());
        preferences.setDefaultNoteColor(colorLabel.getBackground());
        preferences.setHost(hostText.getText());
        preferences.setUsername(usernameText.getText());
        preferences.setPasswordStored(passwordStoredCheckbox.isSelected());
        preferences.setPassword(new String(passwordText.getPassword()));
        preferences.setEmailEnabled(emailEnabledCheckbox.isSelected());
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }

    private void initComponents() {
        ResourceBundle bundle = ResourceBundle.getBundle("jrico.jstickynotes.resource.jstickynotes");
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        buttonsSeparator = new JSeparator();
        okButton = new JButton();
        cancelButton = new JButton();
        preferencesTabbedPane = new JTabbedPane();
        generalScroll = new JScrollPane();
        generalPanel = new JPanel();
        generalDescriptionLabel = new JLabel();
        colorButton = new JButton();
        colorLabel = new JLabel();
        fontButton = new JButton();
        fontLabel = new JLabel();
        CellConstraints cc = new CellConstraints();

        setModal(true);
        setTitle(bundle.getString("PreferencesDialog.this.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        dialogPane.setBorder(Borders.DIALOG_BORDER);
        dialogPane.setLayout(new BorderLayout());

        buttonBar.setBorder(null);
        buttonBar.setLayout(new FormLayout("$glue, $button, $rgap, $button", "$ugap, default, $ugap, pref"));
        buttonBar.add(buttonsSeparator, cc.xywh(1, 2, 4, 1));

        okButton.setText(bundle.getString("PreferencesDialog.okButton.text"));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButtonActionPerformed(e);
            }
        });
        buttonBar.add(okButton, cc.xy(2, 4));

        cancelButton.setText(bundle.getString("PreferencesDialog.cancelButton.text"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed(e);
            }
        });
        buttonBar.add(cancelButton, cc.xy(4, 4));
        dialogPane.add(buttonBar, BorderLayout.SOUTH);

        generalPanel.setBorder(Borders.TABBED_DIALOG_BORDER);
        generalPanel.setLayout(new FormLayout("2*($button, $lcgap), default:grow",
            "default, $ugap, fill:default, $lgap, fill:default"));

        generalDescriptionLabel.setText(bundle.getString("PreferencesDialog.generalDescriptionLabel.text"));
        generalPanel.add(generalDescriptionLabel, cc.xywh(1, 1, 5, 1));

        colorButton.setText(bundle.getString("PreferencesDialog.colorButton.text"));
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colorButtonActionPerformed(e);
            }
        });
        generalPanel.add(colorButton, cc.xy(1, 3));
        generalPanel.add(colorLabel, cc.xy(3, 3));

        fontButton.setText(bundle.getString("PreferencesDialog.fontButton.text"));
        fontButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fontButtonActionPerformed(e);
            }
        });
        generalPanel.add(fontButton, cc.xy(1, 5));

        fontLabel.setText(bundle.getString("PreferencesDialog.fontLabel.text"));
        generalPanel.add(fontLabel, cc.xywh(3, 5, 3, 1));
        generalScroll.setViewportView(generalPanel);
        preferencesTabbedPane.addTab(bundle.getString("PreferencesDialog.generalPanel.tab.title"), generalScroll);

        dialogPane.add(preferencesTabbedPane, BorderLayout.CENTER);
        contentPane.add(dialogPane, BorderLayout.CENTER);

        emailPanel = new JPanel();
        preferencesTabbedPane
            .addTab(bundle.getString("PreferencesDialog.emailPanel.tab.title"), null, emailPanel, null);
        emailPanel.setBorder(Borders.TABBED_DIALOG_BORDER);
        emailPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

        emailDescriptionLabel = new JLabel(bundle.getString("PreferencesDialog.emailDescriptionLabel.text")); //$NON-NLS-1$
        emailPanel.add(emailDescriptionLabel, "1, 1, 5, 1");

        emailEnabledCheckbox = new JCheckBox(bundle.getString("PreferencesDialog.emailEnabledCheckbox.text")); //$NON-NLS-1$
        emailEnabledCheckbox.addChangeListener(this);
        emailPanel.add(emailEnabledCheckbox, "1, 3, 5, 1");

        hostLabel = new JLabel(bundle.getString("PreferencesDialog.hostLabel.text")); //$NON-NLS-1$
        hostLabel.setEnabled(false);
        emailPanel.add(hostLabel, "1, 5, right, default");

        hostText = new JTextField();
        hostText.setEnabled(false);
        emailPanel.add(hostText, "3, 5, 3, 1, fill, default");
        hostText.setColumns(10);

        usernameLabel = new JLabel(bundle.getString("PreferencesDialog.usernameLabel.text")); //$NON-NLS-1$
        usernameLabel.setEnabled(false);
        emailPanel.add(usernameLabel, "1, 7, right, default");

        usernameText = new JTextField();
        usernameText.setEnabled(false);
        emailPanel.add(usernameText, "3, 7, 3, 1, fill, default");
        usernameText.setColumns(10);

        passwordLabel = new JLabel(bundle.getString("PreferencesDialog.passwordLabel.text")); //$NON-NLS-1$
        passwordLabel.setEnabled(false);
        emailPanel.add(passwordLabel, "1, 9, right, default");

        passwordText = new JPasswordField();
        passwordText.setEnabled(false);
        emailPanel.add(passwordText, "3, 9, fill, default");
        passwordText.setColumns(10);
        echoChar = passwordText.getEchoChar();

        showPasswordCheckbox = new JCheckBox(bundle.getString("PreferencesDialog.showPasswordCheckbox.text")); //$NON-NLS-1$
        showPasswordCheckbox.addChangeListener(this);
        showPasswordCheckbox.setEnabled(false);
        emailPanel.add(showPasswordCheckbox, "5, 9");

        passwordStoredCheckbox = new JCheckBox(bundle.getString("PreferencesDialog.passwordStoredCheckbox.text")); //$NON-NLS-1$
        passwordStoredCheckbox.setEnabled(false);
        emailPanel.add(passwordStoredCheckbox, "1, 11, 5, 1");

        colorLabel.setOpaque(true);
        colorLabel.setBackground(preferences.getDefaultNoteColor());
        fontLabel.setFont(preferences.getDefaultFont());
        fontLabel.setForeground(preferences.getDefaultFontColor());
        hostText.setText(preferences.getHost());
        usernameText.setText(preferences.getUsername());
        emailEnabledCheckbox.setSelected(preferences.isEmailEnabled());
        passwordStoredCheckbox.setSelected(preferences.isPasswordStored());

        if (preferences.isPasswordStored()) {
            passwordText.setText(preferences.getPassword());
        }

        pack();
        setLocationRelativeTo(null);
        Widgets.installEscAction(dialogPane, cancelButton, "doClick");
    }
}
