/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JComponent.*;



/**
 * Klasse <code>UserMessage</code> ist ein utility zum Abfragen oder zum Informieren des Benutzers.<br>
 * Eine Meldung besteht aus dem Identifikator (Suchschlüssel im Meldungspool), einem Dialogtitel, einem Buttonset und
 * dem Meldungstext mit eventuellen Platzhaltern für die Parameter.
 *
 * @author S. Gorkhover
 * @version 18.03.1999
 *
 * @changed 23.04.99 Juergen.Verwohlt@jCatalog.com
 * @subject Weitere Ausgabemethoden: show(String, String) und show(String,String,Frame)
 *
 * @changed 28.05.99 Juergen.Verwohlt@jCatalog.com
 * @subject MessageException unterstützen
 *
 * @changed 09.06.99 Juergen.Verwohlt@jCatalog.com
 * @subject Neue Klasse MessagesException zur Anzeige verwenden
 *
 * @changed 10.12.00 gears@apache.org
 * @subject Unicode
 */
public class UserMessage {

    private static Translator res = null;

    public static void setTranslator(Translator aRes) {
        res = aRes;
        if (res == null) {
            //log.debug("UserMessage: setTranslator(null) !");
            res = new SecureResourceBundle(null);
        }

        MessagesDialog.setTranslator(res);
    }


    // Zulässige Werte für Dialogart:

    /**
     * Möglicher Wert des Meldungstypes. Fenster-Title "Info" (de).
     */
    private static final int INFO = JOptionPane.PLAIN_MESSAGE;

    /**
     * Möglicher Wert des Meldungstypes. Fenster-Title "Warnung" (de).
     */
    private static final int WARNING = JOptionPane.WARNING_MESSAGE;

    /**
     * Möglicher Wert des Meldungstypes. Fenster-Title "Frage" (de).
     */
    private static final int QUESTION = JOptionPane.QUESTION_MESSAGE;

    /**
     * Möglicher Wert des Meldungstypes. Fenster-Title "Fehler" (de).
     */
    private static final int ERROR = JOptionPane.ERROR_MESSAGE;

    /**
     * Möglicher Wert des Meldungstypes. Fenster-Title "Systemfehler" (de).
     */
    private static final int SYS_ERROR = JOptionPane.ERROR_MESSAGE;



    /*
     * Style Constanten orientieren sich auf die
     * Constanten der ButtonDialog-Klasse und legen das Dialog-ButtonSet fest.
     */

    /**
     * Wert für Setzten keines Buttons
     */
    public static final int STYLE_NOBUTTON = -2;

    /**
     * Wert für Setzten von nur Ja-Button.
     */
    public static final int STYLE_Y = JOptionPane.DEFAULT_OPTION;
    // ButtonDialog.YES;            // = 1

    /**
     * Wert für Setzten von Ja- und Nein-Buttons.
     */
    public static final int STYLE_Y_N = JOptionPane.YES_NO_OPTION;
    // ButtonDialog.YES_NO;         // = 2;

    /**
     * Wert für Setzten von Ja-, Nein-und Abbruch Buttons.
     */
    public static final int STYLE_Y_N_C = JOptionPane.YES_NO_CANCEL_OPTION;
    // ButtonDialog.YES_NO_CANCEL;  // = 3;


    // Platzhalter für Parameter in der Properties-Datei
    private static final String PARAMETER_TAG = "&&&";


    /**
     * Benutzer-Antwort-Constanten orientieren sich auf die
     * Constanten der ButtonDialog-Klasse
     */

    /**
     * Möglicher Rückgabewert @see ButtonDialog
     */
    public static final int YES = JOptionPane.YES_OPTION;
    // ButtonDialog.YES;            // = 2

    /**
     * Möglicher Rückgabewert @see ButtonDialog
     */
    public static final int NO = JOptionPane.NO_OPTION;
    // ButtonDialog.NO;            // = 4;

    /**
     * Möglicher Rückgabewert @see ButtonDialog
     */
    public static final int CANCEL = JOptionPane.CANCEL_OPTION;
    // ButtonDialog.CANCEL;        // = 8;


    // Default-Values
    private static int buttonType = STYLE_Y;
    private static int iconType = WARNING;
    private static String currentIconName = "";
    private static String actMessId = null;
    // private static MessagesDialog nobuttonDialog = null;


