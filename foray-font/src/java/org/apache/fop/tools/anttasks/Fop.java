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

package org.apache.fop.tools.anttasks;

// Ant
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.GlobPatternMapper;

// Java
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.cli.InputHandler;

import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.logging.Log;
import org.xml.sax.SAXException;

/**
 * Wrapper for FOP which allows it to be accessed from within an Ant task.
 * Accepts the inputs:
 * <ul>
 * <li>fofile -> formatting objects file to be transformed</li>
 * <li>format -> MIME type of the format to generate ex. "application/pdf"</li>
 * <li>outfile -> output filename</li>
 * <li>baseDir -> directory to work from</li>
 * <li>relativebase -> (true | false) control whether to use each FO's 
 *      directory as base directory. false uses the baseDir parameter.</li>
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
    private boolean force = false;
    private boolean relativebase = false;

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
     * Set whether to include files (external-graphics, instream-foreign-object)
     * from a path relative to the .fo file (true) or the working directory (false, default)
     * only useful for filesets
     *
     * @param relbase true if paths are relative to file.
     */
    public void setRelativebase(boolean relbase) {
        this.relativebase = relbase;
    }
    
    /**
     * Gets the relative base attribute
     * @return the relative base attribute
     */
    public boolean getRelativebase() {
        return relativebase;
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
     * Sets the base directory for single FO file (non-fileset) usage
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
        return (baseDir != null) ? baseDir : getProject().resolveFile(".");
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
        int logLevel = SimpleLog.LOG_LEVEL_INFO;
        switch (getMessageType()) {
            case Project.MSG_DEBUG  : logLevel = SimpleLog.LOG_LEVEL_DEBUG; break;
            case Project.MSG_INFO   : logLevel = SimpleLog.LOG_LEVEL_INFO; break;
            case Project.MSG_WARN   : logLevel = SimpleLog.LOG_LEVEL_WARN; break;
            case Project.MSG_ERR    : logLevel = SimpleLog.LOG_LEVEL_ERROR; break;
            case Project.MSG_VERBOSE: logLevel = SimpleLog.LOG_LEVEL_DEBUG; break;
            default: logLevel = SimpleLog.LOG_LEVEL_INFO;
        }
        SimpleLog logger = new SimpleLog("FOP/Anttask");
        logger.setLevel(logLevel);
        try {
            FOPTaskStarter starter = new FOPTaskStarter(this);
            starter.setLogger(logger);
            starter.run();
        } catch (FOPException ex) {
            throw new BuildException(ex);
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        } catch (SAXException saxex) {
            throw new BuildException(saxex);
        }

    }

}

class FOPTaskStarter {

    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();
    
    private Fop task;
    private String baseURL = null;

    /**
     * logging instance
     */
    protected Log logger = null;


    /**
     * Sets the Commons-Logging instance for this class
     * @param logger The Commons-Logging instance
     */
    public void setLogger(Log logger) {
        this.logger = logger;
    }

    /**
     * Returns the Commons-Logging instance for this class
     * @return  The Commons-Logging instance
     */
    protected Log getLogger() {
        return logger;
    }

    FOPTaskStarter(Fop task) throws SAXException, IOException {
        this.task = task;
        if (task.getUserconfig() != null) {
            fopFactory.setUserConfig(task.getUserconfig());
        }
    }

    private static final String[][] SHORT_NAMES = {
        {"pdf",  MimeConstants.MIME_PDF},
        {"ps",   MimeConstants.MIME_POSTSCRIPT},
        {"mif",  MimeConstants.MIME_MIF},
        {"rtf",  MimeConstants.MIME_RTF},
        {"pcl",  MimeConstants.MIME_PCL},
        {"txt",  MimeConstants.MIME_PLAIN_TEXT},
        {"at",   MimeConstants.MIME_FOP_AREA_TREE},
        {"xml",  MimeConstants.MIME_FOP_AREA_TREE},
        {"tiff", MimeConstants.MIME_TIFF},
        {"tif",  MimeConstants.MIME_TIFF},
        {"png",  MimeConstants.MIME_PNG},
        {"afp",  MimeConstants.MIME_AFP}
    };

    private String normalizeOutputFormat(String format) {
        for (int i = 0; i < SHORT_NAMES.length; i++) {
            if (SHORT_NAMES[i][0].equals(format)) {
                return SHORT_NAMES[i][1];
            }
        }
        return format; //no change
    }

