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
import org.apache.tools.ant.util.GlobPatternMapper;

// SAX
import org.xml.sax.XMLReader;

// Java
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

// FOP
import org.apache.fop.apps.Starter;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.FOInputHandler;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOUserAgent;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * Wrapper for FOP which allows it to be accessed from within an Ant task.
 * Accepts the inputs:
 * <ul>
 * <li>fofile -> formatting objects file to be transformed</li>
 * <li>format -> MIME type of the format to generate ex. "application/pdf"</li>
 * <li>outfile -> output filename</li>
 * <li>baseDir -> directory to work from</li>
 * <li>userconfig -> file with user configuration (same as the "-c" command 
 *      line option)</li>
 * <li>messagelevel -> (error | warn | info | verbose | debug) level to output 
 *      non-error messages</li>
 * <li>logFiles -> Controls whether the names of the files that are processed 
 *      are logged or not</li>
 * </ul>
 */
public class Fop extends Task {
    
    private File foFile;
    private List filesets = new java.util.ArrayList();
    private File outFile;
    private File outDir;
    private String format; //MIME type
    private File baseDir;
    private File userConfig;
    private int messageType = Project.MSG_VERBOSE;
    private boolean logFiles = true;

    /**
     * Sets the filename for the userconfig.xml.
     * @param userConfig Configuration to use
     */
    public void setUserconfig(File userConfig) {
        this.userConfig = userConfig;
    }

    /**
     * Returns the file for the userconfig.xml.
     * @return the userconfig.xml file
     */
    public File getUserconfig() {
        return this.userConfig;
    }

    /**
     * Sets the input XSL-FO file.
     * @param foFile input XSL-FO file
     */
    public void setFofile(File foFile) {
        this.foFile = foFile;
    }

    /**
     * Gets the input XSL-FO file.
     * @return input XSL-FO file
     */
    public File getFofile() {
        return foFile;
    }

    /**
     * Adds a set of XSL-FO files (nested fileset attribute).
     * @param set a fileset
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }
    
    /**
     * Returns the current list of filesets.
     * @return the filesets
     */
    public List getFilesets() {
        return this.filesets;
    }

    /**
     * Sets the output file.
     * @param outFile File to output to
     */
    public void setOutfile(File outFile) {
        this.outFile = outFile;
    }

    /**
     * Gets the output file.
     * @return the output file
     */
    public File getOutfile() {
        return this.outFile;
    }

    /**
     * Sets the output directory.
     * @param outDir Directory to output to
     */
    public void setOutdir(File outDir) {
        this.outDir = outDir;
    }

    /**
     * Gets the output directory.
     * @return the output directory
     */
    public File getOutdir() {
        return this.outDir;
    }

    /**
     * Sets output format (MIME type).
     * @param format the output format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the output format (MIME type).
     * @return the output format
     */
    public String getFormat() {
        return this.format;
    }

    /**
     * Sets the message level to be used while processing.
     * @param messageLevel (error | warn| info | verbose | debug)
     */
    public void setMessagelevel(String messageLevel) {
        if (messageLevel.equalsIgnoreCase("info")) {
            messageType = Project.MSG_INFO;
        } else if (messageLevel.equalsIgnoreCase("verbose")) {
            messageType = Project.MSG_VERBOSE;
        } else if (messageLevel.equalsIgnoreCase("debug")) {
            messageType = Project.MSG_DEBUG;
        } else if (messageLevel.equalsIgnoreCase("err") 
                 || messageLevel.equalsIgnoreCase("error")) {
            messageType = Project.MSG_ERR;
        } else if (messageLevel.equalsIgnoreCase("warn")) {
            messageType = Project.MSG_WARN;
        } else {
            log("messagelevel set to unknown value \"" + messageLevel 
                + "\"", Project.MSG_ERR);
            throw new BuildException("unknown messagelevel");
        }
    }

    /**
     * Returns the message type corresponding to Project.MSG_*
     * representing the current message level.
     * @see org.apache.tools.ant.Project
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Sets the base directory; currently ignored.
     * @param baseDir File to use as a working directory
     */
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Gets the base directory.
     * @return the base directory
     */
    public File getBasedir() {
        return (baseDir != null) ? baseDir : project.resolveFile(".");
    }

    /**
     * Controls whether the filenames of the files that are processed are logged
     * or not.
     * @param logFiles True if the feature should be enabled 
     */
    public void setLogFiles(boolean logFiles) {
        this.logFiles = logFiles;
    }

    /**
     * Returns True if the filename of each file processed should be logged.
     * @return True if the filenames should be logged.
     */
    public boolean getLogFiles() {
        return this.logFiles;
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
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
        try {
            Starter starter = new FOPTaskStarter(this);
            starter.enableLogging(log);
            starter.run();
        } catch (FOPException ex) {
            throw new BuildException(ex);
        }

    }

}

