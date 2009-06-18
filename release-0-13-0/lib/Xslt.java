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
    
//package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.Task;
import java.net.*;
import java.io.*;
import java.util.*;
import org.apache.xalan.xslt.*;

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
 * <li>smart
 * <li>dependent
 * </ul>
 * <p>  
 * Of these arguments, <b>infile, outfile</b> and <b>xsltfile</b> are required.
 * <p>smart defaults to 'no'. The other allowed value is 'yes'. If smart is set to 'yes'
 * xalan is only called if either the outfile is older than the infile or the stylesheet
 * or the outfile doesn't exist. 
 * <p>dependent defaults to 'none'. Other possible values: a comma delimited list of file names
 * which date is checked against the output file. This way you can name files which, if 
 * they have been modified, initiate a restart of the xslt process, like external entities etc.
 * <p>
 * @author Fotis Jannidis <a href="mailto:fotis@jannidis.de">fotis@jannidis.de</a>
 */


public class Xslt extends Task {
  private String infile, outfile, xsltfile;
  private String smart = "no";  //defaults to do conversion everytime task is called
  private String dependent = "none"; //defaults to no dependencies
  private boolean startXslt = false; 


  /**
   * Sets the input file
   *
   */
  public void setInfile (String infile) {
    this.infile = infile;
  }

  /**
   * Sets the stylesheet file
   *
   */
  public void setXsltfile (String xsltfile) {
    this.xsltfile = xsltfile;
  }
  
  /**
   * Sets the output file
   *
   */
  public void setOutfile (String outfile) {
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
  public void setSmart (String smart) {
    this.smart = smart;
  }
  
/**
   * Sets the value for dependent 
   *
   * @param option valid values:
   * <ul>
   * <li>none: (default) 
   * <li>comma delimited list of files whose existence and date is checked 
   *     against the output file
   * </ul>
   */
  public void setDependent (String dependent) {
    this.dependent = dependent;
  }
  
  /**
   * Calls Xalan and does the transformation
   *
   */
  private void transform(String xmlSourceURL, String xslURL, String outputURL)
                           throws java.io.IOException, 
                                  java.net.MalformedURLException, 
                                  org.xml.sax.SAXException 
  {               
    // Use XSLTProcessor to instantiate an XSLTProcessor.
    org.apache.xalan.xslt.XSLTProcessor processor =
                 org.apache.xalan.xslt.XSLTProcessorFactory.getProcessor();
                 
    // Create the 3 objects the XSLTProcessor needs to perform the transformation.
    org.apache.xalan.xslt.XSLTInputSource xmlSource = 
                        new org.apache.xalan.xslt.XSLTInputSource (infile);
    org.apache.xalan.xslt.XSLTInputSource xslSheet = 
                       new org.apache.xalan.xslt.XSLTInputSource (xsltfile);
    org.apache.xalan.xslt.XSLTResultTarget xmlResult = 
                       new org.apache.xalan.xslt.XSLTResultTarget (outfile);

    // Perform the transformation.
    System.out.println("============================");
    System.out.println("xslt \nin: " + infile + "\nstyle: " + xsltfile + "\nout: " + outfile);
    System.out.println("============================");
    processor.process(xmlSource, xslSheet, xmlResult);
  } //end transform
    
  /**
   *  Catches the errors transform() can throw and 
   *  returns meaningfull error messages
   */
  private void startTransform () {
    try {
      transform (infile,xsltfile,outfile);  
    } catch (org.xml.sax.SAXException saxerror) {
      System.out.println("Task xslt - SAX ERROR:\n      " + saxerror);
    } catch (MalformedURLException urlerror) {
      System.out.println("Task xslt - URL ERROR:\n      " + urlerror); 
    } catch (IOException ioerror) {
      System.out.println("Task xslt - IO ERROR:\n      " + ioerror); 
    }
  } //end startTransform
  
  /**
   *  Checks for existence of output file and compares 
   *  dates with input and stylesheet file
   */
  private boolean smartCheck (File outfileF, long outfileLastModified, File infileF, File xsltfileF) {
 
    if (outfileF.exists()) {
      //checks whether output file is older than input file or xslt stylesheet file
      if ((outfileLastModified < infileF.lastModified()) |
          (outfileLastModified < xsltfileF.lastModified())) {
        return true;
      } 
    } else {
      //if output file does not exist, start xslt process
      return true;
    }
    return false;
  } //end smartCheck
  
  /**
   *  Checks for existence and date of dependent files
   *  This could be folded together with smartCheck by using 
   *  a general routine but it wouldn't be as fast as now
   */
  private boolean dependenciesCheck(File outfileF, long outfileLastModified) {
    String dependentFileName;
    File dependentFile;
    StringTokenizer tokens = new StringTokenizer(dependent,",");
    while (tokens.hasMoreTokens()) {
      dependentFileName = (String) tokens.nextToken();
      dependentFile = new File (dependentFileName);
      //check: does dependent file exist
      if (dependentFile.exists()) {
        //check dates
        if ((outfileLastModified < dependentFile.lastModified()) ) {
          return true;
        }
      } else {
        System.err.println("Task xslt - ERROR in attribute 'dependent':\n      file " + dependentFileName + " does not exist.");
      }
    }
    return false;   
  } //end dependenciesCheck
  
/**
 *  Main method, which is called by ant. 
 *  Checks for the value of smart and calls startTransform accordingly
 */
  public void execute () throws org.apache.tools.ant.BuildException {
    
    File outfileF = new File (outfile); 
    File infileF = new File(infile);
    File xsltfileF = new File (xsltfile);
    long outfileLastModified = outfileF.lastModified();
    boolean startFileExist = true;

    //checks whether input and stylesheet exist. 
    //this could be left to the parser, but this solution does make problems if smart is set to yes
    if (!infileF.exists()) {
      System.err.println("Task xslt - ERROR:\n      Input file " + infile + " does not exist!");
      startFileExist = false;
    } else if (!xsltfileF.exists()) {
      System.err.println("Task xslt - ERROR:\n      Stylesheet file " + xsltfile + " does not exist!");
      startFileExist = false;
    } 

    //checks attribute 'smart' 
    if (smart.equals("no")) {
      startXslt = true;
    //if attribute smart = 'yes'
    } else if (smart.equals("yes")) {
      startXslt = smartCheck (outfileF,outfileLastModified,infileF,xsltfileF);
      //checks dependent files against output file, makes only sense if smartCheck returns false
      if (!dependent.equals("none") & (startXslt == false)) {
        startXslt = dependenciesCheck(outfileF,outfileLastModified);      
      }
      //returns error message, if smart has another value as 'yes' or 'no'
    } else {
      System.err.println("Task xslt - ERROR: Allowed values for the attribute smart are 'yes' or 'no'");
    }
    if (startFileExist & startXslt) {
      startTransform();
    }
  } //end execute
  
  //quick access for debugging  
  //usage XSLT infile xsltfile outfile (smart is 'yes') 
  /*
  public static void main (String args[]) {
      Xslt xslt = new Xslt();
      xslt.setInfile(args[0]);
      xslt.setXsltfile(args[1]);
      xslt.setOutfile(args[2]);
      xslt.setSmart("yes");
      xslt.setDependent("test1,test2");
      xslt.execute();
  } */
    
}
