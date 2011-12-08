package jrico.jstickynotes.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import jrico.jstickynotes.util.Screen;
import jrico.jstickynotes.util.Widgets;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class DeleteDialog extends JDialog implements ActionListener {

    private int option = JOptionPane.NO_OPTION;

    /**
     * Create the dialog.
     */
    public DeleteDialog(Window owner) {
        super(owner);
        initComponents();
    }

    private void initComponents() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DeleteDialog.this.windowClosing();
            }
        });
        setResizable(false);
        setModal(true);
        setTitle("JStickyNotes - Delete confirmation");
        getContentPane().setLayout(new BorderLayout());

        JPanel dialogPanel = new JPanel();
        dialogPanel.setBorder(Borders.DIALOG_BORDER);
        getContentPane().add(dialogPanel, BorderLayout.CENTER);
        dialogPanel.setLayout(new BorderLayout(0, 0));

        JPanel buttonPane = new JPanel();
        dialogPanel.add(buttonPane, BorderLayout.SOUTH);
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
                FormFactory.BUTTON_COLSPEC, FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC, },
            new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("25px"), }));

        JSeparator buttonsSeparator = new JSeparator();
        buttonPane.add(buttonsSeparator, "1, 2, 4, 1");

        JButton okButton = new JButton("Yes");
        okButton.addActionListener(this);
        okButton.setActionCommand("Yes");
        buttonPane.add(okButton, "2, 4");
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("No");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("No");
        buttonPane.add(cancelButton, "4, 4");

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("32px"),
                FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
            new RowSpec[] { RowSpec.decode("83px"), }));

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        contentPanel.add(iconLabel, "1, 1");

        JLabel messageLabel = new JLabel("Do you really want to delete the note?");
        contentPanel.add(messageLabel, "3, 1");
        dialogPanel.add(contentPanel, BorderLayout.CENTER);

        Widgets.installEscAction(dialogPanel, cancelButton, "doClick");
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        option = "Yes".equals(e.getActionCommand()) ? JOptionPane.YES_OPTION : JOptionPane.NO_OPTION;
        windowClosing();
    }

    public int getOption() {
        return option;
    }

    private void windowClosing() {
        dispose();
    }

    public static int showDialog(Window owner, Window parent) {
        DeleteDialog deleteDialog = new DeleteDialog(owner);

        if (parent == null) {
            deleteDialog.setLocationRelativeTo(owner);
        } else {
            Screen.locate(parent, deleteDialog);
        }

        deleteDialog.setVisible(true);

        return deleteDialog.getOption();
    }
}