    /**
     * Ersetzt die eventuellen Platzhalter durch die übergebenen Parameter
     */
    static String prepareMessage(String rawText, String[] par) {
        //log.debug("prepareMessage(): " + rawText + ", parameter: "
        //                     + par);
        int index = rawText.indexOf(PARAMETER_TAG);
        String composedMess = "";
        if ((index == -1) && (par == null))
            return rawText;
        if ((index != -1) && (par == null)) {
            //log.debug("Message " + actMessId
            //                     + " erwartet Parameter. Aufgerufen ohne Parameter");
            return rawText;
        }
        if ((index == -1) && (par != null)) {
            //log.debug("Message " + actMessId
            //                     + " erwartet keine Parameter. Aufgerufen mit folgenden Parametern:");
            for (int i = 0; i < par.length; ++i) {
                //log.debug(par[i].toString());
            }
            return rawText;
        }
        int tagCount = 0;
        while (rawText.indexOf(PARAMETER_TAG) != -1) {
            index = rawText.indexOf(PARAMETER_TAG);
            try {
                composedMess += rawText.substring(0, index) + par[tagCount];
            } catch (ArrayIndexOutOfBoundsException ex) {
                //log.error("Anzahl der übergebenen Parameter zu der Meldung "
                //                     + actMessId
                //                     + " ist weniger als erwartet.", ex);
                return composedMess + rawText;
            }
            rawText = rawText.substring(index + PARAMETER_TAG.length());
            tagCount++;
        }
        composedMess += rawText;
        if (tagCount != par.length) {
            //log.debug("Die zu der Meldung " + actMessId
            //                     + "  übergebenen Parameter sind mehr als die Meldung vorsieht.");
        }
        return composedMess;
    }

    /**
     * Gibt den Title fürs Dialogfenster.
     * Dieser wird durch die Dialogart festgelegt
     * (erster Teil in der MeldungsDefinition in der Properties-Datei).
     */
    static String getTitle(String strVal) {
        String title = null;
        int choice = getValue(strVal);
        switch (choice) {
        case INFO:
            title = "Info";
            currentIconName = "info.gif";
            break;
        case WARNING:
            title = "Warning";
            currentIconName = "warn.gif";
            break;
        case QUESTION:
            title = "Question";
            currentIconName = "quest.gif";
            break;
        case ERROR:
            title = "Error";
            currentIconName = "error.gif";
            break;
        default:
            title =
                "Ungültiger IonType für diese Meldung. Prüfen in Übersetzungsdatei.";
        }
        return title;
    }

    /**
     * Liefert den Wert der über den Nemen übergebenen Klassenvariablen.
     */
    static int getValue(String fieldName) {

        int val = -1;
        if (fieldName.equals("INFO"))
            return INFO;
        else if (fieldName.equals("WARNING"))
            return WARNING;
        else if (fieldName.equals("ERROR"))
            return ERROR;
        else if (fieldName.equals("SYS_ERROR"))
            return SYS_ERROR;
        else if (fieldName.equals("QUESTION"))
            return QUESTION;
        else if (fieldName.equals("STYLE_NOBUTTON"))
            return STYLE_NOBUTTON;
        else if (fieldName.equals("STYLE_Y"))
            return STYLE_Y;
        else if (fieldName.equals("STYLE_Y_N"))
            return STYLE_Y_N;
        else if (fieldName.equals("STYLE_Y_N_C"))
            return STYLE_Y_N_C;
        return val;
    }

