package jrico.jstickynotes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import jrico.jstickynotes.util.Screen;
import jrico.jstickynotes.util.Widgets;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ColorChooser extends JDialog implements ActionListener {

    private int option = JOptionPane.NO_OPTION;
    private JColorChooser chooser;

    /**
     * Create the dialog.
     */
    public ColorChooser(Window owner) {
        super(owner);
        initComponents();
    }

    private void initComponents() {
        setModal(true);
        setTitle("JStickyNotes - Choose color");
        setBounds(100, 100, 559, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBorder(Borders.DIALOG_BORDER);
        getContentPane().add(dialogPanel, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        dialogPanel.add(buttonPane, BorderLayout.SOUTH);
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"),
                FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.BUTTON_COLSPEC, },
            new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                    FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("25px"), }));

        JSeparator separator = new JSeparator();
        buttonPane.add(separator, "1, 2, 4, 1");

        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "2, 4");
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "4, 4");

        chooser = new JColorChooser();
        dialogPanel.add(chooser, BorderLayout.CENTER);

        Widgets.installEscAction(dialogPanel, cancelButton, "doClick");
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        option = "OK".equals(e.getActionCommand()) ? JOptionPane.OK_OPTION : JOptionPane.NO_OPTION;
        windowClosing();
    }

    public Color getColor() {
        return chooser.getColor();
    }

    public void setColor(Color color) {
        chooser.setColor(color);
    }

    public int getOption() {
        return option;
    }

    private void windowClosing() {
        dispose();
    }

    public static Color showDialog(Window owner, Window parent, Color initialColor) {
        ColorChooser colorChooser = new ColorChooser(owner);
        colorChooser.setColor(initialColor);

        if (parent == null) {
            colorChooser.setLocationRelativeTo(owner);
        } else {
            Screen.locate(parent, colorChooser);
        }

        colorChooser.setVisible(true);
        Color color = null;

        if (colorChooser.getOption() == JOptionPane.OK_OPTION) {
            color = colorChooser.getColor();
        }

        return color;
    }

    public static Color showDialog(Window owner, Color initialColor) {
        return showDialog(owner, null, initialColor);
    }
}
