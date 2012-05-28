/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.awt.viewer;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.fop.Version;

/**
 * AWT Viewer's "About" dialog.
 * Originally contributed by:
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */
public class PreviewDialogAboutBox extends Dialog implements ActionListener {
    private JButton okButton;

    /**
     * Creates modal "About" dialog, attached to a given parent frame.
     * @param parent parent frame
     * @param translator Translator for localization
     */
    public PreviewDialogAboutBox(Frame parent, Translator translator) {
        super(parent, true);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        setTitle(translator.getString("About.Title"));
        setResizable(false);
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel insetsPanel1 = new JPanel();
        JPanel insetsPanel2 = new JPanel();
        JPanel insetsPanel3 = new JPanel();
        okButton = new JButton();
        JLabel imageControl1 = new JLabel();
        imageControl1.setIcon(new ImageIcon(getClass().getResource("images/fop.gif")));
        JLabel label1 = new JLabel(translator.getString("About.Product"));
        JLabel label2 = new JLabel(translator.getString("About.Version")
                                            + " " + Version.getVersion());
        JLabel label3 = new JLabel(translator.getString("About.Copyright"));
        panel1.setLayout(new BorderLayout());
        panel2.setLayout(new BorderLayout());
        insetsPanel1.setLayout(new FlowLayout());
        insetsPanel2.setLayout(new FlowLayout());
        insetsPanel2.setBorder(new EmptyBorder(10, 10, 10, 10));
        insetsPanel3.setLayout(new GridLayout(3, 1));
        insetsPanel3.setBorder(new EmptyBorder(10, 10, 10, 10));
        okButton.setText(translator.getString("Button.Ok"));
        okButton.addActionListener(this);
        insetsPanel2.add(imageControl1, null);
        panel2.add(insetsPanel2, BorderLayout.WEST);
        insetsPanel3.add(label1);
        insetsPanel3.add(label2);
        insetsPanel3.add(label3);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(okButton);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
        add(panel1);
        pack();
    }

    /**
     * {@inheritDoc}
     */
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    private void cancel() {
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            cancel();
        }
    }
}

