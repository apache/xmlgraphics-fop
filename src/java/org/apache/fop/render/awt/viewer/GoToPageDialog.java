/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.render.awt.viewer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
     * @param frame Frame to attach to
     * @param title dialog title
     * @param translator translator for localization
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
                okButtonActionPerformed(e);
            }
        });
        cancelButton.setText(translator.getString("Button.Cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed(e);
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

    private void okButtonActionPerformed(ActionEvent e) {
        try {
            pageNumber = Integer.parseInt(pgNbField.getText());
            dispose();
        } catch (NumberFormatException nfe) {
            pgNbField.setText("???");
        }

    }

    private void cancelButtonActionPerformed(ActionEvent e) {
        pageNumber = -1;
        dispose();
    }

    /**
     * Returns page number, entered by user.
     * @return the page number
     */
    public int getPageNumber() {
        return pageNumber;
    }
}

