// Ant
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.fop.apps.*;

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

/* the code is adapted from Fops CommandLine class */
public class Fop {
  String fofile, pdffile;
  
  public void setFofile(String fofile) {
    this.fofile = fofile;
  }
  
  public void setPdffile(String pdffile) {
    this.pdffile = pdffile;
  }
  

  /**
   * creates a SAX parser, using the value of org.xml.sax.parser
   * defaulting to org.apache.xerces.parsers.SAXParser
   *
   * @return the created SAX parser
   */
  static Parser createParser() {
    String parserClassName = System.getProperty("org.xml.sax.parser");
    if (parserClassName == null) {
      parserClassName = "org.apache.xerces.parsers.SAXParser";
    }
    System.err.println("using SAX parser " + parserClassName);

    try {
      return (Parser) Class.forName(parserClassName).newInstance();
    } catch (ClassNotFoundException e) {
      System.err.println("Could not find " + parserClassName);
    } catch (InstantiationException e) {
      System.err.println("Could not instantiate " + parserClassName);
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

  public void execute () throws BuildException {
    boolean errors = false;
    String version = Version.getVersion();
    System.out.println("=======================\nTask " + version + 
                       "\nconverting file " + fofile + " to " + pdffile);
  
    if (!(new File(fofile).exists())) {
      errors = true;
      System.err.println("Task Fop - ERROR: Formatting objects file " + fofile + " missing.");
    }
  
    Parser parser = createParser();
  
    if (parser == null) {
        System.err.println("Task Fop - ERROR: Unable to create SAX parser");
        errors = true;
    }

    if (!errors) {
      try {
          Driver driver = new Driver();
          driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", version);
          driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
          driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
          driver.setWriter(new PrintWriter(new FileWriter(pdffile)));
          driver.buildFOTree(parser, fileInputSource(fofile));
          driver.format();
          driver.render();
      } catch (Exception e) {
          System.err.println("Task Fop - FATAL ERROR: " + e.getMessage());
          System.exit(1);
      }
    }
    System.out.println("=======================\n");  
  }
}

