/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.anttasks;

import org.apache.tools.ant.Task;
import java.net.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;



/**
 * Task to call the XSLT processor Xalan (part of xml.apache.org), which converts xml files
 * from a source to an output using a stylesheet file
 *
 * <p>
 * This task can take the following arguments:
 * <ul>
 * <li>infile
 * <li>xsltfile
 * <li>outfile
 * <li>mergefile
 * <li>smart
 * <li>dependent
 * </ul>
 * <p>
 * Of these arguments, <b>infile, outfile</b> and <b>xsltfile</b> are required.
 * <p>smart defaults to 'no'. The other allowed value is 'yes'. If smart is set to 'yes'
 * <P>
 * xalan is only called if either the outfile is older than the infile or the stylesheet
 * or the outfile doesn't exist.
 * <P>
 * <p>dependent defaults to 'none'. Other possible values: a comma delimited list of file names
 * which date is checked against the output file. This way you can name files which, if
 * they have been modified, initiate a restart of the xslt process, like external entities etc.
 * <p>
 * The mergefile parameter causes this task to merge the contents of the specified file into the infile at the end. This is used for the font character mapping generation because the keys() xslt function doesn't work on an external document.
 *
 * @author Fotis Jannidis <a href="mailto:fotis@jannidis.de">fotis@jannidis.de</a>
 * @author Kelly A. Campbell <a href="mailto:camk@camk.net">camk@camk.net</a>
 */


public class Xslt extends Task {
    private String infile, outfile, xsltfile, mergefile;
    private String smart =
        "no";                             // defaults to do conversion everytime task is called
    private String dependent = "none";    // defaults to no dependencies
    private boolean startXslt = false;

    /**
     * When true, we use the trax api's from xalan2, otherwise
     * just the xalan1 native interfaces
     */
    private boolean useTrax = false;


    /**
     * Sets the input file
     *
     */
    public void setInfile(String infile) {
        this.infile = infile;
    }

    public void setMergefile(String mergefile) {
        this.mergefile = mergefile;
    }

    /**
     * Sets the stylesheet file
     *
     */
    public void setXsltfile(String xsltfile) {
        this.xsltfile = xsltfile;
    }

    /**
     * Sets the output file
     *
     */
    public void setOutfile(String outfile) {
        this.outfile = outfile;
    }

    /**
     * Sets the value for smart
     *
     * @param option valid values:
     * <ul>
     * <li>yes: check whether output file is older than input or stylesheet
     * <li>no: (default) do conversion everytime task is called
     * </ul>
     */
    public void setSmart(String smart) {
        this.smart = smart;
    }

    /**
     * Sets the value for dependent
     *
     * @param option valid values:
     * <ul>
     * <li>none: (default)
     * <li>comma delimited list of files whose existence and date is checked
     * against the output file
     * </ul>
     */
    public void setDependent(String dependent) {
        this.dependent = dependent;
    }


    /**
     * Builds a document from the given file, merging the mergefile onto the end of the root node
     */
    private org.w3c.dom.Document buildDocument(String xmlFile)
            throws IOException, SAXException {
        try {

            javax.xml.parsers.DocumentBuilder docBuilder =
                javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(new FileInputStream(xmlFile));

            if (mergefile != null &&!mergefile.equals("")) {

                File mergefileF = new File(mergefile);

                Document mergedoc =
                    docBuilder.parse(new FileInputStream(mergefileF));
                Node mergenode = doc.importNode(mergedoc.getDocumentElement(),
                                                true);
                doc.getDocumentElement().appendChild(mergenode);
            }

            return doc;
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            System.out.println("Task xslt - SAX ERROR:\n      "
                               + e.getMessage());
        }
        return null;
    }


