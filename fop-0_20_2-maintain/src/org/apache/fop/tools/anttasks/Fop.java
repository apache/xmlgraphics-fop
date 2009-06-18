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
package org.apache.fop.tools.anttasks;

// Ant
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

// SAX
import org.xml.sax.XMLReader;

// Java
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

// FOP
import org.apache.fop.messaging.MessageHandler;
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
import org.apache.tools.ant.util.GlobPatternMapper;

/**
 * Wrapper for Fop which allows it to be accessed from within an Ant task.
 * Accepts the inputs:
 * <ul>
 * <li>fofile -> formatting objects file to be transformed</li>
 * <li>format -> MIME type of the format to generate ex. "application/pdf"</li>
 * <li>outfile -> output filename</li>
 * <li>baseDir -> directory to work from</li>
 * <li>userconfig -> file with user configuration (same as the "-c" command line option)</li>
 * <li>messagelevel -> (error | warn | info | verbose | debug) level to output non-error messages</li>
 * <li>logFiles -> Controls whether the names of the files that are processed are logged or not</li>
 * </ul>
 */
public class Fop extends Task {
    
    File foFile;
    List filesets = new java.util.ArrayList();
    File outFile;
    File outDir;
    String format; //MIME type
    File baseDir;
    File userConfig;
    int messageType = Project.MSG_VERBOSE;
    boolean logFiles = true;
    private boolean force = false;

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
     * Set whether to check dependencies, or to always generate;
     * optional, default is false.
     *
     * @param force true if always generate.
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * Gets the force attribute
     * @return the force attribute
     */
    public boolean getForce() {
        return force;
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
        } else if (messageLevel.equalsIgnoreCase("err") || messageLevel.equalsIgnoreCase("error")) {
            messageType = Project.MSG_ERR;
        } else if (messageLevel.equalsIgnoreCase("warn")) {
            messageType = Project.MSG_WARN;
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
     * Controls whether the filenames of the files that are processed are logged
     * or not.
     */
    public void setLogFiles(boolean aBoolean) {
        logFiles = aBoolean;
    }

    public boolean getLogFiles() {
        return logFiles;
    }

    /**
     * Starts execution of this task
     */
    public void execute() throws BuildException {
        int logLevel = ConsoleLogger.LEVEL_INFO;
        switch (getMessageType()) {
            case Project.MSG_DEBUG  : logLevel = ConsoleLogger.LEVEL_DEBUG; break;
            case Project.MSG_INFO   : logLevel = ConsoleLogger.LEVEL_INFO; break;
            case Project.MSG_WARN   : logLevel = ConsoleLogger.LEVEL_WARN; break;
            case Project.MSG_ERR    : logLevel = ConsoleLogger.LEVEL_ERROR; break;
            case Project.MSG_VERBOSE: logLevel = ConsoleLogger.LEVEL_DEBUG; break;
        }
        Logger log = new ConsoleLogger(logLevel);
        MessageHandler.setScreenLogger(log);
        try {
            Starter starter = new FOPTaskStarter(this, log, logFiles);
            starter.run();
        } catch (FOPException ex) {
            throw new BuildException(ex);
        }

    }

}

class FOPTaskStarter extends Starter {
    Fop task;
    Logger log;
    boolean logFiles;

    FOPTaskStarter(Fop task, Logger aLogger, boolean aLogFiles) throws FOPException {
        this.task = task;
        log = aLogger;
        logFiles = aLogFiles;
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
        } else if (format.equalsIgnoreCase("application/vnd.gp-PCL") ||
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
                if (task.getBasedir() != null) {
                    Configuration.put("baseDir",
                                      task.getBasedir().toURL().
                                      toExternalForm());
                } else {
                    Configuration.put("baseDir",
                                      task.getFofile().getParentFile().toURL().
                                      toExternalForm());
                }
            }
            task.log("Using base directory: " +
                     Configuration.getValue("baseDir"), Project.MSG_DEBUG);
        } catch (Exception e) {

            task.log("Error setting base directory: " + e, Project.MSG_ERR);
        }

