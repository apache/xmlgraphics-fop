/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Go to Page Dialog.
 * Originally contributed by:
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */
public class GoToPageDialog extends JDialog {
    private JTextField pgNbField;
    private int pageNumber = -1;

    /**
     * Creates modal dialog with a given title, attached to a given frame.
     */
    public GoToPageDialog(Frame frame, String title, Translator translator) {
        super(frame, title, true);
        jbInit(translator);
        pack();
    }

    private void jbInit(Translator translator) {
        JPanel panel1 = new JPanel();
        GridBagLayout gridBagLayout1 = new GridBagLayout();
        JLabel pgNbLabel = new JLabel();
        pgNbField = new JTextField();
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        panel1.setLayout(gridBagLayout1);
        pgNbLabel.setText(translator.getString("Label.Page.number"));
        okButton.setText(translator.getString("Button.Ok"));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okButton_actionPerformed(e);
            }
        });
        cancelButton.setText(translator.getString("Button.Cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButton_actionPerformed(e);
            }
        });
        panel1.setMinimumSize(new Dimension(250, 78));
        getContentPane().add(panel1);
        panel1.add(pgNbLabel,
                   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(10, 10, 10, 5), 0, 0));
        panel1.add(pgNbField,
                   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.BOTH,
                                          new Insets(10, 5, 10, 10), 0, 0));
        panel1.add(okButton,
                   new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.EAST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 0, 10, 5), 0, 0));
        panel1.add(cancelButton,
                   new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                          GridBagConstraints.WEST,
                                          GridBagConstraints.NONE,
                                          new Insets(0, 10, 10, 10), 0, 0));
    }

    private void okButton_actionPerformed(ActionEvent e) {
        try {
            pageNumber = Integer.parseInt(pgNbField.getText());
            dispose();
        } catch (NumberFormatException nfe) {
            pgNbField.setText("???");
        }

    }

    private void cancelButton_actionPerformed(ActionEvent e) {
        pageNumber = -1;
        dispose();
    }

    /**
     * Returns page number, entered by user.
     */
    public int getPageNumber() {
        return pageNumber;
    }
}