    /**
     * Calls Xalan and does the transformation
     *
     */
    private void transform() {
        try {
            org.w3c.dom.Document source = buildDocument(infile);
            // Perform the transformation.
            System.out.println("============================");
            System.out.println("xslt \nin: " + infile + "\nstyle: "
                               + xsltfile + "\nout: " + outfile);
            System.out.println("============================");
            org.apache.fop.tools.xslt.XSLTransform.transform(source,
                    xsltfile, outfile);


        } catch (org.xml.sax.SAXException saxerror) {
            System.out.println("Task xslt - SAX ERROR:\n      " + saxerror);
        } catch (MalformedURLException urlerror) {
            System.out.println("Task xslt - URL ERROR:\n      " + urlerror);
        } catch (IOException ioerror) {
            System.out.println("Task xslt - IO ERROR:\n      " + ioerror);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }    // end transform


    /**
     * Checks for existence of output file and compares
     * dates with input and stylesheet file
     */
    private boolean smartCheck(File outfileF, long outfileLastModified,
                               File infileF, File xsltfileF) {

        if (outfileF.exists()) {
            // checks whether output file is older than input file or xslt stylesheet file
            if ((outfileLastModified < infileF.lastModified())
                    | (outfileLastModified < xsltfileF.lastModified())) {
                return true;
            }
        } else {
            // if output file does not exist, start xslt process
            return true;
        }
        return false;
    }    // end smartCheck

    /**
     * Checks for existence and date of dependent files
     * This could be folded together with smartCheck by using
     * a general routine but it wouldn't be as fast as now
     */
    private boolean dependenciesCheck(File outfileF,
                                      long outfileLastModified) {
        String dependentFileName;
        File dependentFile;
        StringTokenizer tokens = new StringTokenizer(dependent, ",");
        while (tokens.hasMoreTokens()) {
            dependentFileName = (String)tokens.nextToken();
            dependentFile = new File(dependentFileName);
            // check: does dependent file exist
            if (dependentFile.exists()) {
                // check dates
                if ((outfileLastModified < dependentFile.lastModified())) {
                    return true;
                }
            } else {
                System.err.println("Task xslt - ERROR in attribute 'dependent':\n      file "
                                   + dependentFileName + " does not exist.");
            }
        }
        return false;
    }    // end dependenciesCheck

    /**
     * Main method, which is called by ant.
     * Checks for the value of smart and calls startTransform accordingly
     */
    public void execute() throws org.apache.tools.ant.BuildException {

        File outfileF = new File(outfile);
        File infileF = new File(infile);
        File xsltfileF = new File(xsltfile);
        long outfileLastModified = outfileF.lastModified();
        boolean startFileExist = true;

        // checks whether input and stylesheet exist.
        // this could be left to the parser, but this solution does make problems if smart is set to yes
        if (!infileF.exists()) {
            System.err.println("Task xslt - ERROR:\n      Input file "
                               + infile + " does not exist!");
            startFileExist = false;
        } else if (!xsltfileF.exists()) {
            System.err.println("Task xslt - ERROR:\n      Stylesheet file "
                               + xsltfile + " does not exist!");
            startFileExist = false;
        }

        // checks attribute 'smart'
        if (smart.equals("no")) {
            startXslt = true;
            // if attribute smart = 'yes'
        } else if (smart.equals("yes")) {
            startXslt = smartCheck(outfileF, outfileLastModified, infileF,
                                   xsltfileF);
            // checks dependent files against output file, makes only sense if smartCheck returns false
            if (!dependent.equals("none") & (startXslt == false)) {
                startXslt = dependenciesCheck(outfileF, outfileLastModified);
            }
            // returns error message, if smart has another value as 'yes' or 'no'
        } else {
            System.err.println("Task xslt - ERROR: Allowed values for the attribute smart are 'yes' or 'no'");
        }
        if (startFileExist & startXslt) {
            transform();
        }
    }    // end execute

    // quick access for debugging
    // usage XSLT infile xsltfile outfile (smart is 'yes')
    /*
     * public static void main (String args[]) {
     * Xslt xslt = new Xslt();
     * xslt.setInfile(args[0]);
     * xslt.setXsltfile(args[1]);
     * xslt.setOutfile(args[2]);
     * xslt.setSmart("yes");
     * xslt.setDependent("test1,test2");
     * xslt.execute();
     * }
     */




}
