/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

// FOP
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.PropertyListMapping;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.ConfigurationReader;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.tools.DocumentInputSource;
import org.apache.fop.tools.DocumentReader;


// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

// SAX
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

// Java
import java.io.*;


/**
 * Primary class that drives overall FOP process.
 * <P>
 * The simplest way to use this is to instantiate it with the
 * InputSource and OutputStream, then set the renderer desired, and
 * calling run();
 * <P>
 * Here is an example use of Driver which outputs PDF:
 *
 * <PRE>
 *   Driver driver = new Driver(new InputSource (args[0]), 
 *                              new FileOutputStream(args[1]));
 *   driver.setRenderer(RENDER_PDF);
 *   driver.run();
 * </PRE>

 * If neccessary, calling classes can call into the lower level
 * methods to setup and
 * render. Methods can be called to set the
 * Renderer to use, the (possibly multiple) ElementMapping(s) to
 * use and the OutputStream to use to output the results of the
 * rendering (where applicable). In the case of the Renderer and
 * ElementMapping(s), the Driver may be supplied either with the
 * object itself, or the name of the class, in which case Driver will
 * instantiate the class itself. The advantage of the latter is it
 * enables runtime determination of Renderer and ElementMapping(s).
 * <P>
 * Once the Driver is set up, the buildFOTree method
 * is called. Depending on whether DOM or SAX is being used, the
 * invocation of the method is either buildFOTree(Document) or
 * buildFOTree(Parser, InputSource) respectively.
 * <P>
 * A third possibility may be used to build the FO Tree, namely
 * calling getContentHandler() and firing the SAX events yourself.
 * <P>
 * Once the FO Tree is built, the format() and render() methods may be
 * called in that order.
 * <P>
 * Here is an example use of Driver which outputs to AWT:
 *
 * <PRE>
 *   Driver driver = new Driver();
 *   driver.setRenderer(new org.apache.fop.render.awt.AWTRenderer(translator));
 *   driver.buildFOTree(parser, fileInputSource(args[0]));
 *   driver.format();
 *   driver.render();
 * </PRE>
 */
public class Driver {
    
    /** Render to PDF. OutputStream must be set */
    public static final int RENDER_PDF = 1;
    
    /** Render to a GUI window. No OutputStream neccessary */
    public static final int RENDER_AWT = 2;

    /** Render to MIF. OutputStream must be set */
    public static final int RENDER_MIF = 3;

    /** Render to XML. OutputStream must be set */
    public static final int RENDER_XML = 4;
   
    /** Render to PRINT. No OutputStream neccessary */
    public static final int RENDER_PRINT = 5;

    /** the FO tree builder */
    private FOTreeBuilder _treeBuilder;

    /** the area tree that is the result of formatting the FO tree */
    private AreaTree _areaTree;

    /** the renderer to use to output the area tree */
    private Renderer _renderer;

    /** the source of the FO file */
    private InputSource _source;
    
    /** the stream to use to output the results of the renderer */
    private OutputStream _stream;

    /** The XML parser to use when building the FO tree */
    private XMLReader _reader;
    
    /** If true, full error stacks are reported */
    private boolean _errorDump = false;

    /** create a new Driver */
    public Driver() {
        reset();
    }

    public Driver(InputSource source, OutputStream stream) {
	this();
	_source = source;
	_stream = stream;
    }

    /**
     * Resets the Driver so it can be reused. Property and element mappings are reset to defaults.
     * The output stream is cleared. The renderer is cleared.
     */
    public synchronized void reset() 
    {
	_stream = null;
	_treeBuilder = new FOTreeBuilder();
	setupDefaultMappings();
    }
    
    /** 
     * Set the error dump option
     * @param dump if true, full stacks will be reported to the error log
     */
    public void setErrorDump(boolean dump) {
        _errorDump = dump;
    }

    /**
     * Set the OutputStream to use to output the result of the Renderer
     * (if applicable)
     * @param stream the stream to output the result of rendering to
     *
     */
    public void setOutputStream(OutputStream stream) {
        _stream = stream;
    }