    private static final String[][] EXTENSIONS = {
        {MimeConstants.MIME_FOP_AREA_TREE,   ".at.xml"},
        {MimeConstants.MIME_FOP_AWT_PREVIEW, null},
        {MimeConstants.MIME_FOP_PRINT,       null},
        {MimeConstants.MIME_PDF,             ".pdf"},
        {MimeConstants.MIME_POSTSCRIPT,      ".ps"},
        {MimeConstants.MIME_PCL,             ".pcl"},
        {MimeConstants.MIME_PCL_ALT,         ".pcl"},
        {MimeConstants.MIME_PLAIN_TEXT,      ".txt"},
        {MimeConstants.MIME_RTF,             ".rtf"},
        {MimeConstants.MIME_RTF_ALT1,        ".rtf"},
        {MimeConstants.MIME_RTF_ALT2,        ".rtf"},
        {MimeConstants.MIME_MIF,             ".mif"},
        {MimeConstants.MIME_SVG,             ".svg"},
        {MimeConstants.MIME_PNG,             ".png"},
        {MimeConstants.MIME_JPEG,            ".jpg"},
        {MimeConstants.MIME_TIFF,            ".tif"},
        {MimeConstants.MIME_AFP,             ".afp"},
        {MimeConstants.MIME_AFP_ALT,         ".afp"},
        {MimeConstants.MIME_XSL_FO,          ".fo"}
    };
    
    private String determineExtension(String outputFormat) {
        for (int i = 0; i < EXTENSIONS.length; i++) {
            if (EXTENSIONS[i][0].equals(outputFormat)) {
                String ext = EXTENSIONS[i][1];
                if (ext == null) {
                    throw new RuntimeException("Output format '" 
                            + outputFormat + "' does not produce a file.");
                } else {
                    return ext;
                }
            }
        }
        return ".unk"; //unknown
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
        //Set base directory
        if (task.getBasedir() != null) {
            try {
                this.baseURL = task.getBasedir().toURL().toExternalForm();
            } catch (MalformedURLException mfue) {
                logger.error("Error creating base URL from base directory", mfue);
            }
        } else {
            try {
                if (task.getFofile() != null) {
                    this.baseURL =  task.getFofile().getParentFile().toURL().
                                      toExternalForm();
                }
            } catch (MalformedURLException mfue) {
                logger.error("Error creating base URL from XSL-FO input file", mfue);
            }
        }

        task.log("Using base URL: " + baseURL, Project.MSG_DEBUG);

        String outputFormat = normalizeOutputFormat(task.getFormat());
        String newExtension = determineExtension(outputFormat);

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
                    render(task.getFofile(), outf, outputFormat);
                    actioncount++;
                } else if (outf.exists() 
                        && (task.getFofile().lastModified() <= outf.lastModified() )) {
                    skippedcount++;
                }
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
                    if (task.getRelativebase()) {
                        this.baseURL = f.getParentFile().toURL().
                                         toExternalForm();
                    }
                    if (this.baseURL == null) {
                        this.baseURL = fs.getDir(task.getProject()).toURL().
                                          toExternalForm();
                    }

                } catch (Exception e) {
                    task.log("Error setting base URL", Project.MSG_DEBUG);
                }

                // Render if "force" flag is set OR 
                // OR output file doesn't exist OR
                // output file is older than input file
                if (task.getForce() || !outf.exists() 
                    || (f.lastModified() > outf.lastModified() )) {
                    render(f, outf, outputFormat);
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
                        String outputFormat) throws FOPException {
        InputHandler inputHandler = new InputHandler(foFile);

        OutputStream out = null;
        try {
            out = new java.io.FileOutputStream(outFile);
            out = new BufferedOutputStream(out);
        } catch (Exception ex) {
            throw new BuildException("Failed to open " + outFile, ex);
        }

        if (task.getLogFiles()) {
            task.log(foFile + " -> " + outFile, Project.MSG_INFO);
        }

        boolean success = false;
        try {
            FOUserAgent userAgent = fopFactory.newFOUserAgent();
            userAgent.setBaseURL(this.baseURL);
            inputHandler.renderTo(userAgent, outputFormat, out);
            success = true;
        } catch (Exception ex) {
            throw new BuildException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ioe) {
                logger.error("Error closing output file", ioe);
            }
            if (!success) {
                outFile.delete();
            }
        }
    }

}