class FOPTaskStarter extends Starter {
    
    private Fop task;
    private String baseURL = null;

    FOPTaskStarter(Fop task) throws FOPException {
        this.task = task;
    }

    private int determineRenderer(String format) {
        if ((format == null) 
                || format.equalsIgnoreCase("application/pdf") 
                || format.equalsIgnoreCase("pdf")) {
            return Driver.RENDER_PDF;
        } else if (format.equalsIgnoreCase("application/postscript") 
                || format.equalsIgnoreCase("ps")) {
            return Driver.RENDER_PS;
        } else if (format.equalsIgnoreCase("application/vnd.mif") 
                || format.equalsIgnoreCase("mif")) {
            return Driver.RENDER_MIF;
        } else if (format.equalsIgnoreCase("application/msword") 
                || format.equalsIgnoreCase("application/rtf") 
                || format.equalsIgnoreCase("rtf")) {
            return Driver.RENDER_RTF;
        } else if (format.equalsIgnoreCase("application/vnd.hp-PCL") 
                || format.equalsIgnoreCase("pcl")) {
            return Driver.RENDER_PCL;
        } else if (format.equalsIgnoreCase("text/plain") 
                || format.equalsIgnoreCase("txt")) {
            return Driver.RENDER_TXT;
        } else if (format.equalsIgnoreCase("text/xml") 
                || format.equalsIgnoreCase("at") 
                || format.equalsIgnoreCase("xml")) {
            return Driver.RENDER_XML;
        } else {
            String err = "Couldn't determine renderer to use: " + format;
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
            case Driver.RENDER_RTF:
                return ".rtf";
            case Driver.RENDER_PCL:
                return ".pcl";
            case Driver.RENDER_TXT:
                return ".txt";
            case Driver.RENDER_XML:
                return ".xml";
            default:
                String err = "Unknown renderer: " + renderer;
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

    /**
     * @see org.apache.fop.apps.Starter#run()
     */
    public void run() throws FOPException {
        //Setup configuration
        if (task.getUserconfig() != null) {
            /**@todo implement me */
        }

        //Set base directory
        if (task.getBasedir() != null) {
            try {
                this.baseURL = task.getBasedir().toURL().toExternalForm();
            } catch (MalformedURLException mfue) {
                getLogger().error("Error creating base URL from base directory", mfue);
            }
        } else {
            try {
                if (task.getFofile() != null) {
                    this.baseURL =  task.getFofile().getParentFile().toURL().
                                      toExternalForm();
                }
            } catch (MalformedURLException mfue) {
                getLogger().error("Error creating base URL from XSL-FO input file", mfue);
            }
        }

        task.log("Using base URL: " + baseURL, Project.MSG_DEBUG);

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

        GlobPatternMapper mapper = new GlobPatternMapper();
        mapper.setFrom("*.fo");
        mapper.setTo("*" + newExtension);

        // deal with the filesets
        for (int i = 0; i < task.getFilesets().size(); i++) {
            FileSet fs = (FileSet) task.getFilesets().get(i);
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
                    if (this.baseURL == null) {
                        this.baseURL = fs.getDir(task.getProject()).toURL().
                                          toExternalForm();
                    }

                } catch (Exception e) {
                    task.log("Error setting base URL", Project.MSG_DEBUG);
                }

                render(f, outf, rint);
                actioncount++;
            }
        }

        if (actioncount == 0) {
            task.log("No files processed. No files were selected by the filesets "
                + "and no fofile was set." , Project.MSG_WARN);
        }
    }

    private void render(File foFile, File outFile,
                        int renderer) throws FOPException {
        InputHandler inputHandler = new FOInputHandler(foFile);
        XMLReader parser = inputHandler.getParser();
        setParserFeatures(parser);

        OutputStream out = null;
        try {
            out = new java.io.FileOutputStream(outFile);
        } catch (Exception ex) {
            throw new BuildException("Failed to open " + outFile, ex);
        }

        if (task.getLogFiles()) {
            task.log(foFile + " -> " + outFile, Project.MSG_INFO);
        }

        try {
            Driver driver = new Driver();
            setupLogger(driver);
            driver.initialize();
            FOUserAgent userAgent = new FOUserAgent();
            userAgent.setBaseURL(this.baseURL);
            userAgent.enableLogging(getLogger());
            driver.setUserAgent(userAgent);
            driver.setRenderer(renderer);
            driver.setOutputStream(out);
            driver.render(parser, inputHandler.getInputSource());
        } catch (Exception ex) {
            throw new BuildException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ioe) {
                getLogger().error("Error closing output file", ioe);
            }
        }
    }

}