    /**
     * Set the source for the FO document. This can be a normal SAX 
     * InputSource, or an DocumentInputSource containing a DOM document.
     * @see DocumentInputSource
     */
    public void setInputSource(InputSource source) 
    {
	_source = source;
    }
    
    /**
     * Sets the reader used when reading in the source. If not set,
     * this defaults to a basic SAX parser.
     */
    public void setXMLReader(XMLReader reader) 
    {
	_reader = reader;
    }
    

    /**
     * Sets all the element and property list mappings to their default values.
     *
     */
    public void setupDefaultMappings() 
    {
	addElementMapping("org.apache.fop.fo.StandardElementMapping");
	addPropertyList  ("org.apache.fop.fo.StandardPropertyListMapping");

	addElementMapping("org.apache.fop.svg.SVGElementMapping");
	addPropertyList  ("org.apache.fop.svg.SVGPropertyListMapping");

        addElementMapping("org.apache.fop.extensions.ExtensionElementMapping");
	addPropertyList  ("org.apache.fop.extensions.ExtensionPropertyListMapping");
    }

    /**
     * Set the rendering type to use. Must be one of 
     * <ul>
     *   <li>RENDER_PDF
     *   <li>RENDER_AWT
     *   <li>RENDER_MIF
     *   <li>RENDER_XML
     * </ul>
     * @param renderer the type of renderer to use
     */
    public void setRenderer(int renderer) 
	throws IllegalArgumentException
    {
	switch (renderer) {
	case RENDER_PDF:
	    setRenderer(new org.apache.fop.render.pdf.PDFRenderer());
	    break;
	case RENDER_AWT:
	    throw new IllegalArgumentException("Use renderer form of setRenderer() for AWT");
	case RENDER_PRINT:
	    throw new IllegalArgumentException("Use renderer form of setRenderer() for PRINT");
	case RENDER_MIF:
	    setRenderer(new org.apache.fop.render.mif.MIFRenderer());
	    break;
	case RENDER_XML:
	    setRenderer(new org.apache.fop.render.xml.XMLRenderer());
	    break;
	default:
	    throw new IllegalArgumentException("Unknown renderer type");
	}
	
    }
    

    /** 
     * Set the Renderer to use 
     * @param renderer the renderer instance to use
     */
    public void setRenderer(Renderer renderer) {
        _renderer = renderer;
    }

    /**
     * @deprecated use renderer.setProducer(version) + setRenderer(renderer) or just setRenderer(renderer_type) which will use the default producer string.
     * @see #setRenderer(int)
     * @see #setRenderer(Renderer)
     */
    public void setRenderer(String rendererClassName, String version) 
    {
	setRenderer(rendererClassName);
    }
    
