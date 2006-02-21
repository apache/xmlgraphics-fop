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
package org.apache.fop.messaging;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Vector;
import java.util.Enumeration;

/**
 * The class MessageHandler contains the static methods log and error which
 * should be used for any end user information instead of System.out.print() or
 * System.err.print(). The class defines several output methods:
 * writing to the screen (default), logging to a file, creating message events and repressing all
 * output. If you don't want to change the default behaviour, you should be
 * happy with MessageHandler.log(message) and MessageHandler.error(message)<br>
 * The class MessageHandler also supports the setting of an id. If set every message
 * has as a prefix an identifying string. That way Fop probably can also be used in
 * environments, where more than one Fop instance are running in same  JVM.<br>
 * If Fop is embedded in a gui application or for any reasons the existing
 * messaging system doesn't meet the programmer's requirements, one can add
 * a MessageEvent listener to MessageHandler and handle the incoming messages
 * in an appropriate way. See the class DefaultMessageListener, which is a trivial
 * implementation of the MessageListener.
 * Here is an example how to configure MessageHandler for the DefaultMessageListener (anybody
 * can provide his own listener by extending MessageListener<br>
 * <code>
 * MessageHandler.setOutputMethod(MessageHandler.EVENT);
 * MessageHandler.addListener(new DefaultMessageListener());
 * </code><br>
 * This examples shows, how to redirect the messages to a log file called fop.log.
 * All messages are appended to this file.
 * <code>
 * MessageHandler.setOutputMethod(MessageHandler.FILE);
 * MessageHandler.setLogfileName("\\fop.log",true);
 * </code>
 */

public class MessageHandler {
    public static final int SCREEN = 0;
    public static final int FILE = 1;
    public static final int EVENT = 2;
    public static final int NONE = 3;    // this should always be the last method

    private static Logger logger = null;
    private static String logfileName = "fop.log";
    private static PrintWriter writer;
    private static int outputMethod = SCREEN;
    private static boolean fileOpened = false;
    private static boolean appendToFile = true;
    private static String message = "";
    private static String prefix = "";
    private static Vector listeners = new Vector();
    private static boolean IDisSet = false;
    private static boolean quiet = false;

    /**
     * helper class to access the message
     * @return a string containing the message
     */

    private static String getMessage() {
        return message;
    }

    /**
     * helper class which sets the message
     * and adds a prefix which can contain
     * the id of the thread which uses this messagehandler
     */
    private static void setMessage(String m) {
        if (IDisSet) {
            message = getID() + ":" + m;
        } else {
            message = m;
        }
    }

    /**
     * informs the user of the message
     * @param message the message for the user
     */
    public static void log(String message) {
        if (quiet) {
            return;
        }

        if (logger == null) {
            logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
            logger.warn("Screen logger not set - Using ConsoleLogger.");
        }

        setMessage(message);
        switch (outputMethod) {
        case SCREEN:
            logger.info(getMessage());
            break;
        case FILE:
            if (fileOpened) {
                writer.print(getMessage());
                writer.flush();
            } else {
                openFile();
                writer.print(getMessage());
                writer.flush();
            }
            break;
        case EVENT:
            setMessage(message);
            Enumeration en = listeners.elements();
            while (en.hasMoreElements()) {
                ((MessageListener)en.nextElement()).processMessage(new MessageEvent(getMessage()));
            }
            break;
        case NONE:
            // do nothing
            break;
        default:
            logger.info(message);
        }
    }

    /**
     * convenience method which adds a return to the message
     * @param message the message for the user
     */
    public static void logln(String message) {
        log(message);
    }

    /**
     * error warning for the user
     * @param errorMessage contains the warning string
     */

    public static void error(String errorMessage) {
        if (logger == null) {
            logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
            logger.warn("Screen logger not set - Using ConsoleLogger.");
        }

        setMessage(errorMessage);
        switch (outputMethod) {
        case SCREEN:
            logger.error(getMessage());
            break;
        case FILE:
            if (fileOpened) {
                writer.print(getMessage());
                writer.flush();
            } else {
                openFile();
                writer.print(getMessage());
                writer.flush();
            }
            break;
        case EVENT:
            setMessage(message);
            Enumeration en = listeners.elements();
            while (en.hasMoreElements()) {
                MessageEvent messEv = new MessageEvent(getMessage());
                messEv.setMessageType(MessageEvent.ERROR);
                ((MessageListener)en.nextElement()).processMessage(messEv);
            }
            break;
        case NONE:
            // do nothing
            break;
        default:
            logger.error(errorMessage);
        }
    }

    /**
     * convenience method which adds a return to the error message
     * @param errorMessage the message for the user
     */
    public static void errorln(String errorMessage) {
        error(errorMessage);
    }

    /**
     * adds a MessageListener which listens for MessageEvents
     * @param MessageListener the listener to add
     */
    public static void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    /**
     * removes a MessageListener
     * @param MessageListener the listener to remove
     */
    public static void removeListener(MessageListener listener) {
        listeners.removeElement(listener);
    }

    /**
     * Sets the Logger used for the screen output method.
     * @param newLogger a logger for screen output. This may not be null.
     */
    public static void setScreenLogger(Logger newLogger) {
        if (newLogger == null)
            throw new NullPointerException();
        logger = newLogger;
    }

    /**
     * sets the output method
     * @param method the output method to use, allowed values are<br>
     * MessageHandler.SCREEN, MessageHandler.FILE, MessageHandler.EVENT
     * MessageHandler.NONE
     */
    public static void setOutputMethod(int method) {
        if (method > NONE) {
            MessageHandler.error("Error: Unknown output method");
        } else {
            outputMethod = method;
        }
    }

    /**
     * informs what output method is set
     * @return the output method
     */
    public static int getOutputMethod() {
        return outputMethod;
    }

    /**
     * sets the logfile name
     * @param filename name of the logfile
     * @param append if true, the logfile is appended
     */
    public static void setLogfileName(String filename, boolean append) {
        logfileName = filename;
        appendToFile = append;
    }

    /**
     * returns the logfile name
     * @return String containing the logfile name
     */
    public static String getLogfileName() {
        return logfileName;
    }

    /**
     * helper file which opens the file for output method FILE
     */
    private static void openFile() {
        try {
            writer =
                new PrintWriter(new FileWriter(logfileName, appendToFile),
                                true);
            writer.println("\n==============================================");
            fileOpened = true;
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe);
        }
    }

    /**
     * if set to true an id string is prefixed to every message
     * uses the thread info as an id for the message producer. Should be used if
     * more than one instance of Fop is running in the same JVM
     * this id becomes a prefix to every message
     */
    private static String getID() {
        return Thread.currentThread().toString();
    }

    /**
     * if set to true an id string is prefixed to every message
     * uses the thread info as an id for the message producer. Should be used if
     * more than one instance of Fop is running in the same JVM
     * this id becomes a prefix to every message
     *
     * @param id boolean (default is false)
     */

    public static void setID(boolean id) {
        IDisSet = id;
    }

    /**
     * if set to true all normal messages are suppressed.
     * error messages are displayed allthesame
     *
     * @param quietMode boolean (default is false)
     */
    public static void setQuiet(boolean quietMode) {
        quiet = quietMode;
    }

}
