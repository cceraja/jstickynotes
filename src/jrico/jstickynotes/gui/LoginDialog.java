package jrico.jstickynotes.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import jrico.jstickynotes.util.Pair;
import jrico.jstickynotes.util.Screen;
import jrico.jstickynotes.util.Widgets;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class LoginDialog extends JDialog implements ActionListener {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("jrico.jstickynotes.resource.jstickynotes"); //$NON-NLS-1$

    private int option = JOptionPane.NO_OPTION;
    private JTextField usernameText;
    private JPasswordField passwordText;

    /**
     * Create the dialog.
     */
    public LoginDialog() {
        super();
        initComponents();
    }

    private void initComponents() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LoginDialog.this.windowClosing();
            }
        });
        setModal(true);
        setTitle(BUNDLE.getString("LoginDialog.this.title")); //$NON-NLS-1$
        getContentPane().setLayout(new BorderLayout());

        JPanel dialogPanel = new JPanel();
        dialogPanel.setBorder(Borders.DIALOG_BORDER);
        getContentPane().add(dialogPanel, BorderLayout.CENTER);
        dialogPanel.setLayout(new BorderLayout(0, 0));

        JPanel buttonPane = new JPanel();
        dialogPanel.add(buttonPane, BorderLayout.SOUTH);
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
                FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC, },
            new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("25px"), }));

        JSeparator buttonsSeparator = new JSeparator();
        buttonPane.add(buttonsSeparator, "1, 2, 4, 1");

        JButton okButton = new JButton(BUNDLE.getString("LoginDialog.okButton.text")); //$NON-NLS-1$
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "2, 4");
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton(BUNDLE.getString("LoginDialog.cancelButton.text")); //$NON-NLS-1$
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton, "4, 4");

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
                FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

        JLabel messageLabel = new JLabel(BUNDLE.getString("LoginDialog.messageLabel.text")); //$NON-NLS-1$
        contentPanel.add(messageLabel, "1, 1, 3, 1");

        JLabel usernameLabel = new JLabel(BUNDLE.getString("LoginDialog.usernameLabel.text")); //$NON-NLS-1$
        contentPanel.add(usernameLabel, "1, 3, right, default");

        usernameText = new JTextField();
        contentPanel.add(usernameText, "3, 3, fill, default");
        usernameText.setColumns(10);

        JLabel passwordLabel = new JLabel(BUNDLE.getString("LoginDialog.passwordLabel.text")); //$NON-NLS-1$
        contentPanel.add(passwordLabel, "1, 5, right, default");
        dialogPanel.add(contentPanel, BorderLayout.NORTH);

        passwordText = new JPasswordField();
        contentPanel.add(passwordText, "3, 5, fill, default");
        passwordText.setColumns(10);

        Widgets.installEscAction(dialogPanel, cancelButton, "doClick");
        pack();
        usernameText.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        option = "OK".equals(e.getActionCommand()) ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION;
        windowClosing();
    }

    public int getOption() {
        return option;
    }

    public String getUsername() {
        return usernameText.getText();
    }

    public void setUsername(String username) {
        usernameText.setText(username);
    }

    public String getPassword() {
        return new String(passwordText.getPassword());
    }

    public void setPassword(String password) {
        passwordText.setText(password);
    }

    private void windowClosing() {
        dispose();
    }

    public static Pair<String, String> showDialog(String username, String password) {
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.setUsername(username);
        loginDialog.setPassword(password);
        Screen.center(loginDialog);
        loginDialog.setVisible(true);

        return loginDialog.getOption() == JOptionPane.OK_OPTION ? Pair.create(loginDialog.getUsername(),
            loginDialog.getPassword()) : null;
    }
}
