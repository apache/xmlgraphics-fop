/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.anttasks;

// Ant
import org.apache.tools.ant.*;


// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Java
import java.io.*;
import java.util.*;

// FOP
import org.apache.fop.messaging.*;
import org.apache.fop.apps.Starter;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.FOInputHandler;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;


/**
 * Wrapper for Fop which allows it to be accessed from within an Ant task.
 * Accepts the inputs:
 * <ul>
 *   <li>fofile -> formatting objects file to be transformed</li>
 *   <li>pdffile -> output filename</li>
 *   <li>baseDir -> directory to work from</li>
 *   <li>messagelevel -> (info | verbose | debug) level to output non-error messages</li>
 * </ul>
 */
public class Fop extends Task {
    File foFile;
    File pdfFile;
    File baseDir;
    int messageType = Project.MSG_VERBOSE;

    /**
     * Sets the input file
     * @param File to input from
     */
    public void setFofile(File foFile) {
        this.foFile = foFile;
    }

    /**
     * Gets the input file
     */
    public File getFofile() {
        if (foFile == null) {
            log("fofile attribute is not set", Project.MSG_ERR);
            throw new BuildException("fofile attribute is not set");
        }
        return foFile;
    }

    /**
     * Sets the output file
     * @param File to output to
     */
    public void setPdffile(File pdfFile) {
        this.pdfFile = pdfFile;
    }

    /**
     * Sets the output file
     */
    public File getPdffile() {
        if (pdfFile == null) {
            log("pdffile attribute is not set", Project.MSG_ERR);
            throw new BuildException("pdffile attribute is not set");
        }
        return pdfFile;
    }

    /**
     * Sets the message level to be used while processing.
     * @param String (info | verbose | debug)
     */
    public void setMessagelevel(String messageLevel) {
        if (messageLevel.equalsIgnoreCase("info")) {
            messageType = Project.MSG_INFO;
        } else if (messageLevel.equalsIgnoreCase("verbose")) {
            messageType = Project.MSG_VERBOSE;
        } else if (messageLevel.equalsIgnoreCase("debug")) {
            messageType = Project.MSG_DEBUG;
        } else {
            log("messagelevel set to unknown value \"" + messageLevel +
                "\"", Project.MSG_ERR);
            throw new BuildException("unknown messagelevel");
        }
    }

    /**
     * Returns the message type corresponding to Property.MSG_(INFO | VERBOSE | DEBUG)
     * representing the current message level.
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Sets the base directory; currently ignored
     * @param File to use as a working directory
     */
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Gets the base directory
     */
    public File getBasedir() {
        return (baseDir != null) ? baseDir : project.resolveFile(".");
    }

    /**
     * Starts execution of this task
     */
    public void execute () throws BuildException {
        try {
            Starter starter = new FOPTaskStarter(this);
            starter.run();
        } catch (FOPException ex) {
            throw new BuildException(ex);
        }

    }
}

class FOPTaskStarter extends Starter {
    Fop task;
    MessageLogger logger;

    FOPTaskStarter(Fop task) throws FOPException {
        this.task = task;
        MessageHandler.setOutputMethod(MessageHandler.EVENT);
        logger = new MessageLogger(new MessageHandler(), task);
        logger.setMessageLevel(task.getMessageType());
    }

    public void run () throws FOPException {
        try {
            Configuration.put("baseDir", task.getBasedir().toURL().toExternalForm());
        } catch (Exception e) {
            task.log("Error setting base directory", Project.MSG_DEBUG);
        }

        InputHandler inputHandler = new FOInputHandler(task.getFofile());
        XMLReader parser = inputHandler.getParser();
        setParserFeatures(parser);

        FileOutputStream pdfOut = null;
        try {
            pdfOut = new FileOutputStream(task.getPdffile());
        } catch (Exception ex) {
            MessageHandler.errorln("Failed to open " + task.getPdffile());
            throw new BuildException(ex);
        }

        task.log("Using base directory: " +
                 Configuration.getValue("baseDir"), Project.MSG_DEBUG);
        task.log(task.getFofile().getName() + " -> " +
                 task.getPdffile().getName(), Project.MSG_INFO);

        try {
            Driver driver =
              new Driver(inputHandler.getInputSource(), pdfOut);
            driver.setRenderer(Driver.RENDER_PDF);
            driver.setXMLReader(parser);
            driver.run();
        } catch (Exception ex) {
            MessageHandler.logln("Error: " + ex.getMessage());
            throw new BuildException(ex);
        }
        logger.die();
    }
}

class MessageLogger implements MessageListener {
    MessageHandler handler;
    Task task;
    int messageLevel = Project.MSG_VERBOSE;
    int lastMessageLevel = Integer.MIN_VALUE;
    StringBuffer cache = new StringBuffer();
    String breakChars = "\n";
    boolean performCaching = true;

    MessageLogger(MessageHandler handler, Task task) {
        this(handler, task, Project.MSG_VERBOSE);
    }

    MessageLogger(MessageHandler handler, Task task, int messageLevel) {
        this.handler = handler;
        this.task = task;
        setMessageLevel(messageLevel);
        handler.addListener(this);
    }

    public void setMessageLevel(int messageLevel) {
        this.messageLevel = messageLevel;
    }

    public int getMessageLevel() {
        return messageLevel;
    }

    public void processMessage(MessageEvent event) {
        task.log("Logger got message: \"" + event.getMessage() + "\"",
                 Project.MSG_DEBUG);

        boolean flushed = false;

        if (!flushed) {
            int messageLevel;
            if (event.getMessageType() == MessageEvent.ERROR) {
                messageLevel = Project.MSG_ERR;
            } else {
                messageLevel = this.messageLevel;
            }
            if (messageLevel != lastMessageLevel) {
                flush();
                flushed = true;
            }
            lastMessageLevel = messageLevel;
        }

        cache.append(event.getMessage());

        if (!performCaching) {
            flush();
            flushed = true;
        }

        for (int i = 0; !flushed && i < breakChars.length(); i++) {
            if (event.getMessage().lastIndexOf(breakChars.charAt(i)) !=
                    -1) {
                flush();
                flushed = true;
            }
        }
    }

    public void flush() {
        StringTokenizer output =
          new StringTokenizer(cache.toString(), "\n", false);
        while (output.hasMoreElements()) {
            task.log(output.nextElement().toString(), lastMessageLevel);
        }
        cache.setLength(0);
    }

    public void die() {
        flush();
        // because MessageHandler is static this has to be done
        // or you can get duplicate messages if there are
        // multiple <fop> tags in a buildfile
        handler.removeListener(this);
    }
}
