/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer;

//Java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

//FOP
import org.apache.fop.apps.Version;

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
        imageControl1.setIcon(new ImageIcon(getClass().getResource("Images/fop.gif")));
        JLabel label1 = new JLabel(translator.getString("About.Product"));
        JLabel label2 = new JLabel(translator.getString("About.Version") + " " + Version.getVersion());
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

    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    void cancel() {
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            cancel();
        }
    }
}

