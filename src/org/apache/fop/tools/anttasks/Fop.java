/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.anttasks;

// Ant
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Java
import java.io.*;
import java.util.*;

// FOP
import org.apache.fop.apps.Options;
import org.apache.fop.apps.Starter;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.FOInputHandler;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * Wrapper for Fop which allows it to be accessed from within an Ant task.
 * Accepts the inputs:
 * <ul>
 * <li>fofile -> formatting objects file to be transformed</li>
 * <li>format -> MIME type of the format to generate ex. "application/pdf"</li>
 * <li>outfile -> output filename</li>
 * <li>baseDir -> directory to work from</li>
 * <li>userconfig -> file with user configuration (same as the "-c" command line option)</li>
 * <li>messagelevel -> (info | verbose | debug) level to output non-error messages</li>
 * </ul>
 */
public class Fop extends Task {
    File foFile;
    ArrayList filesets = new ArrayList();
    File outFile;
    File outDir;
    String format; //MIME type
    File baseDir;
    File userConfig;
    int messageType = Project.MSG_VERBOSE;

    /**
     * Sets the input file
     * @param File to input from
     */
    public void setUserconfig (File userConfig) {
        this.userConfig = userConfig;
    }

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
        return foFile;
    }

    /**
     * Adds a set of fo files (nested fileset attribute).
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }

    /**
     * Sets the output file
     * @param File to output to
     */
    public void setOutfile(File outFile) {
        this.outFile = outFile;
    }

    /**
     * Gets the output file
     */
    public File getOutfile() {
        return this.outFile;
    }

    /**
     * Sets the output directory
     * @param Directory to output to
     */
    public void setOutdir(File outDir) {
        this.outDir = outDir;
    }

    /**
     * Gets the output directory
     */
    public File getOutdir() {
        return this.outDir;
    }

    /**
     * Sets output format (MIME type)
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the output format (MIME type)
     */
    public String getFormat() {
        return this.format;
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
    public void execute() throws BuildException {
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
    Logger log;

    FOPTaskStarter(Fop task) throws FOPException {
        this.task = task;

	log = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
    }

    private int determineRenderer(String format) {
        if ((format == null) ||
                format.equalsIgnoreCase("application/pdf") ||
                format.equalsIgnoreCase("pdf")) {
            return Driver.RENDER_PDF;
        } else if (format.equalsIgnoreCase("application/postscript") ||
            format.equalsIgnoreCase("ps")) {
            return Driver.RENDER_PS;
        } else if (format.equalsIgnoreCase("application/vnd.mif") ||
            format.equalsIgnoreCase("mif")) {
            return Driver.RENDER_MIF;
        } else if (format.equalsIgnoreCase("application/vnd.hp-PCL") ||
            format.equalsIgnoreCase("pcl")) {
            return Driver.RENDER_PCL;
        } else if (format.equalsIgnoreCase("text/plain") ||
            format.equalsIgnoreCase("txt")) {
            return Driver.RENDER_TXT;
        } else if (format.equalsIgnoreCase("text/xml") ||
            format.equalsIgnoreCase("at") ||
            format.equalsIgnoreCase("xml")) {
            return Driver.RENDER_XML;
        } else {
            String err = "Couldn't determine renderer to use: "+format;
            log.error(err);
            throw new BuildException(err);
        }
    }

    private String determineExtension(int renderer) {
        switch (renderer) {
            case Driver.RENDER_PDF:
                return ".pdf";
            case Driver.RENDER_PS:
                return ".ps";
            case Driver.RENDER_MIF:
                return ".mif";
            case Driver.RENDER_PCL:
                return ".pcl";
            case Driver.RENDER_TXT:
                return ".txt";
            case Driver.RENDER_XML:
                return ".xml";
            default:
                String err = "Unknown renderer: "+renderer;
                log.error(err);
                throw new BuildException(err);
        }
    }

    private File replaceExtension(File file, String expectedExt,
                                  String newExt) {
        String name = file.getName();
        if (name.toLowerCase().endsWith(expectedExt)) {
            name = name.substring(0, name.length() - expectedExt.length());
        }
        name = name.concat(newExt);
        return new File(file.getParentFile(), name);
    }

    public void run() throws FOPException {
        if (task.userConfig != null) {
            new Options (task.userConfig);
        }

        try {
            if (task.getFofile() != null) {
                Configuration.put("baseDir",
                                  task.getFofile().getParentFile().toURL().
                                  toExternalForm());
            }
        } catch (Exception e) {
            log.error("Error setting base directory",e);
        }

        task.log("Using base directory: " +
                 Configuration.getValue("baseDir"), Project.MSG_DEBUG);

        int rint = determineRenderer(task.getFormat());
        String newExtension = determineExtension(rint);

        int actioncount = 0;

        // deal with single source file
        if (task.getFofile() != null) {
            if (task.getFofile().exists()) {
                File outf = task.getOutfile();
                if (outf == null) {
                    throw new BuildException("outfile is required when fofile is used");
                }
                if (task.getOutdir() != null) {
                    outf = new File(task.getOutdir(), outf.getName());
                }
                render(task.getFofile(), outf, rint);
                actioncount++;
            }
        }

        // deal with the filesets
        for (int i = 0; i < task.filesets.size(); i++) {
            FileSet fs = (FileSet) task.filesets.get(i);
            DirectoryScanner ds = fs.getDirectoryScanner(task.getProject());
            String[] files = ds.getIncludedFiles();

            for (int j = 0; j < files.length; j++) {
                File f = new File(fs.getDir(task.getProject()), files[j]);
                File outf = replaceExtension(f, ".fo", newExtension);
                if (task.getOutdir() != null) {
                    outf = new File(task.getOutdir(), outf.getName());
                }
                try {
                    Configuration.put("baseDir",
                                      fs.getDir(task.getProject()).toURL().
                                      toExternalForm());

                } catch (Exception e) {
                    task.log("Error setting base directory",
                             Project.MSG_DEBUG);
                }

                render(f, outf, rint);
                actioncount++;
            }
        }

        if (actioncount == 0) {
            task.log(
              "No files processed. No files were selected by the filesets and no fofile was set." ,
              Project.MSG_WARN);
        }
    }

    private void render(File foFile, File outFile,
                        int renderer) throws FOPException {
        InputHandler inputHandler = new FOInputHandler(foFile);
        XMLReader parser = inputHandler.getParser();
        setParserFeatures(parser);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
        } catch (Exception ex) {
            log.error("Failed to open " + outFile);
            throw new BuildException(ex);
        }

        task.log(foFile + " -> " + outFile, Project.MSG_INFO);

        try {
            Driver driver = new Driver(inputHandler.getInputSource(), out);
            driver.setLogger(log);
            driver.setRenderer(renderer);
	    if (renderer == Driver.RENDER_XML) {
		HashMap rendererOptions = new HashMap();
		rendererOptions.put("fineDetail", new Boolean(true));
		driver.getRenderer().setOptions(rendererOptions);
	    }
            driver.setXMLReader(parser);
            driver.run();
            out.close();
        } catch (Exception ex) {
            log.error("Couldn't render file: " + ex.getMessage());
            throw new BuildException(ex);
        }
    }

}

