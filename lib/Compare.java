/*
// header - edit "Data/yourJavaHeader" to customize
// contents - edit "EventHandlers/Java file/onCreate" to customize
//
*/

import java.util.*;
import java.io.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import java.text.DateFormat;


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
      filenameListTmp.add(tokens.nextToken());
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

