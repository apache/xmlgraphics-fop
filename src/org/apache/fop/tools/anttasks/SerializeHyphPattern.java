/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.anttasks;

// Java
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

// Ant
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;

// FOP
import org.apache.fop.layout.hyphenation.HyphenationTree;
import org.apache.fop.layout.hyphenation.HyphenationException;

/**
 * SerializeHyphPattern
 */


public class SerializeHyphPattern extends MatchingTask {
    private File sourceDir, targetDir;
    private boolean errorDump = false;

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws org.apache.tools.ant.BuildException {
        DirectoryScanner ds = this.getDirectoryScanner(sourceDir);
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            processFile(files[i].substring(0, files[i].length() - 4));
        }
    }    // end execute


    /**
     * Sets the source directory.
     * @param sourceDir source directory
     */
    public void setSourceDir(String sourceDir) {
        File dir = new File(sourceDir);
        if (!dir.exists()) {
            System.err.println("Fatal Error: source directory " + sourceDir
                               + " for hyphenation files doesn't exist.");
            System.exit(1);
        }
        this.sourceDir = dir;
    }

    /**
     * Sets the target directory
     * @param targetDir target directory
     */
    public void setTargetDir(String targetDir) {
        File dir = new File(targetDir);
        this.targetDir = dir;
    }

    /**
     * Controls the amount of error information dumped.
     * @param errorDump True if more error info should be provided
     */
    public void setErrorDump(boolean errorDump) {
        this.errorDump = errorDump;
    }


    /*
     * checks whether input or output files exists or the latter is older than input file
     * and start build if necessary
     */
    private void processFile(String filename) {
        File infile = new File(sourceDir, filename + ".xml");
        File outfile = new File(targetDir, filename + ".hyp");
        //long outfileLastModified = outfile.lastModified();
        boolean startProcess = true;

        startProcess = rebuild(infile, outfile);
        if (startProcess) {
            buildPatternFile(infile, outfile);
        }
    }

    /*
     * serializes pattern files
     */
    private void buildPatternFile(File infile, File outfile) {
        System.out.println("Processing " + infile);
        HyphenationTree hTree = new HyphenationTree();
        try {
            hTree.loadPatterns(infile.toString());
            if (errorDump) {
                System.out.println("Stats: ");
                hTree.printStats();
            }
        } catch (HyphenationException ex) {
            System.err.println("Can't load patterns from xml file " + infile
                               + " - Maybe hyphenation.dtd is missing?");
            if (errorDump) {
                System.err.println(ex.toString());
            }
        }
        // serialize class
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new java.io.BufferedOutputStream(
                    new java.io.FileOutputStream(outfile)));
            out.writeObject(hTree);
            out.close();
        } catch (IOException ioe) {
            System.err.println("Can't write compiled pattern file: "
                               + outfile);
            System.err.println(ioe);
        }
    }

    /**
     * Checks for existence of output file and compares
     * dates with input and stylesheet file
     */
    private boolean rebuild(File infile, File outfile) {
        if (outfile.exists()) {
            // checks whether output file is older than input file
            if (outfile.lastModified() < infile.lastModified()) {
                return true;
            }
        } else {
            // if output file does not exist, start process
            return true;
        }
        return false;
    }    // end rebuild

    /*
     * //quick access for debugging
     * public static void main (String args[]) {
     * SerializeHyphPattern ser = new SerializeHyphPattern();
     * ser.setIncludes("*.xml");
     * ser.setSourceDir("\\xml-fop\\hyph\\");
     * ser.setTargetDir("\\xml-fop\\hyph\\");
     * ser.execute();
     * }
     */


}
