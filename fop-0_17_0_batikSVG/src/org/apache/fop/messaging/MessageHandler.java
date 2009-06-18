/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */


package org.apache.fop.messaging;

import java.io.*;
import java.util.*;


/** The class MessageHandler contains the static methods log and error which
 *  should be used for any end user information instead of System.out.print() or
 *  System.err.print(). The class defines several output methods:
 *  writing to the screen (default), logging to a file, creating message events and repressing all
 *  output. If you don't want to change the default behaviour, you should be
 *  happy with MessageHandler.log(message) and MessageHandler.error(message)<br>
 *  The class MessageHandler also supports the setting of an id. If set every message
 *  has as a prefix an identifying string. That way Fop probably can also be used in
 *  environments, where more than one Fop instance are running in same  JVM.<br>
 *  If Fop is embedded in a gui application or for any reasons the existing
 *  messaging system doesn't meet the programmer's requirements, one can add
 *  a MessageEvent listener to MessageHandler and handle the incoming messages
 *  in an appropriate way. See the class DefaultMessageListener, which is a trivial
 *  implementation of the MessageListener.
 *  Here is an example how to configure MessageHandler for the DefaultMessageListener (anybody
 *  can provide his own listener by extending MessageListener<br>
 *  <code>
 *  MessageHandler.setOutputMethod(MessageHandler.EVENT);
 *  MessageHandler.addListener(new DefaultMessageListener());
 *  </code><br>
 *  This examples shows, how to redirect the messages to a log file called fop.log.
 *  All messages are appended to this file.
 *  <code>
 *   MessageHandler.setOutputMethod(MessageHandler.FILE);
 *   MessageHandler.setLogfileName("\\fop.log",true);
 *  </code>
 */

public class MessageHandler {
    public static final int SCREEN = 0;
    public static final int FILE = 1;
    public static final int EVENT = 2;
    public static final int NONE = 3; //this should always be the last method

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
     *  helper class to access the message
     *  @return a string containing the message
     */

    private static String getMessage () {
        return message;
    }

    /**
      *  helper class which sets the message
      *  and adds a prefix which can contain
      *  the id of the thread which uses this messagehandler
      */
    private static void setMessage (String m) {
        if (IDisSet) {
            message = getID() + ":" + m;
        } else {
            message = m;
        }
    }

    /**
      *  informs the user of the message
      *  @param message the message for the user
      */
    public static void log (String message) {
        if (!quiet) {
	        setMessage(message);
            switch (outputMethod) {
                case SCREEN:
                    System.out.print(getMessage ());
                    break;
                case FILE:
                    if (fileOpened) {
                        writer.print(getMessage ());
                        writer.flush();
                    } else {
                        openFile();
                        writer.print(getMessage ());
                        writer.flush();
                    }
                    break;
                case EVENT:
                    setMessage(message);
                    Enumeration enum = listeners.elements();
                    while (enum.hasMoreElements()) {
                        ((MessageListener) enum.nextElement()).
                        processMessage(
                          new MessageEvent(getMessage()));
                    }
                    break;
                case NONE:
                    //do nothing
                    break;
                default:
                    System.out.print(message);
            }
        }
    }

    /**
      *  convenience method which adds a return to the message
      *  @param message the message for the user
      */
    public static void logln (String message) {
        log (message + "\n");
    }

    /**
      *  error warning for the user
      *  @param errorMessage contains the warning string
      */

    public static void error (String errorMessage) {
        setMessage(errorMessage);
        switch (outputMethod) {
            case SCREEN:
                System.err.print(getMessage());
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
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements()) {
                    MessageEvent messEv = new MessageEvent(getMessage());
                    messEv.setMessageType(MessageEvent.ERROR);
                    ((MessageListener) enum.nextElement()).
                    processMessage(messEv);
                }
                break;
            case NONE:
                //do nothing
                break;
            default:
                System.err.print(errorMessage);
        }
    }

    /**
      *  convenience method which adds a return to the error message
      *  @param errorMessage the message for the user
      */
    public static void errorln (String errorMessage) {
        error (errorMessage + "\n");
    }

    /**
      *  adds a MessageListener which listens for MessageEvents
      *  @param MessageListener the listener to add
      */
    public static void addListener(MessageListener listener) {
        listeners.addElement(listener);
    }

    /**
      *  removes a MessageListener
      *  @param MessageListener the listener to remove
      */
    public static void removeListener(MessageListener listener) {
        listeners.removeElement(listener);
    }

    /**
      *  sets the output method
      *  @param method the output method to use, allowed values are<br>
      *  MessageHandler.SCREEN, MessageHandler.FILE, MessageHandler.EVENT
      *  MessageHandler.NONE
      */
    public static void setOutputMethod (int method) {
        if (method > NONE) {
            MessageHandler.error("Error: Unknown output method");
        } else {
            outputMethod = method;
        }
    }

    /**
      *  informs what output method is set
      *  @return the output method
      */
    public static int getOutputMethod () {
        return outputMethod;
    }

    /**
      *  sets the logfile name
      *  @param filename name of the logfile
      *  @param append if true, the logfile is appended
      */
    public static void setLogfileName (String filename, boolean append) {
        logfileName = filename;
        appendToFile = append;
    }

    /**
      *  returns the logfile name
      *  @return String containing the logfile name
      */
    public static String getLogfileName () {
        return logfileName;
    }

    /**
      *  helper file which opens the file for output method FILE
      */
    private static void openFile () {
        try {
            writer = new PrintWriter (
                       new FileWriter (logfileName, appendToFile), true);
            writer.println("\n==============================================");
            fileOpened = true;
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe);
        }
    }

    /**
      *  if set to true an id string is prefixed to every message
      *  uses the thread info as an id for the message producer. Should be used if
      *  more than one instance of Fop is running in the same JVM
      *  this id becomes a prefix to every message
      */
    private static String getID () {
        return Thread.currentThread().toString();
    }

    /**
      * if set to true an id string is prefixed to every message
      *  uses the thread info as an id for the message producer. Should be used if
      *  more than one instance of Fop is running in the same JVM
      *  this id becomes a prefix to every message
      *
      *  @param id boolean (default is false)
      */

    public static void setID (boolean id) {
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
