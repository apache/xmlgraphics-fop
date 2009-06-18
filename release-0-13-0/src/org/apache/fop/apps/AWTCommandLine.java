
package org.apache.fop.apps;
/*
  originally contributed by
  Juergen Verwohlt: Juergen.Verwohlt@jcatalog.com,
  Rainer Steinkuhle: Rainer.Steinkuhle@jcatalog.com,
  Stanislav Gorkhover: Stanislav.Gorkhover@jcatalog.com
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;



/**
 * initialize AWT previewer
 */

public class AWTCommandLine {


  public static String TRANSLATION_PATH = "/org/apache/fop/viewer/resources/";


  private Translator resource;



  public AWTCommandLine(String srcFile, String language) {

    if (language == null)
      language = System.getProperty("user.language");

    resource = getResourceBundle(TRANSLATION_PATH + "resources." + language);

    UserMessage.setTranslator(getResourceBundle(TRANSLATION_PATH + "messages." + language));

    resource.setMissingEmphasized(false);

    AWTRenderer renderer = new AWTRenderer(resource);
    PreviewDialog frame = createPreviewDialog(renderer, resource);
    renderer.setProgressListener(frame);


//init parser
    frame.progress(resource.getString("Init parser") + " ...");
    Parser parser = createParser();


	if (parser == null) {
	    System.err.println("ERROR: Unable to create SAX parser");
	    System.exit(1);
	}


	try {
	    Driver driver = new Driver();
	    driver.setRenderer(renderer);

// init mappings: time
        frame.progress(resource.getString("Init mappings") + " ...");

	    driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
	    driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");

// build FO tree: time
        frame.progress(resource.getString("Build FO tree") + " ...");
	    driver.buildFOTree(parser, fileInputSource(srcFile));

// layout FO tree: time
        frame.progress(resource.getString("Layout FO tree") + " ...");
	    driver.format();

// render: time
        frame.progress(resource.getString("Render") + " ...");
        driver.render();

        frame.progress(resource.getString("Show"));

	} catch (Exception e) {
	    System.err.println("FATAL ERROR: " + e.getMessage());
        e.printStackTrace();
	    System.exit(1);
	}
  }


    static Parser createParser() {
	String parserClassName =
	    System.getProperty("org.xml.sax.parser");
	if (parserClassName == null) {
	    parserClassName = "com.jclark.xml.sax.Driver";
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



  protected PreviewDialog createPreviewDialog(AWTRenderer renderer, Translator res) {
    PreviewDialog frame = new PreviewDialog(renderer, res);
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
    return frame;
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


  private SecureResourceBundle getResourceBundle(String path) {
    InputStream in = null;

    try {
    URL url = getClass().getResource(path);
    in = url.openStream();
    } catch(Exception ex) {
      System.out.println("Can't find URL to: <" + path + "> " + ex.getMessage());
    }
    return new SecureResourceBundle(in);
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
    String language = null;
    String imageDir = null;

    System.err.println(Version.getVersion());
    if (args.length < 1 || args.length > 3) {
      System.err.println("usage: java AWTCommandLine " +
                         "formatting-object-file [language] ");
      System.exit(1);
    }

    srcPath = args[0];
    if (args.length > 1) {
      language = args[1];
    }

    new AWTCommandLine(srcPath, language);

  }  // main
}  // AWTCommandLine

