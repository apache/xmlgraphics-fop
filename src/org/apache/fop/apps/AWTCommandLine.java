
package org.apache.fop.apps;
/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@af-software.de,
  Rainer Steinkuhle: Rainer.Steinkuhle@af-software.de,
  Stanislav Gorkhover: Stanislav.Gorkhover@af-software.de
 */


import org.apache.fop.viewer.*;
import org.apache.fop.render.awt.*;

import javax.swing.UIManager;
import java.awt.*;

// SAX
import org.xml.sax.Parser;
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




/**
 * initialize AWT previewer
 */

public class AWTCommandLine {


  public AWTCommandLine(AWTRenderer aRenderer) {

    PreviewDialog frame = new PreviewDialog(aRenderer);
    frame.validate();

    // center window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

 /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */

    static Parser createParser() {
	String parserClassName =
	    System.getProperty("org.xml.sax.parser");
	if (parserClassName == null) {
	    parserClassName = "org.apache.xerces.parsers.SAXParser";
	}
	System.err.println("using SAX parser " + parserClassName);

	try {
	    return (Parser)
		Class.forName(parserClassName).newInstance();
	} catch (ClassNotFoundException e) {
	    System.err.println("Could not find " + parserClassName);
	} catch (InstantiationException e) {
	    System.err.println("Could not instantiate "
			       + parserClassName);
	} catch (IllegalAccessException e) {
	    System.err.println("Could not access " + parserClassName);
	} catch (ClassCastException e) {
	    System.err.println(parserClassName + " is not a SAX driver");
	}
	return null;
    }

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
  }



  /* main
   */
  public static void main(String[] args) {
    try  {
      UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
    } catch (Exception e) {
      e.printStackTrace();
    }

    String srcPath = null;

    System.err.println(Version.getVersion());
    if (args.length == 1) {
      srcPath = args[0];
    }
    else {
      System.err.println("usage: java " +
                         "AWTCommandLine " +
                         "formatting-object-file");

      System.exit(1);
    }


    AWTRenderer renderer = new AWTRenderer();
    new AWTCommandLine(renderer);

//init parser
    Parser parser = createParser();

	if (parser == null) {
	    System.err.println("ERROR: Unable to create SAX parser");
	    System.exit(1);
	}

	try {
	    Driver driver = new Driver();
	    driver.setRenderer(renderer);

// init mappings: time
	    driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
	    driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");

// build FO tree: time
	    driver.buildFOTree(parser, fileInputSource(srcPath));

// layout FO tree: time
	    driver.format();

// render: time
        driver.render();

	} catch (Exception e) {
	    System.err.println("FATAL ERROR: " + e.getMessage());
	    System.exit(1);
	}

  }  // main
}  // AWTCommandLine

