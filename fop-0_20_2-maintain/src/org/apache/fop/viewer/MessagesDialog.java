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
package org.apache.fop.viewer;

import java.io.StringWriter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Die Klasse <code>MessagesDialog</code> dient der Anzeige von Meldungen.
 * Die Klasse erweitert <code>JOptionPane</code> um die Möglichkeit, auf Knopfdruck
 * eine Detailanzeige einzublenden, in der z.B. bei Fehlern der StackTrace ausgegeben
 * werden kann.
 *
 * @author Juergen.Verwohlt@jCatalog.com
 * @version 1.0 09.06.99
 */
public class MessagesDialog extends JOptionPane {

    static Translator res;

    public static void setTranslator(Translator aRes) {
        res = aRes;
        iniConstants();
    }


    static String DETAIL_OPTION;
    static String YES_OPTION;
    static String NO_OPTION;
    static String CANCEL_OPTION;
    static String OK_OPTION;

    static String[] defaultDetailOption;
    static String[] yesNoDetailOption;
    static String[] yesNoCancelDetailOption;
    static String[] okCancelDetailOption;

    static String[] defaultOption;
    static String[] yesNoOption;
    static String[] yesNoCancelOption;
    static String[] okCancelOption;



    private static void iniConstants() {
        DETAIL_OPTION = res.getString("Details");
        YES_OPTION = res.getString("Yes");
        NO_OPTION = res.getString("No");
        CANCEL_OPTION = res.getString("Cancel");
        OK_OPTION = res.getString("Ok");

        defaultDetailOption = new String[] {
            OK_OPTION, DETAIL_OPTION
        };
        yesNoDetailOption = new String[] {
            YES_OPTION, NO_OPTION, DETAIL_OPTION
        };
        yesNoCancelDetailOption = new String[] {
            YES_OPTION, NO_OPTION, CANCEL_OPTION, DETAIL_OPTION
        };
        okCancelDetailOption = new String[] {
            OK_OPTION, CANCEL_OPTION, DETAIL_OPTION
        };

        defaultOption = new String[] {
            OK_OPTION
        };
        yesNoOption = new String[] {
            YES_OPTION, NO_OPTION
        };
        yesNoCancelOption = new String[] {
            YES_OPTION, NO_OPTION, CANCEL_OPTION
        };
        okCancelOption = new String[] {
            OK_OPTION, CANCEL_OPTION
        };
    }


    protected String detailInformation = null;
    protected JDialog dialog = null;
    protected boolean showsDetails = false;

    // MessagesDialog.showConfirmDialog(null,preparedMes,title,
    // optionTypeIndex,messageTypeIndex);

    public MessagesDialog(Object message, int messageType, int optionType,
                          Icon icon, Object[] options, Object initialValue) {
        super(message, messageType, optionType, icon, options, initialValue);
        setMinimumSize(new Dimension(240, 96));
    }

    public static int showConfirmDialog(Component parentComponent,
                                        Object message, String title,
                                        int optionType, int messageType) {
        Object[] options;

        switch (optionType) {
        case JOptionPane.YES_NO_OPTION:
            options = yesNoOption;
            break;
        case JOptionPane.YES_NO_CANCEL_OPTION:
            options = yesNoCancelOption;
            break;
        case JOptionPane.OK_CANCEL_OPTION:
            options = okCancelOption;
            break;
        default:
            options = defaultOption;
        }

        MessagesDialog pane = new MessagesDialog(message, messageType,
                                                 JOptionPane.DEFAULT_OPTION,
                                                 null, options, options[0]);

        pane.setInitialValue(options[0]);

        JDialog dialog = pane.createDialog(parentComponent, title);

        pane.setDialog(dialog);
        pane.selectInitialValue();

        dialog.show();

        Object selectedValue = pane.getValue();

        if (selectedValue == null)
            return CLOSED_OPTION;

        if (selectedValue.equals(OK_OPTION))
            return JOptionPane.OK_OPTION;
        if (selectedValue.equals(CANCEL_OPTION))
            return JOptionPane.CANCEL_OPTION;
        if (selectedValue.equals(YES_OPTION))
            return JOptionPane.YES_OPTION;
        if (selectedValue.equals(NO_OPTION))
            return JOptionPane.NO_OPTION;

        return CLOSED_OPTION;
    }

    /**
     * Öffnet ein Dialogfenster, bei dem zusätzlich zu den spez. Buttons noch ein
     * 'Detail'-Button erscheint. Wird dieser Knopf vom Benutzer betätigt, erscheint
     * die übergebene Detailinformation in einem scrollbaren Bereich des Dialogs.
     */
    public static int showDetailDialog(Component parentComponent,
                                       Object message, String title,
                                       int optionType, int messageType,
                                       Icon icon,
                                       String newDetailInformation) {
        Object[] options;

        switch (optionType) {
        case JOptionPane.YES_NO_OPTION:
            options = yesNoDetailOption;
            break;
        case JOptionPane.YES_NO_CANCEL_OPTION:
            options = yesNoCancelDetailOption;
            break;
        case JOptionPane.OK_CANCEL_OPTION:
            options = okCancelDetailOption;
            break;
        default:
            options = defaultDetailOption;
        }

        MessagesDialog pane = new MessagesDialog(message, messageType,
                                                 JOptionPane.DEFAULT_OPTION,
                                                 icon, options, options[0]);

        pane.setDetailInformation(newDetailInformation);
        pane.setInitialValue(options[0]);

        JDialog dialog = pane.createDialog(parentComponent, title);

        pane.setDialog(dialog);
        pane.selectInitialValue();

        dialog.show();

        Object selectedValue = pane.getValue();

        if (selectedValue == null)
            return CLOSED_OPTION;

        if (((String)selectedValue).equals(DETAIL_OPTION))
            return CLOSED_OPTION;

        if (selectedValue.equals(OK_OPTION))
            return JOptionPane.OK_OPTION;
        if (selectedValue.equals(CANCEL_OPTION))
            return JOptionPane.CANCEL_OPTION;
        if (selectedValue.equals(YES_OPTION))
            return JOptionPane.YES_OPTION;
        if (selectedValue.equals(NO_OPTION))
            return JOptionPane.NO_OPTION;

        return CLOSED_OPTION;
    }

    /**
     * Die Methode fügt in den übergebenen Dialog eine scrollbare Textkomponente ein,
     * in der die Detailinformation angezeigt wird.
     *
     * @param JDialog dialog   Der Dialog, in den die Textkomponente eingefügt werden soll
     */
    protected void displayDetails(JDialog dialog) {
        if (getDetailInformation() != null && dialog != null
                && showsDetails == false) {
            showsDetails = true;
            JScrollPane aScrollPane = new JScrollPane();
            JTextArea aTextArea = new JTextArea();
            StringWriter aStringWriter = new StringWriter();

            aTextArea.setText(getDetailInformation());
            aTextArea.setEditable(false);

            aScrollPane.getViewport().add(aTextArea, null);
            dialog.getContentPane().add(aScrollPane, BorderLayout.SOUTH);
            aScrollPane.setPreferredSize(new Dimension(320, 240));
            dialog.pack();
        }
    }

    // Zugriff

    public void setValue(Object aValue) {
        if (aValue != null && DETAIL_OPTION.equals(aValue))
            displayDetails(getDialog());
        else
            super.setValue(aValue);
    }

    public String getDetailInformation() {
        return detailInformation;
    }

    public void setDetailInformation(String aValue) {
        detailInformation = aValue;
    }

    public JDialog getDialog() {
        return dialog;
    }

    public void setDialog(JDialog aValue) {
        dialog = aValue;
    }

}