    /**
     * Set the class name of the Renderer to use as well as the
     * producer string for those renderers that can make use of it.
     * @param rendererClassName classname of the renderer to use such as 
     * "org.apache.fop.render.pdf.PDFRenderer"
     * @exception IllegalArgumentException if the classname was invalid.
     * @see #setRenderer(int)
     */
    public void setRenderer(String rendererClassName) 
	throws IllegalArgumentException
    {
	try {
	    _renderer = (Renderer) Class.forName(rendererClassName).newInstance();
	    _renderer.setProducer(Version.getVersion());
	}
	catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find " + 
					       rendererClassName);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate " +
                                   rendererClassName);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access " + rendererClassName);
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException(rendererClassName + " is not a renderer");
        }
    }

    /**
     * Add the given element mapping.
     * An element mapping maps element names to Java classes.
     *
     * @param mapping the element mappingto add
     */
    public void addElementMapping(ElementMapping mapping) {
	mapping.addToBuilder(_treeBuilder);
    }

    /**
     * add the element mapping with the given class name
     */
    public void addElementMapping(String mappingClassName) 
	throws IllegalArgumentException
    {
	try {
            ElementMapping mapping = (ElementMapping) Class.forName(
                     mappingClassName).newInstance();
	    addElementMapping(mapping);
	} catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find " + mappingClassName);
	}
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate " +
                                   mappingClassName);
	}
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access " + mappingClassName);
	}
        catch (ClassCastException e) {
            throw new IllegalArgumentException(mappingClassName + " is not an ElementMapping");
	}
    }

    /**
     * Add the PropertyListMapping.
     */
    public void addPropertyList(PropertyListMapping mapping) 
    {
	mapping.addToBuilder(_treeBuilder);
    }
    

    /**
     * Add the PropertyListMapping with the given class name.
     */
    public void addPropertyList(String listClassName) 
	throws IllegalArgumentException
    {
	try {
            PropertyListMapping mapping = (PropertyListMapping) Class.forName(
                     listClassName).newInstance();
	    addPropertyList(mapping);
	    
	} catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find " + listClassName);
	}
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate " +
					       listClassName);
	}
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access " + listClassName);
	}
        catch (ClassCastException e) {
            throw new IllegalArgumentException(listClassName + " is not an ElementMapping");
	}
    }

    /**
     * Returns the tree builder (a SAX ContentHandler).
     *
     * Used in situations where SAX is used but not via a FOP-invoked
     * SAX parser. A good example is an XSLT engine that fires SAX
     * events but isn't a SAX Parser itself.
     */
    public ContentHandler getContentHandler() {
        return _treeBuilder;
    }

    /**
     * Build the formatting object tree using the given SAX Parser and
     * SAX InputSource
     */
    public synchronized void buildFOTree(XMLReader parser,
                            InputSource source)
	throws FOPException 
    {

        parser.setContentHandler(_treeBuilder);
        try {
            parser.parse(source);
        } catch (SAXException e) {
            if (e.getException() instanceof FOPException) {
                throw (FOPException) e.getException();
            } else {
                throw new FOPException(e);
            }
        }
        catch (IOException e) {
            throw new FOPException(e);
        }
    }

    /**
     * Build the formatting object tree using the given DOM Document
     */
    public synchronized void buildFOTree(Document document) 
	throws FOPException 
    {
	try {
	    DocumentInputSource source = new DocumentInputSource(document);
	    DocumentReader reader = new DocumentReader();
	    reader.setContentHandler(_treeBuilder);
	    reader.parse(source);
	} catch (SAXException e) {
            throw new FOPException(e);
	} catch (IOException e) {
            throw new FOPException(e);
	}
	
    } 

    /**
     * Dumps an error
     */
    public void dumpError(Exception e) {
        if (_errorDump) {
            if (e instanceof SAXException) {
                e.printStackTrace();
                if (((SAXException) e).getException() != null) {
                    ((SAXException) e).getException().printStackTrace();
                }
            }
            else if (e instanceof FOPException) {
                e.printStackTrace();
                if (((FOPException) e).getException() != null) {
                    ((FOPException) e).getException().printStackTrace();
                }
            }
	    else {
                e.printStackTrace();
            }
        }
    }

    /**
     * format the formatting object tree into an area tree
     */
    public synchronized void format() throws FOPException {
        FontInfo fontInfo = new FontInfo();
        _renderer.setupFontInfo(fontInfo);

        _areaTree = new AreaTree();
        _areaTree.setFontInfo(fontInfo);

        _treeBuilder.format(_areaTree);
    }

    /**
     * render the area tree to the output form
     */
    public synchronized void render() throws IOException, FOPException {
        _renderer.render(_areaTree, _stream);
    }

    /**
     * Runs the formatting and renderering process using the previously set
     * inputsource and outputstream
     */
    public synchronized void run()
	throws IOException, FOPException 
    {
	if (_renderer == null) {
	    setRenderer(RENDER_PDF);
	}
	if (_source == null) {
	    throw new FOPException("InputSource is not set.");
	}
	if (_reader == null) {
	    if (!(_source instanceof DocumentInputSource)) {
		_reader = ConfigurationReader.createParser();
	    }
	}
	if (_source instanceof DocumentInputSource) {
	    buildFOTree(((DocumentInputSource)_source).getDocument());
	}
	else {
	    buildFOTree(_reader, _source);
	}
	format();
	render();
    }
    
}
