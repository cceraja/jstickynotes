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

package jrico.jstickynotes.model;

import java.awt.Color;
import java.awt.Font;

import jrico.jstickynotes.util.Bean;

public class Preferences extends Bean {

    public static final Color DEFAULT_COLOR = new Color(0xfffaaa);

    /*
     * Properties
     */

    public static final String DEFAULT_COLOR_PROPERTY = "defaultNoteColor";
    public static final String FONT_PROPERTY = "defaultFont";
    public static final String FONT_COLOR_PROPERTY = "defaultFontColor";
    public static final String HOST_PROPERTY = "host";
    public static final String USERNAME_PROPERTY = "username";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String EMAIL_ENABLED_PROPERTY = "emailEnabled";
    public static final String PASSWORD_STORED_PROPERTY = "passwordStored";
    public static final String AUTO_LOGIN_PROPERTY = "autoLogin";

    private Color defaultNoteColor;

    private Font defaultFont;

    private Color defaultFontColor;

    private String host;

    private String username;

    private String password;

    private boolean emailEnabled;

    private boolean passwordStored;

    private boolean autoLogin;

    /**
     * @return the defaultNoteColor
     */
    public Color getDefaultNoteColor() {
        return defaultNoteColor;
    }

    /**
     * @param defaultNoteColor
     *            the defaultNoteColor to set
     */
    public void setDefaultNoteColor(Color defaultNoteColor) {
        Color oldDefaultColor = getDefaultNoteColor();
        this.defaultNoteColor = defaultNoteColor;
        getNotifier().firePropertyChange(DEFAULT_COLOR_PROPERTY, oldDefaultColor, defaultNoteColor);
    }

    /**
     * @return the defaultFont
     */
    public Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * @param defaultFont
     *            the defaultFont to set
     */
    public void setDefaultFont(Font defaultFont) {
        Font oldDefaultFont = getDefaultFont();
        this.defaultFont = defaultFont;
        getNotifier().firePropertyChange(FONT_PROPERTY, oldDefaultFont, defaultFont);
    }

    /**
     * @return the defaultFontColor
     */
    public Color getDefaultFontColor() {
        return defaultFontColor;
    }

    /**
     * @param defaultFontColor
     *            the defaultFontColor to set
     */
    public void setDefaultFontColor(Color defaultFontColor) {
        Color oldFontColor = getDefaultFontColor();
        this.defaultFontColor = defaultFontColor;
        getNotifier().firePropertyChange(FONT_COLOR_PROPERTY, oldFontColor, defaultFontColor);
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host
     *            the host to set
     */
    public void setHost(String host) {
        String oldHost = getHost();
        this.host = host;
        getNotifier().firePropertyChange(HOST_PROPERTY, oldHost, host);
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        String oldUsername = getUsername();
        this.username = username;
        getNotifier().firePropertyChange(USERNAME_PROPERTY, oldUsername, username);
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        String oldPassword = getPassword();
        this.password = password;
        getNotifier().firePropertyChange(PASSWORD_PROPERTY, oldPassword, password);
    }

    /**
     * @return the emailEnabled
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    /**
     * @param emailEnabled
     *            the emailEnabled to set
     */
    public void setEmailEnabled(boolean emailEnabled) {
        boolean oldEmailEnabled = isEmailEnabled();
        this.emailEnabled = emailEnabled;
        getNotifier().firePropertyChange(EMAIL_ENABLED_PROPERTY, oldEmailEnabled, emailEnabled);
    }

    /**
     * @return the passwordStored
     */
    public boolean isPasswordStored() {
        return passwordStored;
    }

    /**
     * @param passwordStored
     *            the passwordStored to set
     */
    public void setPasswordStored(boolean passwordStored) {
        boolean oldPasswordStored = isPasswordStored();
        this.passwordStored = passwordStored;
        getNotifier().firePropertyChange(PASSWORD_STORED_PROPERTY, oldPasswordStored, passwordStored);
    }

    /**
     * @return the autoLogin config
     */
    public boolean isAutoLogin() {
        return autoLogin;
    }

    /**
     * @param autoLogin
     *            whether the auto-login is enabled at startup or not
     */
    public void setAutoLogin(boolean autoLogin) {
        boolean oldAutoLogin = this.isAutoLogin();
        this.autoLogin = autoLogin;
        getNotifier().firePropertyChange(AUTO_LOGIN_PROPERTY, oldAutoLogin, autoLogin);
    }

}
