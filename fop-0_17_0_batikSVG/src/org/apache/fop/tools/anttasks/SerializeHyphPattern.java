/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *  any, must include the following acknowlegement:
 *     "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowlegement may appear in the software itself,
 *  if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *  Foundation" must not be used to endorse or promote products derived
 *  from this software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *  nor may "Apache" appear in their names without prior written
 *  permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.fop.tools.anttasks;

//package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;
import java.net.*;
import java.io.*;
import java.util.*;
import org.xml.sax.SAXException;

//fop
import org.apache.fop.layout.hyphenation.HyphenationTree;
import org.apache.fop.layout.hyphenation.HyphenationException;

/**
 *    SerializeHyphPattern
 */


public class SerializeHyphPattern extends MatchingTask {
    private File sourceDir, targetDir;
    private boolean errorDump = false;

    /**
       *  Main method, which is called by ant.
       */
    public void execute () throws org.apache.tools.ant.BuildException {
        DirectoryScanner ds = this.getDirectoryScanner(sourceDir);
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length ; i++) {
            processFile(files[i].substring(0, files[i].length() - 4));
        }
    } //end execute


    /**
       * Sets the source directory
       *
       */
    public void setSourceDir (String sourceDir) {
        File dir = new File(sourceDir);
        if (!dir.exists()) {
            System.err.println("Fatal Error: source directory " +
                               sourceDir + " for hyphenation files doesn't exist.");
            System.exit(1);
        }
        this.sourceDir = dir;
    }

    /**
         * Sets the target directory
         *
         */
    public void setTargetDir (String targetDir) {
        File dir = new File(targetDir);
        this.targetDir = dir;
    }

    /**
         * more error information
         *
         */
    public void setErrorDump (boolean errorDump) {
        this.errorDump = errorDump;
    }


    /*
      * checks whether input or output files exists or the latter is older than input file
      * and start build if necessary
      */
    private void processFile (String filename) {
        File infile = new File (sourceDir , filename + ".xml");
        File outfile = new File(targetDir , filename + ".hyp");
        long outfileLastModified = outfile.lastModified();
        boolean startProcess = true;

        startProcess = rebuild(infile, outfile);
        if (startProcess) {
            buildPatternFile(infile, outfile);
        }
    }

    /*
       * serializes pattern files
       */
    private void buildPatternFile (File infile, File outfile) {
        System.out.println("Processing " + infile);
        HyphenationTree hTree = new HyphenationTree();
        try {
            hTree.loadPatterns(infile.toString());
            if (errorDump) {
                System.out.println("Stats: ");
                hTree.printStats();
            }
        } catch (HyphenationException ex) {
            System.err.println("Can't load patterns from xml file " +
                               infile + " - Maybe hyphenation.dtd is missing?");
            if (errorDump) {
                System.err.println(ex.toString());
            }
        }
        //serialize class
        try {
            ObjectOutputStream out = new ObjectOutputStream (
                                       new FileOutputStream(outfile));
            out.writeObject(hTree);
            out.close();
        } catch (IOException ioe) {
            System.err.println("Can't write compiled pattern file: " +
                               outfile);
            System.err.println(ioe);
        }
    }

    /**
       *  Checks for existence of output file and compares
       *  dates with input and stylesheet file
       */
    private boolean rebuild (File infile, File outfile) {
        if (outfile.exists()) {
            //checks whether output file is older than input file
            if (outfile.lastModified() < infile.lastModified()) {
                return true;
            }
        } else {
            //if output file does not exist, start process
            return true;
        }
        return false;
    } //end rebuild

/*
    //quick access for debugging
    public static void main (String args[]) {
        SerializeHyphPattern ser = new SerializeHyphPattern();
        ser.setIncludes("*.xml");
        ser.setSourceDir("\\xml-fop\\hyph\\");
        ser.setTargetDir("\\xml-fop\\hyph\\");
        ser.execute();
    }
*/


}
