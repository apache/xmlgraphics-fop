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


import java.util.*;
import java.io.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import java.text.DateFormat;

  /**  This class is an extension of Ant, a script utility from 
   *   jakarta.apache.org. 
   *   It provides methods to compare two files
   */   

public class Compare {
  private String referenceDirectory, testDirectory;
  private String [] filenameList;
  private String filenames;
  private static boolean IDENTICAL_FILES = true;
  private static boolean NOTIDENTICAL_FILES = false;
  private BufferedInputStream oldfileInput;
  private BufferedInputStream newfileInput;
 
  //sets directory for test files
  public void setTestDirectory(String testDirectory) {
    if (!(testDirectory.endsWith("/") | testDirectory.endsWith("\\"))) {
      testDirectory += File.separator; 
    }
    this.testDirectory = testDirectory;
  }
  
  //sets directory for reference files
  public void setReferenceDirectory(String referenceDirectory) {
    if (!(referenceDirectory.endsWith("/") | referenceDirectory.endsWith("\\"))) {
      referenceDirectory += File.separator; 
    }
    this.referenceDirectory = referenceDirectory;
  }

  public void setFilenames (String filenames) {
    StringTokenizer tokens = new StringTokenizer(filenames,",");
    Vector filenameListTmp = new Vector(20);
    while (tokens.hasMoreTokens()) {
      filenameListTmp.addElement(tokens.nextToken());
    }
    filenameList = new String [filenameListTmp.size()] ;
    filenameListTmp.copyInto((String[]) filenameList);
  }
  
  private boolean compareBytes (File oldFile, File newFile) {
    try {
      oldfileInput = new BufferedInputStream(new FileInputStream(oldFile));
      newfileInput = new BufferedInputStream(new FileInputStream(newFile));      
      int charactO = 0;
      int charactN = 0;
      boolean identical = true;
      
      while (identical & (charactO != -1)) {
        if (charactO == charactN) {
          charactO = oldfileInput.read();
          charactN = newfileInput.read(); 
        } else {
          return NOTIDENTICAL_FILES;
        }
      }
      return IDENTICAL_FILES;
    } catch (IOException io) {
      System.err.println("Task Compare - Error: \n" + io.toString());
    }
    return NOTIDENTICAL_FILES;
  }
  
  private boolean compareFileSize(File oldFile, File newFile) {
    if (oldFile.length() != newFile.length()) {
      return NOTIDENTICAL_FILES;
    } else {
      return IDENTICAL_FILES;
    }
  } //end: compareBytes

  private boolean filesExist (File oldFile, File newFile) {
    if (!oldFile.exists())  {
      System.err.println("Task Compare - ERROR: File "  
                          + referenceDirectory + oldFile.getName() 
                          + " doesn't exist!");
      return false;
    } else if (!newFile.exists()) {
      System.err.println("Task Compare - ERROR: File " 
                          + testDirectory + newFile.getName() + " doesn't exist!");    
      return false;
    } else {
      return true;
    }
  } 
  
  public void writeHeader (PrintWriter results)  {
    String dateTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(new Date());
    results.println("<html><head><title>Test Results</title></head><body>\n");
    results.println("<h2>Compare Results<br>");
    results.println("<font size='1'>created " + dateTime + "</font></h2>");
    results.println("<table cellpadding='10' border='2'><thead><th align='center'>reference file</th><th align='center'>test file</th>" 
                    + "<th align='center'>identical?</th></thead>");
                    
  
  }
  
  //main method of task compare
  public void execute () throws BuildException {
    boolean identical = false; 
    File oldFile;
    File newFile;   
    try {
      PrintWriter results = new PrintWriter (new FileWriter("results.html"),true);
      this.writeHeader(results);
      for (int i = 0; i < filenameList.length; i++) {
        oldFile = new File (referenceDirectory + filenameList[i]);
        newFile = new File (testDirectory + filenameList[i]);      
        if (filesExist(oldFile, newFile)) {
          identical = compareFileSize(oldFile, newFile);  
          if (identical) {
            identical = compareBytes(oldFile,newFile);
          } 
          if (!identical) { 
            System.out.println("Task Compare: \nFiles " + referenceDirectory 
                                + oldFile.getName()+ " - " + testDirectory 
                                + newFile.getName() + " are *not* identical.");
            results.println("<tr><td><a href='" + referenceDirectory + oldFile.getName() + "'>"
                             + oldFile.getName() + "</a> </td><td> <a href='"   
                             + testDirectory + newFile.getName() +"'>" 
                             + newFile.getName() +"</a>" 
                             + " </td><td><font color='red'>No</font></td></tr>");
          } else {
            results.println("<tr><td><a href='" + referenceDirectory + oldFile.getName() + "'>"
                             + oldFile.getName() + "</a> </td><td> <a href='"   
                             + testDirectory + newFile.getName() + "'>" 
                             + newFile.getName() + "</a>" 
                             + " </td><td>Yes</td></tr>");
          }
        } 
      }
      results.println("</table></html>");
    } catch (IOException ioe) {
      System.err.println("ERROR: " + ioe);
    }
  } //end: execute()
}

