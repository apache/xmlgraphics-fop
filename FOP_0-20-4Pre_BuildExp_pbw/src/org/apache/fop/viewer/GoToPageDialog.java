/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

package org.apache.fop.viewer;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class GoToPageDialog extends JDialog {
    JPanel panel1 = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel pgNbLabel = new JLabel();
    JTextField pgNbField = new JTextField();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();

    int pageNumber = -1;

    public GoToPageDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            //log.error("GoToPageDialog: Konstruktor: "
            //                       + ex.getMessage(), ex);
        }
    }

    public GoToPageDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        panel1.setLayout(gridBagLayout1);
        pgNbLabel.setText("Page number");
        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okButton_actionPerformed(e);
            }

        });
        cancelButton.setText("Cancel");
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

    void okButton_actionPerformed(ActionEvent e) {
        try {
            pageNumber = Integer.parseInt(pgNbField.getText());
            dispose();
        } catch (Exception ex) {
            pgNbField.setText("???");
        }

    }

    void cancelButton_actionPerformed(ActionEvent e) {
        pageNumber = -1;
        dispose();
    }

    public int getPageNumber() {
        return pageNumber;
    }

}
