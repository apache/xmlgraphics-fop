package org.apache.fop.apps;

/*
  originally contributed by
  Stanislav Gorkhover: stanislav.gorkhover@jcatalog.com
  jCatalog Software AG
 */


import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.awt.Graphics;
import java.awt.print.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.Page;
import org.apache.fop.messaging.MessageHandler;


/**
 * This class prints a xsl-fo dokument without interaction.
 * At the moment java has not the possibility to configure the printer and it's
 * options without interaction (30.03.2000).
 * This class allows to print a set of pages (from-to), even/odd pages and many copies.
 * - Print from page xxx: property name - start, value int
 * - Print to page xxx: property name - end, value int
 * - Print even/odd pages: property name - even, value boolean
 * - Print xxx copies: property name - copies, value int
 *
 */
public class PrintCommandLine extends CommandLine {


  public static void main(String[] args) {

    String version = Version.getVersion();
    MessageHandler.errorln(version);

    if (args.length != 1) {
      MessageHandler.errorln("usage: java [-Dstart=i] [-Dend=i]"
        + " [-Dcopies=i] [-Deven=true|false]"
        + " org.apache.fop.apps.PrintCommandLine formatting-object-file");
      System.exit(1);
    }

    XMLReader parser = createParser();
    
    if (parser == null) {
      MessageHandler.errorln("ERROR: Unable to create SAX parser");
      System.exit(1);
    }

    // setting the necessary parser features
    try {
      parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    } catch (SAXException e) {
      MessageHandler.errorln("Error in setting up parser feature namespace-prefixes");
      MessageHandler.errorln("You need a parser which supports SAX version 2");  
    }
    
    PrintRenderer renderer = new PrintRenderer();
    try {
      Driver driver = new Driver();

      driver.setRenderer(renderer);
      driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
      driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
	    driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
	    driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
      driver.buildFOTree(parser, fileInputSource(args[0]));
      driver.format();
      driver.render();
    } catch (Exception e) {
      MessageHandler.errorln("FATAL ERROR: " + e.getMessage());
      System.exit(1);
    }

    int copies = PrintRenderer.getIntProperty("copies", 1);
    renderer.setCopies(copies);

    PrinterJob pj = PrinterJob.getPrinterJob();
    pj.setPageable(renderer);

    pj.setCopies(copies);
    try {
      pj.print();
    } catch(PrinterException pe) {
      pe.printStackTrace();
    }
  }


  static class PrintRenderer extends AWTRenderer {

    static int EVEN_AND_ALL = 0;
    static int EVEN = 1;
    static int ODD = 2;

    int startNumber;
    int endNumber;
    int mode = EVEN_AND_ALL;
    int copies = 1;

    PrintRenderer() {
      super(null);

      startNumber = getIntProperty("start", 1) - 1;
      endNumber = getIntProperty("end", -1);

      mode = EVEN_AND_ALL;
      String str = System.getProperty("even");
      if (str != null) {
        try {
          mode = Boolean.valueOf(str).booleanValue() ? EVEN : ODD;
        } catch (Exception e) {
        }
      }

    }


    static int getIntProperty(String name, int def) {
      String propValue = System.getProperty(name);
      if (propValue != null) {
        try {
          return Integer.parseInt(propValue);
        } catch(Exception e) {
          return def;
        }
      }
      else {
        return def;
      }
    }

    public void render(AreaTree areaTree, PrintWriter writer) throws IOException {
      tree = areaTree;
      if (endNumber == -1) {
        endNumber = tree.getPages().size();
      }

      Vector numbers = getInvalidPageNumbers();
      for (int i = numbers.size() - 1; i > -1; i--)
        tree.getPages().removeElementAt(Integer.parseInt((String)numbers.elementAt(i)));

    }

    public void renderPage(Page page) {
      pageWidth  = (int)((float)page.getWidth() / 1000f);
      pageHeight = (int)((float)page.getHeight() / 1000f);
      super.renderPage(page);
    }


    private Vector getInvalidPageNumbers() {

      Vector vec = new Vector();
      int max = tree.getPages().size();
      boolean isValid;
      for (int i = 0; i < max; i++) {
        isValid = true;
        if (i < startNumber || i > endNumber) {
          isValid = false;
        }
        else if (mode != EVEN_AND_ALL) {
          if (mode == EVEN && ((i + 1) % 2 != 0))
            isValid = false;
          else if (mode == ODD && ((i + 1) % 2 != 1))
            isValid = false;
        }

        if (!isValid)
          vec.add(i + "");
      }

      return vec;
    }

    void setCopies(int val) {
      copies = val;
      Vector copie = tree.getPages();
      for (int i = 1; i < copies; i++) {
        tree.getPages().addAll(copie);
      }

    }

  } // class PrintRenderer
} // class PrintCommandLine

