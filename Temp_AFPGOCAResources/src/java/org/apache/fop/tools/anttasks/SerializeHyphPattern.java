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

// Java
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

// Ant
import org.apache.tools.ant.Task;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

// FOP
import org.apache.fop.hyphenation.HyphenationTree;
import org.apache.fop.hyphenation.HyphenationException;

/**
 * SerializeHyphPattern
 */


public class SerializeHyphPattern extends Task {
    private List filesets = new java.util.ArrayList();
    private File targetDir;
    private boolean errorDump = false;

    /**
     * {@inheritDoc}
     */
    public void execute() throws org.apache.tools.ant.BuildException {
        // deal with the filesets
        for (int i = 0; i < getFilesets().size(); i++) {
            FileSet fs = (FileSet) getFilesets().get(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (int j = 0; j < files.length; j++) {
                processFile(basedir, files[j].substring(0, files[j].length() - 4));
            }
        }
    }    // end execute


    /**
     * Adds a set of pattern files (nested fileset attribute).
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
    private void processFile(File basedir, String filename) {
        File infile = new File(basedir, filename + ".xml");
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
     * FileSet set = new FileSet();
     * set.setDir(new File("src/hyph"));
     * set.setIncludes("*.xml");
     * ser.addFileset(set);
     * ser.setTargetDir("build/hyph");
     * ser.execute();
     * }
     */


}
