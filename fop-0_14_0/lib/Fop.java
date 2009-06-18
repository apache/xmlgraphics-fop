/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */

// Ant
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;


// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Java
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.apps.*;

/**
 * extension to Ant which allows usage of Fop in the 
 * same way as org.apache.fop.apps.CommandLine (the code is adapted from this class)
 * Gets input and output filenames from the script file <br/>
 * needed libraries: Sax 2 parser (defaults to Xerces-J), Jimi for images, w3c.jar 
 * containing org.w3c.dom.svg etc. for svg support
 */

public class Fop {
  String fofile, pdffile;
  
  /**
   * sets the name of the input file
   * @param String name of the input fo file 
   */
  public void setFofile(String fofile) {
    this.fofile = fofile;
  }
  
  /**
   * sets the name of the output file
   * @param String name of the output pdf file 
   */
  public void setPdffile(String pdffile) {
    this.pdffile = pdffile;
  }
  

  /**
   * creates a SAX parser, using the value of org.xml.sax.parser
   * defaulting to org.apache.xerces.parsers.SAXParser
   *
   * @return the created SAX parser
   */
  static XMLReader createParser() {
    String parserClassName =
        System.getProperty("org.xml.sax.parser");
    if (parserClassName == null) {
        parserClassName = "org.apache.xerces.parsers.SAXParser";
    }
    MessageHandler.logln("using SAX parser " + parserClassName);

    try {
        return (XMLReader)
          Class.forName(parserClassName).newInstance();
    } catch (ClassNotFoundException e) {
        MessageHandler.errorln("Could not find " + parserClassName);
    } catch (InstantiationException e) {
        MessageHandler.errorln("Could not instantiate "
                       + parserClassName);
    } catch (IllegalAccessException e) {
        MessageHandler.errorln("Could not access " + parserClassName);
    } catch (ClassCastException e) {
        MessageHandler.errorln(parserClassName + " is not a SAX driver"); 
    }
    return null;
  }  // end: createParser

  /**
   * create an InputSource from a file name
   *
   * @param filename the name of the file
   * @return the InputSource created
   */
  protected static InputSource fileInputSource(String filename) {

    /* this code adapted from James Clark's in XT */
    File file = new File(filename);
    String path = file.getAbsolutePath();
    String fSep = System.getProperty("file.separator");
    if (fSep != null && fSep.length() == 1)
        path = path.replace(fSep.charAt(0), '/');
    if (path.length() > 0 && path.charAt(0) != '/')
        path = '/' + path;
    try {
        return new InputSource(new URL("file", null,
               path).toString());
    }
    catch (java.net.MalformedURLException e) {
        throw new Error("unexpected MalformedURLException");
    }
  }  // end: fileInputSource

  /**
   * main method, starts execution of this task
   * 
   */
  public void execute () throws BuildException {
    boolean errors = false;
    String version = Version.getVersion();
    MessageHandler.logln("=======================\nTask " + version + 
                       "\nconverting file " + fofile + " to " + pdffile);
  
    if (!(new File(fofile).exists())) {
      errors = true;
      MessageHandler.errorln("Task Fop - ERROR: Formatting objects file " + fofile + " missing.");
    }
  
    XMLReader parser = createParser();
  
    if (parser == null) {
        MessageHandler.errorln("Task Fop - ERROR: Unable to create SAX parser");
        errors = true;
    }

    // setting the parser features
    try {
      parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    } catch (SAXException e) {
      MessageHandler.errorln("Error in setting up parser feature namespace-prefixes");
      MessageHandler.errorln("You need a parser which supports SAX version 2");  
      System.exit(1);  
    }

    if (!errors) {
      try {
        Driver driver = new Driver();
        driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", version);
        driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
        driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
	    driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
	    driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
        driver.setWriter(new PrintWriter(new FileWriter(pdffile)));
        driver.buildFOTree(parser, fileInputSource(fofile));
        driver.format();
        driver.render();
      } catch (Exception e) {
        MessageHandler.errorln("Task Fop - FATAL ERROR: " + e.getMessage());
        System.exit(1);
      }
    }
    MessageHandler.logln("=======================\n");  
  } // end: execute
}

