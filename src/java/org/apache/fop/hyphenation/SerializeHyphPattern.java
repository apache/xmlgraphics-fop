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

package org.apache.fop.hyphenation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * <p>Serialize hyphenation patterns.</p>
 * <p>For all xml files in the source directory a pattern file is built in the target directory.</p>
 * <p>This class may be called from the ant build file in a java task.</p>
 */
public class SerializeHyphPattern {

    private boolean errorDump;

    /**
     * Controls the amount of error information dumped.
     * @param errorDump True if more error info should be provided
     */
    public void setErrorDump(boolean errorDump) {
        this.errorDump = errorDump;
    }

    /**
     * Compile all xml files in sourceDir, and write output hyp files in targetDir
     * @param sourceDir Directory with pattern xml files
     * @param targetDir Directory to which compiled pattern hyp files should be written
     */
    public void serializeDir(File sourceDir, File targetDir) {
        final String extension = ".xml";
        String[] sourceFiles = sourceDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        });
        for (int j = 0; j < sourceFiles.length; j++) {
            File infile = new File(sourceDir, sourceFiles[j]);
            String outfilename = sourceFiles[j].substring(0, sourceFiles[j].length()
                                                          - extension.length()) + ".hyp";
            File outfile = new File(targetDir, outfilename);
            serializeFile(infile, outfile);
        }
    }

    /*
     * checks whether input or output files exists or the latter is older than input file
     * and start build if necessary
     */
    private void serializeFile(File infile, File outfile) {
        boolean startProcess;
        startProcess = rebuild(infile, outfile);
        if (startProcess) {
            HyphenationTree hTree = buildPatternFile(infile);
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
    }

    /*
     * serializes pattern files
     */
    private HyphenationTree buildPatternFile(File infile) {
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
        return hTree;
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


    /**
     * Entry point for ant java task
     * @param args sourceDir, targetDir
     */
    public static void main(String[] args) {
        SerializeHyphPattern ser = new SerializeHyphPattern();
        ser.serializeDir(new File(args[0]), new File(args[1]));
    }

}