    private static String getStackTrace(Exception exception) {

        if (exception == null)
            return "null";

        String stack = "";

        StringWriter strWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(strWriter));
        stack = strWriter.toString();
        stack = stack.replace('\r', ' ');
        stack = stack.replace('\t', ' ');
        return stack;
    }

    private static int display(String textID, String[] param,
                               Exception exception, Frame frame) {

        String translatedMes = "";
        String preparedMes = "";
        String messageType = "";
        String optionType = "";
        String title = "";
        MessagesDialog dialog = null;
        actMessId = textID;

        boolean messageFound = res.contains(textID);
        translatedMes = res.getString(textID);

        if (messageFound) {
            try {
                messageType =
                    translatedMes.substring(0, translatedMes.indexOf(':'));
                translatedMes =
                    translatedMes.substring(translatedMes.indexOf(':') + 1);

                optionType =
                    translatedMes.substring(0, translatedMes.indexOf(':'));
                translatedMes =
                    translatedMes.substring(translatedMes.indexOf(':') + 1);

            } catch (Exception ex) {
                //log.debug("FALSCHES FORMAT: MESSAGE: " + textID);
            }
        } else {    // Message not found
            //log.debug("UserMessage: textID '" + textID
            //                     + "' not found. Return "
            //                     + "value 'CANCEL' = " + CANCEL);

            // return CANCEL;

            messageType = "ERROR";
            optionType = "STYLE_Y";
            translatedMes = "textID '" + textID + "' not found."
                            + "\nIt is possible the message file not found.";
        }

        preparedMes = prepareMessage(translatedMes, param);
        // Exception exception = getException(param);

        // WARNING -> nach Warnung übersetzen
        title = res.getString(getTitle(messageType));

        // WARNING -> JOptionPane.WARNING_MESSAGE
        int messageTypeIndex = getValue(messageType);

        // Button Set idetifizieren
        int optionTypeIndex = getValue(optionType);

        int result = CANCEL;

        if (exception != null) {
            String str = getStackTrace(exception);
            if (exception instanceof MessageException) {
                MessageException ex = (MessageException)exception;
                if (ex.getException() != null)
                    str += "\n" + getStackTrace(ex.getException());
            }
            result = MessagesDialog.showDetailDialog(null, preparedMes,
                                                     title, optionTypeIndex,
                                                     messageTypeIndex, null,
                                                     str);
        } else {
            if (optionTypeIndex == STYLE_NOBUTTON) {
                // Wird nicht mehr unterstützt
                //log.debug("UserMessage: STYLE_NOBUTTON wird nicht unterstützt");
                return result;
            } else {
                result = MessagesDialog.showConfirmDialog(null, preparedMes,
                                                          title,
                                                          optionTypeIndex,
                                                          messageTypeIndex);
            }
        }
        return result;
    }

    /**
     * Öffnet das Dialogfenster mit der Übersetzung der per Suchschlüssel übergebenen Meldung
     * mit eingesetzten Parametern. Für die Übersetzung der Parameter trägt die aufrufende Stelle die Sorge.
     * Der Dialog ist modal zum Frame <code>frame</code>.
     * @param <UL>
     * <LI> textID - Suchschlüssel der Meldung im Meldungspool,
     * <LI> param - Array der in die Meldung einzusetztenden Parameter,
     * <LI> frame - das Fenster, zu dem der Dialog modal ist.
     * </UL>
     * @return <UL>
     * <LI> -1 wenn keine Rückgabe möglich ist oder Dialog NICHT MODAL ist.
     * <LI> ButtonDialog.YES = 2
     * <LI> ButtonDialog.NO = 4
     * <LI> ButtonDialog.CANCEL = 8
     * <LI> Wird das Dialog-Fenster ohne Buttonklick geschlossen (Kreuzchen oben rechts), so ist die Rückgabe gleich ButtonDialog.CANCEL.
     * </UL>
     */
    public static int show(String messageId, String[] parameterList,
                           Exception anException, Frame parentFrame) {
        return display(messageId, parameterList, anException, parentFrame);
    }

    public static int show(String messageId, String[] parameterList,
                           Exception anException) {
        return display(messageId, parameterList, anException, (Frame)null);
    }

    public static int show(String messageId, String[] parameterList,
                           Frame parentFrame) {
        return display(messageId, parameterList, (Exception)null,
                       parentFrame);
    }

    public static int show(String messageId, String[] parameterList) {
        return display(messageId, parameterList, (Exception)null,
                       (Frame)null);
    }

    public static int show(String messageId, String parameter,
                           Frame parentFrame) {
        return display(messageId, new String[] {
            parameter
        }, (Exception)null, parentFrame);
    }

    public static int show(String messageId, String parameter) {
        return display(messageId, new String[] {
            parameter
        }, (Exception)null, (Frame)null);
    }

    public static int show(String messageId, Frame parentFrame) {
        return display(messageId, (String[])null, (Exception)null,
                       parentFrame);
    }

    public static int show(String messageId) {
        return display(messageId, (String[])null, (Exception)null,
                       (Frame)null);
    }

    public static int show(String messageId, Exception anException,
                           Frame aFrame) {
        return display(messageId, (String[])null, anException, aFrame);
    }

    public static int show(String messageId, Exception anException) {
        return display(messageId, (String[])null, anException, (Frame)null);
    }

    public static int show(Exception anException, Frame aFrame) {
        if (anException instanceof MessageException) {
            MessageException ex = (MessageException)anException;
            return show(ex.getMessageId(), ex.getParameterList(), ex, aFrame);
        } else
            return show("UNHANDLED_EXCEPTION", (String[])null, anException,
                        aFrame);
    }

}