        int rint = determineRenderer(task.getFormat());
        String newExtension = determineExtension(rint);

        // actioncount = # of fofiles actually processed through FOP
        int actioncount = 0;
        // skippedcount = # of fofiles which haven't changed (force = "false")
        int skippedcount = 0; 

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

                // Render if "force" flag is set OR 
                // OR output file doesn't exist OR
                // output file is older than input file
                if (task.getForce() || !outf.exists() 
                    || (task.getFofile().lastModified() > outf.lastModified() )) {
                    render(task.getFofile(), outf, rint);
                    actioncount++;
                } else if (outf.exists() && (task.getFofile().lastModified() <= outf.lastModified() )) {
                    skippedcount++;
                }
            }
        }

        GlobPatternMapper mapper = new GlobPatternMapper();
        mapper.setFrom("*.fo");
        mapper.setTo("*" + newExtension);

        // deal with the filesets
        for (int i = 0; i < task.filesets.size(); i++) {
            FileSet fs = (FileSet) task.filesets.get(i);
            DirectoryScanner ds = fs.getDirectoryScanner(task.getProject());
            String[] files = ds.getIncludedFiles();


            for (int j = 0; j < files.length; j++) {
                File f = new File(fs.getDir(task.getProject()), files[j]);
                File outf = null;
                if (task.getOutdir() != null && files[j].endsWith(".fo")) {
                  String[] sa = mapper.mapFileName(files[j]);
                  outf = new File(task.getOutdir(), sa[0]);
                } else {
                  outf = replaceExtension(f, ".fo", newExtension);
                  if (task.getOutdir() != null) {
                      outf = new File(task.getOutdir(), outf.getName());
                  }
                }
                try {
                    if (task.getBasedir() != null) {
                        Configuration.put("baseDir",
                                          task.getBasedir().toURL().
                                          toExternalForm());
                    } else {
                        Configuration.put("baseDir",
                                          fs.getDir(task.getProject()).toURL().
                                          toExternalForm());
                    }
                    task.log("Using base directory: " +
                             Configuration.getValue("baseDir"), Project.MSG_DEBUG);
                } catch (Exception e) {
                    task.log("Error setting base directory: " + e, Project.MSG_ERR);
                }

                // Render if "force" flag is set OR 
                // OR output file doesn't exist OR
                // output file is older than input file
                if (task.getForce() || !outf.exists() 
                    || (f.lastModified() > outf.lastModified() )) {
                    render(f, outf, rint);
                    actioncount++;
                } else if (outf.exists() && (f.lastModified() <= outf.lastModified() )) {
                    skippedcount++;
                }
                
            }
        }

        if (actioncount + skippedcount == 0) {
            task.log("No files processed. No files were selected by the filesets "
                + "and no fofile was set." , Project.MSG_WARN);
        } else if (skippedcount > 0) {  
            task.log(skippedcount + " xslfo file(s) skipped (no change found"
                + " since last generation; set force=\"true\" to override)."
                , Project.MSG_INFO);
        }
    }

    private void render(File foFile, File outFile,
                        int renderer) throws FOPException {
        InputHandler inputHandler = new FOInputHandler(foFile);
        XMLReader parser = inputHandler.getParser();

        OutputStream out = null;
        try {
            File dir = outFile.getParentFile();
            dir.mkdirs();
            out = new java.io.FileOutputStream(outFile);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }

        if (logFiles) task.log(foFile + " -> " + outFile, Project.MSG_INFO);

        try {
            Driver driver = new Driver(inputHandler.getInputSource(), out);
            driver.setLogger(log);
            driver.setRenderer(renderer);
            if (renderer == Driver.RENDER_XML) {
                Map rendererOptions = new java.util.HashMap();
                rendererOptions.put("fineDetail", new Boolean(true));
                driver.getRenderer().setOptions(rendererOptions);
            }
            driver.setXMLReader(parser);
            driver.run();
            out.close();
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

}

