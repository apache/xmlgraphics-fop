package org.apache.fop.apps;

import org.xml.sax.*;
import com.jclark.xsl.sax.*;
import java.io.*;

// FOP
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping; 
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.Renderer;

//////////////////////////////////////////////////////////////////////////////////////
/**
 * A DocumentHandler that writes a PDF representation to an OutputStream.
 * 
 * Use with James Clark's XT. Just put FOP on your class path and add
 * 	<xsl:output method="fop:org.apache.fop.apps.PDFOutputHandler"
 *	            xmlns:fop="http://www.jclark.com/xt/java"/>
 * to your stylesheet. Now XT will automatically call FOP.
 * 
 */
public class PDFOutputHandler extends FOTreeBuilder implements OutputDocumentHandler {
  
  /** the area tree that is the result of formatting the FO tree */
  protected AreaTree areaTree;
  
  /** the renderer to use to output the area tree */
  protected Renderer renderer;
  
  /** the PrintWriter to use to output the results of the renderer */
  protected PrintWriter writer;

  private boolean keepOpen;

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   */
  public PDFOutputHandler() {
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   */
  public PDFOutputHandler(OutputStream out) {
    this();
    this.writer = new PrintWriter(out);
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   */
  public DocumentHandler init(Destination dest, AttributeList atts) throws IOException {
    this.writer = new PrintWriter(dest.getOutputStream("application/pdf", null));
    this.keepOpen = dest.keepOpen();

    String version = org.apache.fop.apps.Version.getVersion();
    setRenderer("org.apache.fop.render.pdf.PDFRenderer", version);
    addElementMapping("org.apache.fop.fo.StandardElementMapping");
    addElementMapping("org.apache.fop.svg.SVGElementMapping");
    return this;
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * set the class name of the Renderer to use as well as the
   * producer string for those renderers that can make use of it
   */
  public void setRenderer(String rendererClassName, String producer) {
      this.renderer = createRenderer(rendererClassName);
      this.renderer.setProducer(producer);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////
  /** 
   * SAX passthrough, finish rendering the document
   */
  public void endDocument() throws SAXException {
    super.endDocument();
    
    try {
      doFormat();
      doRender();
    } catch (IOException io) {
      throw new SAXException(io);
    } catch (FOPException fop) {
      throw new SAXException(fop);
    }
    writer.flush();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * format the formatting object tree into an area tree
   */
  public void doFormat()
    throws FOPException {
    FontInfo fontInfo = new FontInfo();
    this.renderer.setupFontInfo(fontInfo);

    this.areaTree = new AreaTree();
    this.areaTree.setFontInfo(fontInfo);

    format(areaTree);
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * render the area tree to the output form
   */
  public void doRender()
    throws IOException {
    this.renderer.render(areaTree, this.writer);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * add the given element mapping.
   *
   * an element mapping maps element names to Java classes
   */
  public void addElementMapping(ElementMapping mapping) {
    mapping.addToBuilder(this);
  }
    
  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * add the element mapping with the given class name
   */
  public void addElementMapping(String mappingClassName) {
    createElementMapping(mappingClassName).addToBuilder(this);
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * protected method used by addElementMapping(String) to
   * instantiate element mapping class
   */
  protected ElementMapping createElementMapping(String mappingClassName) {
    System.err.println("using element mapping " + mappingClassName);

    try {
        return (ElementMapping)
    	Class.forName(mappingClassName).newInstance();
    } catch (ClassNotFoundException e) {
        System.err.println("Could not find " + mappingClassName);
    } catch (InstantiationException e) {
        System.err.println("Could not instantiate "
    		       + mappingClassName);
    } catch (IllegalAccessException e) {
        System.err.println("Could not access " + mappingClassName);
    } catch (ClassCastException e) {
        System.err.println(mappingClassName + " is not an element mapping"); 
    }
    return null;
  }

  //////////////////////////////////////////////////////////////////////////////////////
  /**
   * protected method used by setRenderer(String, String) to
   * instantiate the Renderer class
   */
  protected Renderer createRenderer(String rendererClassName) {
    System.err.println("using renderer " + rendererClassName);

    try {
        return (Renderer)
    	Class.forName(rendererClassName).newInstance();
    } catch (ClassNotFoundException e) {
        System.err.println("Could not find " + rendererClassName);
    } catch (InstantiationException e) {
        System.err.println("Could not instantiate "
    		       + rendererClassName);
    } catch (IllegalAccessException e) {
        System.err.println("Could not access " + rendererClassName);
    } catch (ClassCastException e) {
        System.err.println(rendererClassName + " is not a renderer"); 
    }
    return null;
  }
}

